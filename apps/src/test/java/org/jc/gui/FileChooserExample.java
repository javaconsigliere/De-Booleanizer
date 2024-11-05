package org.jc.gui;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class FileChooserExample {
    public static void main(String[] args) {
        // Ensure the GUI is created on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            // Create a JFrame (invisible, used as parent for JFileChooser)
            JFrame frame = new JFrame();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(false); // We don't need to display the frame

            // Create a JFileChooser instance
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

            // Show the Open dialog
            int result = fileChooser.showOpenDialog(frame);

            if (result == JFileChooser.APPROVE_OPTION) {
                // User selected a file
                java.io.File selectedFile = fileChooser.getSelectedFile();
                System.out.println("Selected file: " + selectedFile.getAbsolutePath());
            } else {
                System.out.println("Open command canceled by user.");
            }

            // Dispose the frame after operation
            frame.dispose();
        });
    }
}

