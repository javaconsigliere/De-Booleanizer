package org.jc.chatgpt;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class FilterPromptPanel extends JPanel {
    private JTextArea filterInputArea;
    private JTextArea promptInputArea;

    /**
     * Constructs a FilterPromptPanel with two labeled, scrollable text areas.
     */
    public FilterPromptPanel() {
        // Set the layout manager to arrange components vertically
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // Initialize the text areas
        filterInputArea = new JTextArea();
        promptInputArea = new JTextArea();

        // Configure the text areas (optional customization)
        configureTextArea(filterInputArea, "Filter-Input");
        configureTextArea(promptInputArea, "Prompt-Input");

        // Add the scrollable text areas to the panel
        add(createScrollPane(filterInputArea, "Filter-Input"));
        add(Box.createRigidArea(new Dimension(0, 10))); // Add vertical spacing
        add(createScrollPane(promptInputArea, "Prompt-Input"));
    }

    /**
     * Configures a JTextArea with default settings.
     *
     * @param textArea The JTextArea to configure.
     * @param name     The name/title for the text area.
     */
    private void configureTextArea(JTextArea textArea, String name) {
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setFont(new Font("SansSerif", Font.PLAIN, 14));
        textArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    }

    /**
     * Creates a JScrollPane containing the given JTextArea with a titled border.
     *
     * @param textArea The JTextArea to include in the scroll pane.
     * @param title    The title for the border.
     * @return A JScrollPane containing the text area.
     */
    private JScrollPane createScrollPane(JTextArea textArea, String title) {
        JScrollPane scrollPane = new JScrollPane(textArea);
        TitledBorder border = BorderFactory.createTitledBorder(title);
        border.setTitleFont(new Font("SansSerif", Font.BOLD, 12));
        scrollPane.setBorder(border);
        return scrollPane;
    }

    /**
     * Retrieves the text from the Filter-Input area.
     *
     * @return The text from the Filter-Input area.
     */
    public String getFilterInputText() {
        return filterInputArea.getText();
    }

    /**
     * Sets the text of the Filter-Input area.
     *
     * @param text The text to set in the Filter-Input area.
     */
    public void setFilterInputText(String text) {
        filterInputArea.setText(text);
    }

    /**
     * Retrieves the text from the Prompt-Input area.
     *
     * @return The text from the Prompt-Input area.
     */
    public String getPromptInputText() {
        return promptInputArea.getText();
    }

    /**
     * Sets the text of the Prompt-Input area.
     *
     * @param text The text to set in the Prompt-Input area.
     */
    public void setPromptInputText(String text) {
        promptInputArea.setText(text);
    }
}
