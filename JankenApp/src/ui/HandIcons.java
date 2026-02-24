package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class HandIcons {

    public static ImageIcon loadIcon(String path) {
        ImageIcon icon = new ImageIcon(path);
        if (icon.getIconWidth() <= 0) {
            BufferedImage img = new BufferedImage(120, 120, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = img.createGraphics();
            g.setColor(Color.LIGHT_GRAY);
            g.fillRect(0, 0, 120, 120);
            g.setColor(Color.BLACK);
            g.drawString("NO IMAGE", 20, 60);
            g.dispose();
            return new ImageIcon(img);
        }
        return icon;
    }

    public static ImageIcon[] defaultIcons() {
        return new ImageIcon[] {
                loadIcon("img/gu.png"),
                loadIcon("img/choki.png"),
                loadIcon("img/pa.png")
        };
    }
}