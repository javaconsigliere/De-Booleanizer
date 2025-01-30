package org.jc.chatgpt;

import org.zoxweb.shared.util.NVGenericMap;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class GPTSelection {

    // Public selection box variable
    public JComboBox<String> selectionBox;
    private final JFrame mainFrame;
    private NVGenericMap selectionInfo = null;
    private final Consumer<String> gptAPIKeyUpdater;



    private String gptAPIKey;

    // Constructor
    public GPTSelection(JFrame mainFrame, Consumer<String> gtpUpdater) {
        this.mainFrame = mainFrame;
        this.gptAPIKeyUpdater = gtpUpdater;
        initializeComponents();
    }

    private void initializeComponents() {
        // Create the selection box with three entries
        String[] options = { "No OCR", "Local OCR", "Remote OCR", "GPT API Key" };
        selectionBox = new JComboBox<>(options);

        // Set default selection to "No OCR"
        selectionBox.setSelectedIndex(0);

        // Add an action listener to handle selection changes
        selectionBox.addActionListener(e->{

                String selectedOption = (String) selectionBox.getSelectedItem();
                if ("Local OCR".equals(selectedOption)) {
                    showLocalOCRDialog();
                } else if ("Remote OCR".equals(selectedOption)) {
                    showRemoteOCRDialog();
                } else if ("GPT API Key".equals(selectedOption)) {
                    showGPTAPIKey();
                }
                else {
                    // No OCR selected, call internal selection parameters
                    setSelectionInfo(null);
                }

        });

        // Add the selection box to the panel
    }

    // Method to display the Local OCR settings dialog
    private void showLocalOCRDialog() {
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
        gbc.insets = new Insets(5,5,5,5);
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
        setButton.addActionListener(e->{

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

        cancelButton.addActionListener(e-> {

                // Reset selection to "No OCR"
                selectionBox.setSelectedIndex(0);
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

    // Method to display the Remote OCR settings dialog
    private void showRemoteOCRDialog() {
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
        gbc.insets = new Insets(5,5,5,5);
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
        setButton.addActionListener(e->{

                // Retrieve input value
                String apiKey = apiKeyField.getText();

                // Perform validation if necessary
                // ...

                // Call internal selection parameters

                setSelectionInfo(new NVGenericMap("remote-ocr").build("api-key", apiKey).build("image-format", imageFormat.getText()));

                // Close the dialog
                dialog.dispose();
        });

        cancelButton.addActionListener(e-> {
                // Reset selection to "No OCR"
                selectionBox.setSelectedIndex(0);
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

    private void showGPTAPIKey() {
        // Create text field
        JTextField apiKeyField = new JTextField(25);
        apiKeyField.setText(getGPTAPIKey());


        // Create buttons
        JButton setButton = new JButton("Set");
        JButton cancelButton = new JButton("Cancel");

        // Create the dialog
        JDialog dialog = new JDialog((Frame) null, "GPT-KEY", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        // Set up the layout
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);
        gbc.anchor = GridBagConstraints.WEST;

        // Add components to the panel
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("API-KEY:"), gbc);

        gbc.gridx = 1;
        panel.add(apiKeyField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;




        // Buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonsPanel.add(setButton);
        buttonsPanel.add(cancelButton);

        // Add action listeners for buttons
        setButton.addActionListener(e->
        {
                // Retrieve input value
                String apiKey = apiKeyField.getText();
                setGPTAPIKey(apiKey);

                // Perform validation if necessary
                // ...

                // Call internal selection parameters

                setSelectionInfo(new NVGenericMap("gpt-api-key").build("gpt-key", apiKey));
                if (gptAPIKeyUpdater != null)
                    gptAPIKeyUpdater.accept(apiKey);

                // Close the dialog
                dialog.dispose();

        });

        cancelButton.addActionListener(e->{
                // Reset selection to "No OCR"
                selectionBox.setSelectedIndex(0);
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
    private void setSelectionInfo(NVGenericMap selection) {
        this.selectionInfo = selection;
        // Implement your internal logic here
        System.out.println("Internal selection parameters called.");
    }


    public NVGenericMap getSelectionInfo()
    {
        return selectionInfo;
    }

    // Main method for testing purposes
    public static void main(String[] args) {
        // Create a JFrame to test the OCRSelectionPanel
        JFrame frame = new JFrame("OCR Selection Test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(200, 75);

        // Create an instance of OCRSelectionPanel
        GPTSelection ocrSelectionPanel = new GPTSelection(frame, null);

        // Add the panel to the frame
        frame.getContentPane().add(ocrSelectionPanel.selectionBox);

        // Display the frame
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public String getGPTAPIKey() {
        return gptAPIKey;
    }

    public void setGPTAPIKey(String gptAPIKey) {
        this.gptAPIKey = gptAPIKey;
        gptAPIKeyUpdater.accept(gptAPIKey);
    }
}
