package ui;

import javax.swing.*;
import java.awt.*;
import java.io.InputStream;

/**
 * IconFactory - Safe icon loading with fallback
 * 
 * Loads icons from resources/icons/ folder.
 * Falls back to Unicode symbols if icons not found.
 */
public class IconFactory {
    
    /**
     * Get the icon color based on current theme.
     * Dark theme -> white icons, Light theme -> dark icons.
     */
    private static Color getIconColor() {
        return UITheme.isDarkMode() ? new Color(220, 220, 220) : new Color(50, 50, 50);
    }
    
    /**
     * Load icon from resources or return fallback.
     * Tries multiple classpath locations for IDE and JAR compatibility.
     * Scales loaded icons to 18x18 with smooth scaling.
     */
    public static ImageIcon loadIcon(String iconName, String fallbackText) {
        // Try multiple resource paths for IDE and JAR compatibility
        String[] paths = {
            "/resources/icons/" + iconName,
            "/icons/" + iconName,
            "icons/" + iconName,
            iconName
        };
        
        for (String path : paths) {
            try {
                // Try getResource (works in more environments than getResourceAsStream)
                java.net.URL url = IconFactory.class.getResource(path);
                if (url != null) {
                    ImageIcon raw = new ImageIcon(url);
                    if (raw.getIconWidth() > 0) {
                        // Scale to 18x18 for crisp toolbar icons
                        Image scaled = raw.getImage().getScaledInstance(18, 18, Image.SCALE_SMOOTH);
                        return new ImageIcon(scaled);
                    }
                }
            } catch (Exception ignored) { }
            
            try {
                InputStream is = IconFactory.class.getResourceAsStream(path);
                if (is != null) {
                    byte[] bytes = is.readAllBytes();
                    is.close();
                    if (bytes.length > 0) {
                        ImageIcon raw = new ImageIcon(bytes);
                        Image scaled = raw.getImage().getScaledInstance(18, 18, Image.SCALE_SMOOTH);
                        return new ImageIcon(scaled);
                    }
                }
            } catch (Exception ignored) { }
        }
        
        // Fallback: Create theme-aware text-based icon
        return createTextIcon(fallbackText, 20, 20);
    }
    
    /**
     * Create a fallback icon using simple geometric shapes.
     * More reliable than emoji which can render as boxes.
     * Uses larger, clearer geometric shapes that are always visible.
     */
    private static ImageIcon createTextIcon(String text, int width, int height) {
        java.awt.image.BufferedImage image = new java.awt.image.BufferedImage(
            width, height, java.awt.image.BufferedImage.TYPE_INT_ARGB
        );
        Graphics2D g2d = image.createGraphics();
        
        // Enable anti-aliasing for crisp rendering
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        
        Color iconColor = getIconColor();
        
        // Always use geometric fallback for reliability (no emoji dependency)
        drawGeometricFallback(g2d, text, width, height, iconColor);
        
        g2d.dispose();
        return new ImageIcon(image);
    }
    
