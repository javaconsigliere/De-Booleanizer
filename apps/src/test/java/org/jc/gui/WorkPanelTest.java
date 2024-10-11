package org.jc.gui;


//public class WorkPanelTest {
//}



import javax.swing.*;
import java.awt.*;

public class WorkPanelTest {


    public static JPanel createWorkPanel()
    {
        // Create the panel with GridBagLayout
        JPanel panel = new JPanel(new GridBagLayout());

        // Create GridBagConstraints
        GridBagConstraints gbc = new GridBagConstraints();

        // Labels
        JLabel resultLabel = new JLabel("GPT Result");
        JLabel promptLabel = new JLabel("GPT Prompt");

        // TextAreas
        JTextArea resultTextArea = new JTextArea();
        JTextArea promptTextArea = new JTextArea();

        // Make TextAreas wrap lines and scrollable
        resultTextArea.setLineWrap(true);
        resultTextArea.setWrapStyleWord(true);

        promptTextArea.setLineWrap(true);
        promptTextArea.setWrapStyleWord(true);

        JScrollPane resultScrollPane = new JScrollPane(resultTextArea);
        JScrollPane promptScrollPane = new JScrollPane(promptTextArea);

        // Set preferred sizes for initial appearance
        resultScrollPane.setPreferredSize(new Dimension(250, 150));
        promptScrollPane.setPreferredSize(new Dimension(250, 150));

        // Insets for padding
        Insets padding = new Insets(5, 5, 5, 5);

        // Add labels to the first row
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = padding;
        gbc.weightx = 0.5; // Equal horizontal space for labels
        gbc.weighty = 0;
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(resultLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        panel.add(promptLabel, gbc);

        // Add TextAreas to the second row
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = padding;
        gbc.weightx = 0.5;
        gbc.weighty = 1.0; // Allows vertical expansion
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(resultScrollPane, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        panel.add(promptScrollPane, gbc);

        return panel;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Create the frame
            JFrame frame = new JFrame("GPT Panel Example");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

//            // Create the panel with GridBagLayout
//            JPanel panel = new JPanel(new GridBagLayout());
//
//            // Create GridBagConstraints
//            GridBagConstraints gbc = new GridBagConstraints();
//
//            // Labels
//            JLabel resultLabel = new JLabel("GPT Result");
//            JLabel promptLabel = new JLabel("GPT Prompt");
//
//            // TextAreas
//            JTextArea resultTextArea = new JTextArea();
//            JTextArea promptTextArea = new JTextArea();
//
//            // Make TextAreas wrap lines and scrollable
//            resultTextArea.setLineWrap(true);
//            resultTextArea.setWrapStyleWord(true);
//
//            promptTextArea.setLineWrap(true);
//            promptTextArea.setWrapStyleWord(true);
//
//            JScrollPane resultScrollPane = new JScrollPane(resultTextArea);
//            JScrollPane promptScrollPane = new JScrollPane(promptTextArea);
//
//            // Set preferred sizes for initial appearance
//            resultScrollPane.setPreferredSize(new Dimension(250, 150));
//            promptScrollPane.setPreferredSize(new Dimension(250, 150));
//
//            // Insets for padding
//            Insets padding = new Insets(5, 5, 5, 5);
//
//            // Add labels to the first row
//            gbc.fill = GridBagConstraints.HORIZONTAL;
//            gbc.insets = padding;
//            gbc.weightx = 0.5; // Equal horizontal space for labels
//            gbc.weighty = 0;
//            gbc.gridx = 0;
//            gbc.gridy = 0;
//            panel.add(resultLabel, gbc);
//
//            gbc.gridx = 1;
//            gbc.gridy = 0;
//            panel.add(promptLabel, gbc);
//
//            // Add TextAreas to the second row
//            gbc.fill = GridBagConstraints.BOTH;
//            gbc.insets = padding;
//            gbc.weightx = 0.5;
//            gbc.weighty = 1.0; // Allows vertical expansion
//            gbc.gridx = 0;
//            gbc.gridy = 1;
//            panel.add(resultScrollPane, gbc);
//
//            gbc.gridx = 1;
//            gbc.gridy = 1;
//            panel.add(promptScrollPane, gbc);

            // Add the panel to the frame
            frame.add(createWorkPanel());
            frame.setSize(600, 400);
            frame.setLocationRelativeTo(null); // Center the frame
            frame.setVisible(true);
        });
    }
}
