package org.jc.gui;

import org.jc.imaging.LedWidget;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class TrafficLightTest {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Create the main application frame
            JFrame frame = new JFrame("Traffic Light Demo");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(400, 200);
            frame.setLayout(new BorderLayout());

            // Create a panel to hold the LEDs horizontally
            JPanel ledPanel = new JPanel();
            ledPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 50, 20)); // Centered with horizontal gap

            // Create red LED widget
            LedWidget redLed = new LedWidget(30, 30, Color.RED, new Color(150, 0, 0));
            redLed.setToolTipText("Red Light: Click to toggle");

            // Create green LED widget
            LedWidget greenLed = new LedWidget(50, 50, Color.GREEN, Color.RED);
            greenLed.setToolTipText("Green Light: Click to toggle");

            redLed.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    redLed.setState(!redLed.getState());
                }});

            greenLed.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    greenLed.setState(!greenLed.getState());
                }});



            // Add LEDs to the panel
            ledPanel.add(redLed);
            ledPanel.add(greenLed);

            JButton button = new JButton("Toggle");
            ledPanel.add(button);
            button.addActionListener(e ->{greenLed.toggleState();redLed.toggleState();});

            // Optional: Add instructions at the bottom
            JLabel instructions = new JLabel("Click on the LEDs to toggle their states.");
            instructions.setHorizontalAlignment(SwingConstants.CENTER);
            instructions.setFont(new Font("Arial", Font.PLAIN, 14));
            instructions.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

            // Add components to the frame
            frame.add(ledPanel, BorderLayout.CENTER);
            frame.add(instructions, BorderLayout.SOUTH);

            // Center the frame on the screen
            frame.setLocationRelativeTo(null);

            // Make the frame visible
            frame.setVisible(true);
        });
    }
}
