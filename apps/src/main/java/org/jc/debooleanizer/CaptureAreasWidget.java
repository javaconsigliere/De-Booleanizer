package org.jc.debooleanizer;

import io.xlogistx.gui.SelectionArea;
import io.xlogistx.gui.SelectionAreaSet;

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
 * Widget that manages the capture areas of a {@link SelectionAreaSet}: one row per
 * {@link SelectionArea} with a checkbox marking whether the area is part of the next
 * capture sweep and a button to remove it, plus Add/Clear controls in a toolbar.
 * <p>
 * All mutations go through this widget so the row display and the underlying set stay
 * in sync. {@link #addArea(Rectangle)} and {@link #clearAreas()} are safe to call from
 * any thread (the set and the checked-state map are thread-safe, row updates are
 * dispatched to the EDT); {@link #getCheckedAreas()} is safe from the capture thread.
 */
public class CaptureAreasWidget extends JPanel {

    private final SelectionAreaSet selectionAreaSet;
    // monotonic so a name is never reused after a removal, per-area state keyed by
    // name elsewhere (e.g. last-capture maps) can never collide with a stale entry
    private final AtomicInteger nameSequence = new AtomicInteger();
    // checkbox state readable from any thread, the JCheckBox itself is EDT-only
    private final Map<SelectionArea, Boolean> checkedState = new ConcurrentHashMap<>();
    // EDT-only
    private final Map<SelectionArea, JPanel> rows = new LinkedHashMap<>();
    private final JPanel rowsPanel;
    private final TitledBorder titledBorder;

    private volatile Runnable addAction;
    private volatile Consumer<SelectionArea> areaRemovedListener;
    private volatile BiConsumer<SelectionArea, String> areaRenamedListener;

    public CaptureAreasWidget(SelectionAreaSet selectionAreaSet) {
        this.selectionAreaSet = selectionAreaSet;
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
    public CaptureAreasWidget setAreaRemovedListener(Consumer<SelectionArea> listener) {
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
    public CaptureAreasWidget setAreaRenamedListener(BiConsumer<SelectionArea, String> listener) {
        areaRenamedListener = listener;
        return this;
    }

    /**
     * Adds the given rectangle as a new named, checked capture area.
     *
     * @param area the screen rectangle to capture
     * @return the created selection area
     */
    public SelectionArea addArea(Rectangle area) {
        SelectionArea selectionArea = new SelectionArea("area-" + nameSequence.incrementAndGet(), null, area);
        checkedState.put(selectionArea, Boolean.TRUE);
        selectionAreaSet.addSelectionArea(selectionArea);
        SwingUtilities.invokeLater(() -> addRow(selectionArea));
        return selectionArea;
    }

    /**
     * Removes the given area from the set and the display.
     *
     * @param selectionArea the area to remove
     */
    public void removeArea(SelectionArea selectionArea) {
        selectionAreaSet.removeSelectionAreas(selectionArea);
        checkedState.remove(selectionArea);
        SwingUtilities.invokeLater(() -> removeRow(selectionArea));
        notifyRemoved(selectionArea);
    }

    /**
     * Removes all areas from the set and the display.
     */
    public void clearAreas() {
        SelectionArea[] areas = selectionAreaSet.getSelectionAreas();
        selectionAreaSet.clearSelectionAreas();
        checkedState.clear();
        SwingUtilities.invokeLater(() -> {
            rows.clear();
            rowsPanel.removeAll();
            refresh();
        });
        for (SelectionArea selectionArea : areas)
            notifyRemoved(selectionArea);
    }

    /**
     * @return the checked areas in insertion order, the ones the next capture sweep
     *         should shoot; never null
     */
    public SelectionArea[] getCheckedAreas() {
        List<SelectionArea> ret = new ArrayList<>();
        for (SelectionArea selectionArea : selectionAreaSet.getSelectionAreas()) {
            if (Boolean.TRUE.equals(checkedState.get(selectionArea)))
                ret.add(selectionArea);
        }
        return ret.toArray(new SelectionArea[0]);
    }

    private void addRow(SelectionArea selectionArea) {
        Rectangle area = selectionArea.getSelectionArea();
        JCheckBox checkBox = new JCheckBox("", true);
        checkBox.setToolTipText("Include this area in the capture");
        checkBox.addItemListener(e -> checkedState.put(selectionArea, checkBox.isSelected()));

        JTextField nameField = new JTextField(selectionArea.getName(), 10);
        nameField.setToolTipText("Area name, press Enter or leave the field to rename");
        nameField.addActionListener(e -> commitRename(selectionArea, nameField));
        nameField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                commitRename(selectionArea, nameField);
            }
        });

        JLabel geometryLabel = new JLabel(area.width + "x" + area.height + " @ (" + area.x + "," + area.y + ")");

        JButton removeButton = new JButton("X");
        removeButton.setMargin(new Insets(0, 4, 0, 4));
        removeButton.setToolTipText("Remove this area");
        removeButton.addActionListener(e -> removeArea(selectionArea));

        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        row.add(removeButton);
        row.add(checkBox);
        row.add(nameField);
        row.add(geometryLabel);
        row.setAlignmentX(LEFT_ALIGNMENT);
        // keep BoxLayout from stretching the row vertically
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, row.getPreferredSize().height));
        rows.put(selectionArea, row);
        rowsPanel.add(row);
        refresh();
    }

    /**
     * Applies the name typed in the row's name field to its area. An empty name or
     * one already used by another area is rejected and the field reverts to the
     * current name.
     */
    private void commitRename(SelectionArea selectionArea, JTextField nameField) {
        String newName = nameField.getText() != null ? nameField.getText().trim() : "";
        String oldName = selectionArea.getName();
        if (newName.equals(oldName))
            return;
        if (newName.isEmpty() || nameInUse(newName)) {
            nameField.setText(oldName);
            return;
        }
        selectionArea.setName(newName);
        // normalize the display in case the typed value had surrounding whitespace
        nameField.setText(newName);
        BiConsumer<SelectionArea, String> listener = areaRenamedListener;
        if (listener != null)
            listener.accept(selectionArea, oldName);
    }

    private boolean nameInUse(String name) {
        for (SelectionArea selectionArea : selectionAreaSet.getSelectionAreas()) {
            if (name.equals(selectionArea.getName()))
                return true;
        }
        return false;
    }

    private void removeRow(SelectionArea selectionArea) {
        JPanel row = rows.remove(selectionArea);
        if (row != null) {
            rowsPanel.remove(row);
            refresh();
        }
    }

    private void refresh() {
        titledBorder.setTitle("Capture Areas (" + selectionAreaSet.getSelectionAreas().length + ")");
        rowsPanel.revalidate();
        rowsPanel.repaint();
        repaint();
    }

    private void notifyRemoved(SelectionArea selectionArea) {
        Consumer<SelectionArea> listener = areaRemovedListener;
        if (listener != null)
            listener.accept(selectionArea);
    }
}