    /**
     * Draw simple geometric shapes as ultimate fallback.
     * Never renders as boxes - always draws something visible.
     * Updated with clearer, bolder shapes for better visibility.
     */
    private static void drawGeometricFallback(Graphics2D g2d, String symbol, int w, int h, Color color) {
        g2d.setColor(color);
        g2d.setStroke(new BasicStroke(2.5f));
        int cx = w / 2;
        int cy = h / 2;
        
        // Map specific symbols to clear geometric shapes
        switch (symbol) {
            case "➕": // Add
                g2d.drawLine(cx, 3, cx, h - 3);
                g2d.drawLine(3, cy, w - 3, cy);
                break;
                
            case "✏": // Edit
                // Draw pencil shape
                g2d.fillPolygon(new int[]{w - 3, w - 3, cx + 2}, new int[]{3, h - 5, h - 5}, 3);
                g2d.drawLine(cx + 2, h - 5, 3, h - 3);
                break;
                
            case "🗑": // Delete
                g2d.drawRect(4, 8, w - 8, h - 10);
                g2d.drawLine(3, 7, w - 3, 7);
                g2d.drawLine(cx - 2, 4, cx + 2, 4);
                g2d.drawLine(7, 11, 7, h - 5);
                g2d.drawLine(cx, 11, cx, h - 5);
                g2d.drawLine(w - 7, 11, w - 7, h - 5);
                break;
                
            case "♻": // Recycle
                g2d.setStroke(new BasicStroke(2f));
                // Draw circular arrows
                g2d.drawArc(3, 3, w - 6, h - 6, 30, 120);
                g2d.drawArc(3, 3, w - 6, h - 6, 150, 120);
                g2d.drawArc(3, 3, w - 6, h - 6, 270, 120);
                // Add arrow heads
                g2d.fillPolygon(new int[]{w - 4, w - 7, w - 5}, new int[]{cy - 1, cy - 3, cy + 2}, 3);
                break;
                
            case "📥": // Import
                g2d.drawRect(4, 4, w - 8, 5);
                g2d.drawLine(cx, 7, cx, h - 4);
                g2d.fillPolygon(new int[]{cx, cx - 4, cx + 4}, new int[]{h - 3, h - 8, h - 8}, 3);
                break;
                
            case "📤": // Export
                g2d.drawRect(4, h - 9, w - 8, 5);
                g2d.drawLine(cx, 4, cx, h - 7);
                g2d.fillPolygon(new int[]{cx, cx - 4, cx + 4}, new int[]{3, 8, 8}, 3);
                break;
                
            case "🔄": // Refresh
                g2d.setStroke(new BasicStroke(2f));
                g2d.drawArc(4, 4, w - 8, h - 8, 90, 280);
                g2d.fillPolygon(new int[]{w - 4, w - 9, w - 4}, new int[]{cy, cy - 3, cy + 3}, 3);
                break;
                
            case "🌙": // Theme
                g2d.fillOval(4, 4, w - 8, h - 8);
                g2d.setColor(UITheme.isDarkMode() ? new Color(50, 50, 50) : new Color(245, 245, 245));
                g2d.fillOval(8, 4, w - 11, h - 8);
                break;
                
            case "📋": // Batch/Clipboard
                // Clipboard with checkboxes
                g2d.drawRoundRect(4, 3, w - 8, h - 6, 2, 2);
                g2d.setStroke(new BasicStroke(1.5f));
                // Draw checkbox list
                g2d.drawRect(7, 7, 3, 3);
                g2d.drawRect(7, 11, 3, 3);
                g2d.drawLine(11, 8, w - 7, 8);
                g2d.drawLine(11, 12, w - 7, 12);
                break;
                
            default:
                // Generic fallback - draw a simple rect
                g2d.drawRoundRect(5, 5, w - 10, h - 10, 3, 3);
                break;
        }
    }
    
    /**
     * Pre-defined icons for toolbar
     */
    public static ImageIcon getAddIcon() {
        return loadIcon("add.png", "➕");
    }
    
    public static ImageIcon getEditIcon() {
        return loadIcon("edit.png", "✏");
    }
    
    public static ImageIcon getDeleteIcon() {
        return loadIcon("delete.png", "🗑");
    }
    
    public static ImageIcon getRecycleIcon() {
        return loadIcon("recycle.png", "♻");
    }
    
    public static ImageIcon getImportIcon() {
        return loadIcon("import.png", "📥");
    }
    
    public static ImageIcon getExportIcon() {
        return loadIcon("export.png", "📤");
    }
    
    public static ImageIcon getRefreshIcon() {
        return loadIcon("refresh.png", "🔄");
    }
    
    public static ImageIcon getThemeIcon() {
        return loadIcon("theme.png", "🌙");
    }
    
    public static ImageIcon getBatchIcon() {
        return loadIcon("batch.png", "📋");
    }
}
