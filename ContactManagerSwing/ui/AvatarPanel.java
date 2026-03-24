package ui;

import javax.swing.*;
import java.awt.*;

/**
 * AvatarPanel - Circular avatar with gradient fill and initials.
 *
 * Custom JPanel rendering a gradient circle with a subtle outer ring
 * and centered bold initials. Uses the current accent color.
 */
public class AvatarPanel extends JPanel {

    private String initials = "?";
    private int diameter;

    public AvatarPanel(int diameter) {
        this.diameter = diameter;
        Dimension size = new Dimension(diameter + 6, diameter + 6);
        setPreferredSize(size);
        setMinimumSize(size);
        setMaximumSize(size);
        setOpaque(false);
    }

    /**
     * Set the name to derive initials from.
     */
    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            this.initials = "?";
        } else {
            String[] parts = name.trim().split("\\s+");
            if (parts.length >= 2) {
                this.initials = ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase();
            } else {
                this.initials = ("" + parts[0].charAt(0)).toUpperCase();
            }
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        Color accent = UITheme.getAccentColor();
        int x = (getWidth() - diameter) / 2;
        int y = (getHeight() - diameter) / 2;

        // Outer subtle ring / shadow
        g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 55));
        g2.fillOval(x - 3, y - 3, diameter + 6, diameter + 6);

        // Gradient fill
        GradientPaint gp = new GradientPaint(
            x, y, accent.brighter(),
            x, y + diameter, accent.darker());
        g2.setPaint(gp);
        g2.fillOval(x, y, diameter, diameter);

        // Top shine
        g2.setColor(new Color(255, 255, 255, 55));
        g2.fillOval(x + diameter / 4, y + 4, diameter / 2, diameter / 4);

        // Initials
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Segoe UI", Font.BOLD, diameter / 3));
        FontMetrics fm = g2.getFontMetrics();
        int textX = x + (diameter - fm.stringWidth(initials)) / 2;
        int textY = y + (diameter - fm.getHeight()) / 2 + fm.getAscent();
        g2.drawString(initials, textX, textY);

        g2.dispose();
    }
}
