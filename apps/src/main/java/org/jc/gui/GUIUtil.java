package org.jc.gui;

import javax.swing.*;
import java.awt.*;

public class GUIUtil {
    private GUIUtil(){}

    public static JPanel createPanel(String title, LayoutManager layout, JComponent ...components)
    {
        JPanel panel = new JPanel(layout);
        for (JComponent component: components)
            panel.add(component);

        // Optionally, set a border with the panel title
        panel.setBorder(BorderFactory.createTitledBorder(title));

        return panel;
    }
}
