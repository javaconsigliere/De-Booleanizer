package org.jc.debooleanizer;

import io.xlogistx.gui.CaptureArea;
import io.xlogistx.gui.CaptureAreaSet;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Widget that manages the capture areas of a {@link CaptureAreaSet}: one row per
 * {@link CaptureArea} with a checkbox marking whether the area is part of the next
 * capture sweep, an editable name field and a button to remove it, plus Add/Clear
 * controls in a toolbar.
 * <p>
 * All mutations go through this widget so the row display and the underlying set stay
 * in sync. {@link #addArea(Rectangle)} and {@link #clearAreas()} are safe to call from
 * any thread (the set and the checked-state map are thread-safe, row updates are
 * dispatched to the EDT); {@link #getCheckedAreas()} is safe from the capture thread.
 */
public class CaptureAreasWidget extends JPanel {

    private final CaptureAreaSet captureAreaSet;
    // monotonic so a name is never reused after a removal, per-area state keyed by
    // name elsewhere (e.g. last-capture maps) can never collide with a stale entry
    private final AtomicInteger nameSequence = new AtomicInteger();
    // checkbox state readable from any thread, the JCheckBox itself is EDT-only
    private final Map<CaptureArea, Boolean> checkedState = new ConcurrentHashMap<>();
    // EDT-only
    private final Map<CaptureArea, JPanel> rows = new LinkedHashMap<>();
    private final JPanel rowsPanel;
    private final TitledBorder titledBorder;

    private volatile Runnable addAction;
    private volatile Consumer<CaptureArea> areaRemovedListener;
    private volatile BiConsumer<CaptureArea, String> areaRenamedListener;

    public CaptureAreasWidget(CaptureAreaSet captureAreaSet) {
        this.captureAreaSet = captureAreaSet;
        setLayout(new BorderLayout());
        titledBorder = BorderFactory.createTitledBorder("Capture Areas (0)");
        titledBorder.setTitleFont(new Font("SansSerif", Font.BOLD, 12));
        setBorder(titledBorder);

        JButton addButton = new JButton("Add");
        addButton.setToolTipText("Drag out a new capture area");
        addButton.addActionListener(e -> {
            Runnable action = addAction;
            if (action != null)
                action.run();
        });
        JButton clearButton = new JButton("Clear");
        clearButton.setToolTipText("Remove all capture areas");
        clearButton.addActionListener(e -> clearAreas());

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        toolbar.add(addButton);
        toolbar.add(clearButton);
        add(toolbar, BorderLayout.NORTH);

        rowsPanel = new JPanel();
        rowsPanel.setLayout(new BoxLayout(rowsPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(rowsPanel,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setPreferredSize(new Dimension(300, 90));
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * Sets the action of the Add button, typically hides the owner frame, lets the
     * user drag out a rectangle and feeds it back via {@link #addArea(Rectangle)}.
     *
     * @param action the add action, null disables the button behavior
     * @return this instance, for fluent chaining
     */
    public CaptureAreasWidget setAddAction(Runnable action) {
        addAction = action;
        return this;
    }

    /**
     * Sets the listener invoked once per area removed via row button or {@link #clearAreas()},
     * lets the owner prune per-area state such as last-capture caches.
     *
     * @param listener the removal listener, may be null
     * @return this instance, for fluent chaining
     */
    public CaptureAreasWidget setAreaRemovedListener(Consumer<CaptureArea> listener) {
        areaRemovedListener = listener;
        return this;
    }

    /**
     * Sets the listener invoked after an area is renamed via its row name field,
     * receives the area (already carrying the new name) and its previous name so the
     * owner can migrate per-area state keyed by name.
     *
     * @param listener the rename listener, may be null
     * @return this instance, for fluent chaining
     */
    public CaptureAreasWidget setAreaRenamedListener(BiConsumer<CaptureArea, String> listener) {
        areaRenamedListener = listener;
        return this;
    }

    /**
     * Adds the given rectangle as a new named, checked capture area.
     *
     * @param area the screen rectangle to capture
     * @return the created capture area
     */
    public CaptureArea addArea(Rectangle area) {
        CaptureArea captureArea = new CaptureArea("area-" + nameSequence.incrementAndGet(), null, area);
        checkedState.put(captureArea, Boolean.TRUE);
        captureAreaSet.addCaptureArea(captureArea);
        SwingUtilities.invokeLater(() -> addRow(captureArea));
        return captureArea;
    }

    /**
     * Removes the given area from the set and the display.
     *
     * @param captureArea the area to remove
     */
    public void removeArea(CaptureArea captureArea) {
        captureAreaSet.removeCaptureAreas(captureArea);
        checkedState.remove(captureArea);
        SwingUtilities.invokeLater(() -> removeRow(captureArea));
        notifyRemoved(captureArea);
    }

    /**
     * Removes all areas from the set and the display.
     */
    public void clearAreas() {
        CaptureArea[] areas = captureAreaSet.getCaptureAreas();
        captureAreaSet.clearCaptureAreas();
        checkedState.clear();
        SwingUtilities.invokeLater(() -> {
            rows.clear();
            rowsPanel.removeAll();
            refresh();
        });
        for (CaptureArea captureArea : areas)
            notifyRemoved(captureArea);
    }

    /**
     * @return the checked areas in insertion order, the ones the next capture sweep
     *         should shoot; never null
     */
    public CaptureArea[] getCheckedAreas() {
        List<CaptureArea> ret = new ArrayList<>();
        for (CaptureArea captureArea : captureAreaSet.getCaptureAreas()) {
            if (Boolean.TRUE.equals(checkedState.get(captureArea)))
                ret.add(captureArea);
        }
        return ret.toArray(new CaptureArea[0]);
    }

    private void addRow(CaptureArea captureArea) {
        Rectangle area = captureArea.getCaptureArea();
        JCheckBox checkBox = new JCheckBox("", true);
        checkBox.setToolTipText("Include this area in the capture");
        checkBox.addItemListener(e -> checkedState.put(captureArea, checkBox.isSelected()));

        JTextField nameField = new JTextField(captureArea.getName(), 10);
        nameField.setToolTipText("Area name, press Enter or leave the field to rename");
        nameField.addActionListener(e -> commitRename(captureArea, nameField));
        nameField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                commitRename(captureArea, nameField);
            }
        });

        JLabel geometryLabel = new JLabel(area.width + "x" + area.height + " @ (" + area.x + "," + area.y + ")");

        JButton removeButton = new JButton("X");
        removeButton.setMargin(new Insets(0, 4, 0, 4));
        removeButton.setToolTipText("Remove this area");
        removeButton.addActionListener(e -> removeArea(captureArea));

        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        row.add(removeButton);
        row.add(checkBox);
        row.add(nameField);
        row.add(geometryLabel);
        row.setAlignmentX(LEFT_ALIGNMENT);
        // keep BoxLayout from stretching the row vertically
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, row.getPreferredSize().height));
        rows.put(captureArea, row);
        rowsPanel.add(row);
        refresh();
    }

    /**
     * Applies the name typed in the row's name field to its area. An empty name or
     * one already used by another area is rejected and the field reverts to the
     * current name.
     */
    private void commitRename(CaptureArea captureArea, JTextField nameField) {
        String newName = nameField.getText() != null ? nameField.getText().trim() : "";
        String oldName = captureArea.getName();
        if (newName.equals(oldName))
            return;
        if (newName.isEmpty() || nameInUse(newName)) {
            nameField.setText(oldName);
            return;
        }
        captureArea.setName(newName);
        // normalize the display in case the typed value had surrounding whitespace
        nameField.setText(newName);
        BiConsumer<CaptureArea, String> listener = areaRenamedListener;
        if (listener != null)
            listener.accept(captureArea, oldName);
    }

    private boolean nameInUse(String name) {
        for (CaptureArea captureArea : captureAreaSet.getCaptureAreas()) {
            if (name.equals(captureArea.getName()))
                return true;
        }
        return false;
    }

    private void removeRow(CaptureArea captureArea) {
        JPanel row = rows.remove(captureArea);
        if (row != null) {
            rowsPanel.remove(row);
            refresh();
        }
    }

    private void refresh() {
        titledBorder.setTitle("Capture Areas (" + captureAreaSet.getCaptureAreas().length + ")");
        rowsPanel.revalidate();
        rowsPanel.repaint();
        repaint();
    }

    private void notifyRemoved(CaptureArea captureArea) {
        Consumer<CaptureArea> listener = areaRemovedListener;
        if (listener != null)
            listener.accept(captureArea);
    }
}
