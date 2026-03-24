package ui;

import javax.swing.*;
import java.awt.*;

/**
 * IconRenderer - Creates small vector-based icons that never render as boxes.
 * 
 * Draws simple geometric shapes instead of relying on emoji fonts.
 * Guaranteed to work on all systems.
 */
public class IconRenderer {
    
    /**
     * Create a small icon (14x14) for labels and buttons.
     * Uses simple vector graphics that always display correctly.
     */
    public static ImageIcon createSmallIcon(IconType type) {
        int size = 14;
        java.awt.image.BufferedImage image = new java.awt.image.BufferedImage(
            size, size, java.awt.image.BufferedImage.TYPE_INT_ARGB
        );
        Graphics2D g2d = image.createGraphics();
        
        // Anti-aliasing for smooth rendering
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        
        // Use theme-aware color
        Color iconColor = UITheme.isDarkMode() ? new Color(160, 160, 160) : new Color(90, 90, 90);
        g2d.setColor(iconColor);
        g2d.setStroke(new BasicStroke(1.5f));
        
        drawIcon(g2d, type, size);
        
        g2d.dispose();
        return new ImageIcon(image);
    }
    
    /**
     * Draw the specific icon shape.
     */
    private static void drawIcon(Graphics2D g2d, IconType type, int size) {
        int cx = size / 2;
        int cy = size / 2;
        
        switch (type) {
            case SEARCH:
                // Magnifying glass
                g2d.drawOval(2, 2, 8, 8);
                g2d.drawLine(9, 9, 12, 12);
                break;
                
            case CATEGORY:
                // Tag shape
                g2d.drawLine(2, 2, 12, 2);
                g2d.drawLine(12, 2, 12, 6);
                g2d.drawLine(12, 6, 7, 11);
                g2d.drawLine(7, 11, 2, 6);
                g2d.drawLine(2, 6, 2, 2);
                g2d.fillOval(8, 4, 2, 2);
                break;
                
            case PHONE:
                // Phone handset
                g2d.drawArc(3, 3, 4, 4, 45, 180);
                g2d.drawArc(7, 7, 4, 4, -135, 180);
                g2d.drawLine(5, 5, 8, 8);
                break;
                
            case EMAIL:
                // Envelope
                g2d.drawRect(1, 4, 12, 7);
                g2d.drawLine(1, 4, 7, 8);
                g2d.drawLine(13, 4, 7, 8);
                break;
                
            case CALENDAR:
                // Calendar
                g2d.drawRect(2, 3, 10, 9);
                g2d.drawLine(2, 6, 12, 6);
                g2d.drawLine(4, 2, 4, 4);
                g2d.drawLine(10, 2, 10, 4);
                break;
                
            case REFRESH:
                // Circular arrow
                g2d.drawArc(2, 2, 10, 10, 90, 270);
                int[] xPoints = {12, 9, 12};
                int[] yPoints = {7, 7, 10};
                g2d.fillPolygon(xPoints, yPoints, 3);
                break;
                
            case CLIPBOARD:
                // Clipboard
                g2d.drawRect(3, 2, 8, 10);
                g2d.drawRect(5, 1, 4, 2);
                g2d.drawLine(5, 5, 9, 5);
                g2d.drawLine(5, 7, 9, 7);
                g2d.drawLine(5, 9, 9, 9);
                break;
                
            case STATS:
                // Bar chart
                g2d.fillRect(2, 8, 2, 4);
                g2d.fillRect(6, 5, 2, 7);
                g2d.fillRect(10, 3, 2, 9);
                break;
                
            default:
                // Generic bullet point
                g2d.fillOval(5, 5, 4, 4);
                break;
        }
    }
    
    /**
     * Icon types available.
     */
    public enum IconType {
        SEARCH,
        CATEGORY,
        PHONE,
        EMAIL,
        CALENDAR,
        REFRESH,
        CLIPBOARD,
        STATS
    }
}
