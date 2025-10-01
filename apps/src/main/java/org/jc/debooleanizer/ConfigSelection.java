package org.jc.debooleanizer;

import io.xlogistx.gui.GUIUtil;
import io.xlogistx.gui.NVGenericMapWidget;
import org.zoxweb.server.logging.LogWrapper;
import org.zoxweb.shared.util.GetNameValue;
import org.zoxweb.shared.util.NVGenericMap;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class ConfigSelection {


    public static final LogWrapper log = new LogWrapper(ConfigSelection.class).setEnabled(true);
    // Public selection box variable
    private final JFrame mainFrame;
    //private NVGenericMap selectionInfo = null;
    private final Consumer<NVGenericMap> aiAPIUpdater;
    private final NVGenericMap data;


    private String aiAPIKey;

    // Constructor
    public ConfigSelection(JFrame mainFrame, NVGenericMap data, Consumer<NVGenericMap> apiUpdater) {
        this.mainFrame = mainFrame;
        this.aiAPIUpdater = apiUpdater;

        this.data = data;
    }


    // Method to display the Local OCR settings dialog
//    public void showLocalOCRDialog() {
//        // Create text fields
//        JTextField pathField = new JTextField(20);
//        JTextField languageField = new JTextField(10);
//
//        // Create buttons
//        JButton setButton = new JButton("Set");
//        JButton cancelButton = new JButton("Cancel");
//
//        // Create the dialog
//        JDialog dialog = new JDialog((Frame) null, "Local OCR Settings", true);
//        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
//
//        // Set up the layout
//        JPanel panel = new JPanel(new GridBagLayout());
//        GridBagConstraints gbc = new GridBagConstraints();
//        gbc.insets = new Insets(5, 5, 5, 5);
//        gbc.anchor = GridBagConstraints.WEST;
//
//        // Add components to the panel
//        gbc.gridx = 0;
//        gbc.gridy = 0;
//        panel.add(new JLabel("Path:"), gbc);
//
//        gbc.gridx = 1;
//        panel.add(pathField, gbc);
//
//        gbc.gridx = 0;
//        gbc.gridy = 1;
//        panel.add(new JLabel("Language:"), gbc);
//
//        gbc.gridx = 1;
//        panel.add(languageField, gbc);
//
//        // Buttons panel
//        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
//        buttonsPanel.add(setButton);
//        buttonsPanel.add(cancelButton);
//
//        // Add action listeners for buttons
//        setButton.addActionListener(e -> {
//
//            // Retrieve input values
//            String path = pathField.getText();
//            String language = languageField.getText();
//
//            // Perform validation if necessary
//            // ...
//
//            // Call internal selection parameters
////            setSelectionInfo(new NVGenericMap("local-ocr").build("path", path).build("language", language));
//
//            // Close the dialog
//            dialog.dispose();
//
//        });
//
//        cancelButton.addActionListener(e -> {
//
//            // Reset selection to "No OCR"
////            selectionBox.setSelectedIndex(0);
//            dialog.dispose();
//
//        });
//
//        // Assemble the dialog
//        dialog.getContentPane().setLayout(new BorderLayout());
//        dialog.getContentPane().add(panel, BorderLayout.CENTER);
//        dialog.getContentPane().add(buttonsPanel, BorderLayout.SOUTH);
//        dialog.pack();
////        dialog.setLocationRelativeTo(mainFrame);
//        dialog.setVisible(true);
//    }
//
//    // Method to display the Remote OCR settings dialog
//    public void showRemoteOCRDialog() {
//        // Create text field
//        JTextField apiKeyField = new JTextField(25);
//        JTextField imageFormat = new JTextField(15);
//
//        // Create buttons
//        JButton setButton = new JButton("Set");
//        JButton cancelButton = new JButton("Cancel");
//
//        // Create the dialog
//        JDialog dialog = new JDialog((Frame) null, "Remote OCR Settings", true);
//        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
//
//        // Set up the layout
//        JPanel panel = new JPanel(new GridBagLayout());
//        GridBagConstraints gbc = new GridBagConstraints();
//        gbc.insets = new Insets(5, 5, 5, 5);
//        gbc.anchor = GridBagConstraints.WEST;
//
//        // Add components to the panel
//        gbc.gridx = 0;
//        gbc.gridy = 0;
//        panel.add(new JLabel("API-KEY:"), gbc);
//
//        gbc.gridx = 1;
//        panel.add(apiKeyField, gbc);
//
//        gbc.gridx = 0;
//        gbc.gridy = 1;
//        panel.add(new JLabel("IMAGE-FORMAT:"), gbc);
//
//        gbc.gridx = 1;
//        panel.add(imageFormat, gbc);
//
//
//        // Buttons panel
//        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
//        buttonsPanel.add(setButton);
//        buttonsPanel.add(cancelButton);
//
//        // Add action listeners for buttons
//        setButton.addActionListener(e -> {
//
//            // Retrieve input value
//            String apiKey = apiKeyField.getText();
//
//            // Perform validation if necessary
//            // ...
//
//            // Call internal selection parameters
//
//            //setSelectionInfo(new NVGenericMap("remote-ocr").build("api-key", apiKey).build("image-format", imageFormat.getText()));
//
//            // Close the dialog
//            dialog.dispose();
//        });
//
//        cancelButton.addActionListener(e -> {
//            // Reset selection to "No OCR"
////            selectionBox.setSelectedIndex(0);
//            dialog.dispose();
//        });
//
//        // Assemble the dialog
//        dialog.getContentPane().setLayout(new BorderLayout());
//        dialog.getContentPane().add(panel, BorderLayout.CENTER);
//        dialog.getContentPane().add(buttonsPanel, BorderLayout.SOUTH);
//        dialog.pack();
//        dialog.setLocationRelativeTo(mainFrame);
//        dialog.setVisible(true);
//    }

//    public void showAIAPIKey() {
//        // Create text field
//        JTextField apiKeyField = new JTextField(25);
//
//        apiKeyField.setText(data.getValue("ai-api-key"));
//
//
//        // Create buttons
//        JButton setButton = new JButton("Set");
//        JButton cancelButton = new JButton("Cancel");
//
//        // Create the dialog
//        JDialog dialog = new JDialog((Frame) null, "AI-API-KEY", true);
//        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
//
//        // Set up the layout
//        JPanel panel = new JPanel(new GridBagLayout());
//        GridBagConstraints gbc = new GridBagConstraints();
//        gbc.insets = new Insets(5, 5, 5, 5);
//        gbc.anchor = GridBagConstraints.WEST;
//
//        // Add components to the panel
//        gbc.gridx = 0;
//        gbc.gridy = 0;
//        panel.add(new JLabel("AI-API-KEY:"), gbc);
//
//        gbc.gridx = 1;
//        panel.add(apiKeyField, gbc);
//
//        gbc.gridx = 0;
//        gbc.gridy = 1;
//
//
//        // Buttons panel
//        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
//        buttonsPanel.add(setButton);
//        buttonsPanel.add(cancelButton);
//
//        // Add action listeners for buttons
//        setButton.addActionListener(e ->
//        {
//            // Retrieve input value
//            String apiKey = apiKeyField.getText();
//
//            // Perform validation if necessary
//            // ...
//
//            // Call internal selection parameters
//
//            //setSelectionInfo(new NVGenericMap("ai-api-key").build("ai-api-key", apiKey));
//
//            if (aiAPIUpdater != null)
//                aiAPIUpdater.accept(GetNameValue.create("ai-api-key", apiKey));
//
//            // Close the dialog
//            dialog.dispose();
//
//        });
//
//        cancelButton.addActionListener(e -> {
//            // Reset selection to "No OCR"
////            selectionBox.setSelectedIndex(0);
//            dialog.dispose();
//        });
//
//        // Assemble the dialog
//        dialog.getContentPane().setLayout(new BorderLayout());
//        dialog.getContentPane().add(panel, BorderLayout.CENTER);
//        dialog.getContentPane().add(buttonsPanel, BorderLayout.SOUTH);
//        dialog.pack();
//        dialog.setLocationRelativeTo(mainFrame);
//        dialog.setVisible(true);
//    }

//    public void showAIAPIConfigOld() {
//        // Create text field
//        JTextField apiURLField = new JTextField(25);
//        apiURLField.setText(data.getValue("ai-api-url"));
//
//        JTextField apiKeyField = new JTextField(25);
//        apiKeyField.setText(data.getValue("ai-api-key"));
//
//
//        // Create buttons
//        JButton setButton = GUIUtil.iconButton(new GUIUtil.SaveIcon(32));//new JButton("Set");
//        JButton cancelButton = GUIUtil.iconButton(new GUIUtil.CancelIcon(32));;
//
//        // Create the dialog
//        JDialog dialog = new JDialog((Frame) null, "AI-API", true);
//        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
//
//        // Set up the layout
//        JPanel panel = new JPanel(new GridBagLayout());
//        GridBagConstraints gbc = new GridBagConstraints();
//        gbc.insets = new Insets(5, 5, 5, 5);
//        gbc.anchor = GridBagConstraints.WEST;
//
//        // Add components to the panel
//        gbc.gridx = 0;
//        gbc.gridy = 0;
//        panel.add(new JLabel("AI-API-URL:"), gbc);
//
//        gbc.gridx = 1;
//        panel.add(apiURLField, gbc);
//
//        gbc.gridx = 0;
//        gbc.gridy = 1;
//        panel.add(new JLabel("AI-API-KEY:"), gbc);
//        gbc.gridx = 1;
//        panel.add(apiKeyField, gbc);
//
//        gbc.gridx = 0;
//        gbc.gridy = 2;
//
//
//        // Buttons panel
//        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
//        buttonsPanel.add(setButton);
//        buttonsPanel.add(cancelButton);
//
//        // Add action listeners for buttons
//        setButton.addActionListener(e ->
//        {
//            // Retrieve input value
//            String apiURL = apiURLField.getText();
//            String apiKey = apiKeyField.getText();
//
//
//            // Perform validation if necessary
//            // ...
//
//            // Call internal selection parameters
//
////            setSelectionInfo(new NVGenericMap("ai-api-url").build("ai-api-url", apiURL));
//            if (aiAPIUpdater != null) {
//                GetNameValue<String>[] configVals = new GetNameValue[]{
//
//                        GetNameValue.create("ai-api-url", apiURL),
//                        GetNameValue.create("ai-api-key", apiKey)
//
//                };
//                aiAPIUpdater.accept(data);
//            }
//
//            // Close the dialog
//            dialog.dispose();
//
//        });
//
//        cancelButton.addActionListener(e -> {
//            // Reset selection to "No OCR"
////            selectionBox.setSelectedIndex(0);
//            dialog.dispose();
//        });
//
//        // Assemble the dialog
//        dialog.getContentPane().setLayout(new BorderLayout());
//        dialog.getContentPane().add(panel, BorderLayout.CENTER);
//        dialog.getContentPane().add(buttonsPanel, BorderLayout.SOUTH);
//        dialog.pack();
//        dialog.setLocationRelativeTo(mainFrame);
//        dialog.setVisible(true);
//    }



    public void showAIAPIConfig() {
        // Create text field
        NVGenericMapWidget nvgmw = new NVGenericMapWidget(data);
        log.getLogger().info("**************** aiAPIUpdater: " + aiAPIUpdater);

        nvgmw.setUpdateConsumer(aiAPIUpdater);

        JDialog dialog = new JDialog((Frame) null, data.getName(), true);
        // Add action listeners for buttons
//        nvgmw.getSave().addActionListener(e ->
//        {
//
//
//
//            // Perform validation if necessary
//            // ...
//
//            // Call internal selection parameters
//
////            setSelectionInfo(new NVGenericMap("ai-api-url").build("ai-api-url", apiURL));
//            if (aiAPIUpdater != null) {
//                GetNameValue<String>[] configVals = new GetNameValue[]{
//                        data.getNV("ai-api-url"),
//                        data.getNV("ai-api-key"),
//
//
//                };
//                aiAPIUpdater.accept(data);
//            }
//
//            // Close the dialog
//            dialog.dispose();
//
//        });
        nvgmw.getCancel().addActionListener(e -> {
            dialog.dispose();
        });

        // Assemble the dialog
        dialog.getContentPane().setLayout(new BorderLayout());
        dialog.getContentPane().add(nvgmw);
        dialog.pack();
        dialog.setLocationRelativeTo(mainFrame);
        dialog.setVisible(true);

    }

    // Method representing the internal selection parameters call
//    public void setSelectionInfo(NVGenericMap selection) {
//        this.selectionInfo = selection;
//        // Implement your internal logic here
//        System.out.println("Internal selection parameters called.");
//    }


//    public NVGenericMap getSelectionInfo() {
//        return selectionInfo;
//    }
//
//
//    public String getAIAPIKey() {
//        return aiAPIKey;
//    }
//
//    public void setAIAPIKey(String aiAPIKey) {
//        this.aiAPIKey = aiAPIKey;
//        aiAPIUpdater.accept(GetNameValue.create("ai-api-key", aiAPIKey));
//    }
}
