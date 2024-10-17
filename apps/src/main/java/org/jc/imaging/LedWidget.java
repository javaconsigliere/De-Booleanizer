package org.jc.imaging;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;

/**
 * LedWidget is a custom Swing component that displays a filled circle (LED)
 * which can be toggled between "on" and "off" states with customizable colors.
 */
public class LedWidget extends JPanel {
    private Color onColor;      // Color when LED is on
    private Color offColor;     // Color when LED is off
    private boolean isOn;       // Current state of the LED

    /**
     * Constructs a LedWidget with specified dimensions and colors.
     *
     * @param width    The preferred width of the widget.
     * @param height   The preferred height of the widget.
     * @param onColor  The color of the LED when it is on.
     * @param offColor The color of the LED when it is off.
     */
    public LedWidget(int width, int height, Color onColor, Color offColor) {
        this.onColor = onColor;
        this.offColor = offColor;
        this.isOn = false; // Default state is off

        // Set preferred size
        this.setPreferredSize(new Dimension(width, height));

        // Optional: Add mouse listener to toggle state on click
        toggleState();
    }

    /**
     * Toggles the LED state between on and off.
     */
    public void toggleState() {
        this.isOn = !this.isOn;
        repaint(); // Repaint the widget to reflect the state change
    }

    /**
     * Sets the LED state.
     *
     * @param isOn true to turn the LED on, false to turn it off.
     */
    public void setState(boolean isOn) {
        this.isOn = isOn;
        repaint();
    }

    /**
     * Retrieves the current state of the LED.
     *
     * @return true if LED is on, false otherwise.
     */
    public boolean getState() {
        return this.isOn;
    }

    /**
     * Sets the color of the LED when it is on.
     *
     * @param onColor The desired "on" color.
     */
    public void setOnColor(Color onColor) {
        this.onColor = onColor;
        repaint();
    }

    /**
     * Sets the color of the LED when it is off.
     *
     * @param offColor The desired "off" color.
     */
    public void setOffColor(Color offColor) {
        this.offColor = offColor;
        repaint();
    }

    /**
     * Overrides the paintComponent method to draw the LED.
     *
     * @param g The Graphics object used for drawing.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawLED(g);
    }

    /**
     * Draws the LED as a filled circle with the appropriate color based on its state.
     *
     * @param g The Graphics object used for drawing.
     */
    private void drawLED(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();

        // Enable anti-aliasing for smooth edges
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Determine the color based on the state
        Color currentColor = isOn ? onColor : offColor;

        // Calculate the largest possible circle within the widget dimensions, considering padding
        int padding = 2;
        int diameter = Math.min(getWidth(), getHeight()) - 2 * padding;
        int x = (getWidth() - diameter) / 2;
        int y = (getHeight() - diameter) / 2;

        if (isOn) {
            // Create a radial gradient for the shining effect
            float radius = diameter / 2f;
            float centerX = x + radius;
            float centerY = y + radius;
            float[] dist = {0.0f, 1.0f};
            Color[] colors = {currentColor, new Color(0, 0, 0, 0)};
            RadialGradientPaint gradient = new RadialGradientPaint(new Point2D.Float(centerX, centerY), radius, dist, colors);
            g2d.setPaint(gradient);
            g2d.fillOval(x, y, diameter, diameter);
        } else {
            // Fill with the off color
            g2d.setColor(currentColor);
            g2d.fillOval(x, y, diameter, diameter);
        }

        // Draw a border around the LED for better visibility
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawOval(x, y, diameter, diameter);

        g2d.dispose();
    }
}
