package org.jc.gui;

import javax.swing.*;

public class LayoutExample {
    public static void main(String[] args) {
        // Create the frame
        JFrame frame = new JFrame("Vertical Layout Example");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Create a panel with BoxLayout (Y_AXIS)
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // Create components
        JButton button1 = new JButton("Button 1");
        JLabel label1 = new JLabel("Label 1");
        JTextField textField1 = new JTextField("Text Field 1");
        JCheckBox checkBox1 = new JCheckBox("Check Box 1");

        // Add components to the panel
        panel.add(button1);
        panel.add(label1);
        panel.add(textField1);
        panel.add(checkBox1);

        // Add panel to the frame
        frame.add(panel);

        // Set frame size and make it visible
        frame.setSize(300, 200);
        frame.setLocationRelativeTo(null); // Center the frame
        frame.setVisible(true);
    }
}
