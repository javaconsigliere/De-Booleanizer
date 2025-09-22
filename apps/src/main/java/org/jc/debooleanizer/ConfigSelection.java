package org.jc.debooleanizer;

import org.zoxweb.shared.util.NVGenericMap;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class ConfigSelection {

    // Public selection box variable
    private final JFrame mainFrame;
    private NVGenericMap selectionInfo = null;
    private final Consumer<String> aiKeyUpdater;


    private String aiAPIKey;

    // Constructor
    public ConfigSelection(JFrame mainFrame, Consumer<String> apiUpdater) {
        this.mainFrame = mainFrame;
        this.aiKeyUpdater = apiUpdater;
    }


    // Method to display the Local OCR settings dialog
    public void showLocalOCRDialog() {
        // Create text fields
        JTextField pathField = new JTextField(20);
        JTextField languageField = new JTextField(10);

        // Create buttons
        JButton setButton = new JButton("Set");
        JButton cancelButton = new JButton("Cancel");

        // Create the dialog
        JDialog dialog = new JDialog((Frame) null, "Local OCR Settings", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        // Set up the layout
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Add components to the panel
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Path:"), gbc);

        gbc.gridx = 1;
        panel.add(pathField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Language:"), gbc);

        gbc.gridx = 1;
        panel.add(languageField, gbc);

        // Buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonsPanel.add(setButton);
        buttonsPanel.add(cancelButton);

        // Add action listeners for buttons
        setButton.addActionListener(e -> {

            // Retrieve input values
            String path = pathField.getText();
            String language = languageField.getText();

            // Perform validation if necessary
            // ...

            // Call internal selection parameters
            setSelectionInfo(new NVGenericMap("local-ocr").build("path", path).build("language", language));

            // Close the dialog
            dialog.dispose();

        });

        cancelButton.addActionListener(e -> {

            // Reset selection to "No OCR"
//            selectionBox.setSelectedIndex(0);
            dialog.dispose();

        });

        // Assemble the dialog
        dialog.getContentPane().setLayout(new BorderLayout());
        dialog.getContentPane().add(panel, BorderLayout.CENTER);
        dialog.getContentPane().add(buttonsPanel, BorderLayout.SOUTH);
        dialog.pack();
//        dialog.setLocationRelativeTo(mainFrame);
        dialog.setVisible(true);
    }

    // Method to display the Remote OCR settings dialog
    public void showRemoteOCRDialog() {
        // Create text field
        JTextField apiKeyField = new JTextField(25);
        JTextField imageFormat = new JTextField(15);

        // Create buttons
        JButton setButton = new JButton("Set");
        JButton cancelButton = new JButton("Cancel");

        // Create the dialog
        JDialog dialog = new JDialog((Frame) null, "Remote OCR Settings", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        // Set up the layout
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Add components to the panel
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("API-KEY:"), gbc);

        gbc.gridx = 1;
        panel.add(apiKeyField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("IMAGE-FORMAT:"), gbc);

        gbc.gridx = 1;
        panel.add(imageFormat, gbc);


        // Buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonsPanel.add(setButton);
        buttonsPanel.add(cancelButton);

        // Add action listeners for buttons
        setButton.addActionListener(e -> {

            // Retrieve input value
            String apiKey = apiKeyField.getText();

            // Perform validation if necessary
            // ...

            // Call internal selection parameters

            setSelectionInfo(new NVGenericMap("remote-ocr").build("api-key", apiKey).build("image-format", imageFormat.getText()));

            // Close the dialog
            dialog.dispose();
        });

        cancelButton.addActionListener(e -> {
            // Reset selection to "No OCR"
//            selectionBox.setSelectedIndex(0);
            dialog.dispose();
        });

        // Assemble the dialog
        dialog.getContentPane().setLayout(new BorderLayout());
        dialog.getContentPane().add(panel, BorderLayout.CENTER);
        dialog.getContentPane().add(buttonsPanel, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setLocationRelativeTo(mainFrame);
        dialog.setVisible(true);
    }

    public void showAIAPIKey() {
        // Create text field
        JTextField apiKeyField = new JTextField(25);
        apiKeyField.setText(getAIAPIKey());


        // Create buttons
        JButton setButton = new JButton("Set");
        JButton cancelButton = new JButton("Cancel");

        // Create the dialog
        JDialog dialog = new JDialog((Frame) null, "AI-API-KEY", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        // Set up the layout
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Add components to the panel
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("AI-API-KEY:"), gbc);

        gbc.gridx = 1;
        panel.add(apiKeyField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;


        // Buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonsPanel.add(setButton);
        buttonsPanel.add(cancelButton);

        // Add action listeners for buttons
        setButton.addActionListener(e ->
        {
            // Retrieve input value
            String apiKey = apiKeyField.getText();
            setAIAPIKey(apiKey);

            // Perform validation if necessary
            // ...

            // Call internal selection parameters

            setSelectionInfo(new NVGenericMap("ai-api-key").build("ai-api-key", apiKey));
            if (aiKeyUpdater != null)
                aiKeyUpdater.accept(apiKey);

            // Close the dialog
            dialog.dispose();

        });

        cancelButton.addActionListener(e -> {
            // Reset selection to "No OCR"
//            selectionBox.setSelectedIndex(0);
            dialog.dispose();
        });

        // Assemble the dialog
        dialog.getContentPane().setLayout(new BorderLayout());
        dialog.getContentPane().add(panel, BorderLayout.CENTER);
        dialog.getContentPane().add(buttonsPanel, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setLocationRelativeTo(mainFrame);
        dialog.setVisible(true);
    }

    // Method representing the internal selection parameters call
    public void setSelectionInfo(NVGenericMap selection) {
        this.selectionInfo = selection;
        // Implement your internal logic here
        System.out.println("Internal selection parameters called.");
    }


    public NVGenericMap getSelectionInfo() {
        return selectionInfo;
    }


    public String getAIAPIKey() {
        return aiAPIKey;
    }

    public void setAIAPIKey(String gptAPIKey) {
        this.aiAPIKey = gptAPIKey;
        aiKeyUpdater.accept(gptAPIKey);
    }
}
