package ui;

import javax.swing.*;
import java.awt.*;

/**
 * EmojiLabel - JLabel with proper emoji font fallback support.
 * 
 * Ensures emoji display correctly across different systems.
 * Falls back to simple text symbols if emoji fonts aren't available.
 */
public class EmojiLabel extends JLabel {
    
    private static final Font[] EMOJI_FONTS = {
        new Font("Segoe UI Emoji", Font.PLAIN, 12),
        new Font("Apple Color Emoji", Font.PLAIN, 12),
        new Font("Noto Color Emoji", Font.PLAIN, 12),
        new Font("Segoe UI Symbol", Font.PLAIN, 12)
    };
    
    public EmojiLabel(String text) {
        super(text);
        setFontWithFallback(text);
    }
    
    /**
     * Try multiple emoji fonts and fall back to regular font if needed.
     */
    private void setFontWithFallback(String text) {
        // Try to find a font that can display the emoji
        for (Font font : EMOJI_FONTS) {
            if (font.canDisplayUpTo(text) == -1) {
                setFont(font);
                return;
            }
        }
        
        // Fallback to regular font
        setFont(new Font("Segoe UI", Font.PLAIN, 12));
    }
    
    /**
     * Create a label with emoji and text, ensuring proper display.
     */
    public static JLabel create(String emoji, String text) {
        String combined = emoji + " " + text;
        
        // Try emoji fonts first
        for (Font font : EMOJI_FONTS) {
            if (font.canDisplayUpTo(combined) == -1) {
                JLabel label = new JLabel(combined);
                label.setFont(font);
                return label;
            }
        }
        
        // Fallback: use simple text prefix instead of emoji
        String fallback = getFallbackText(emoji) + " " + text;
        JLabel label = new JLabel(fallback);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        return label;
    }
    
    /**
     * Map emoji to simple text fallback.
     */
    private static String getFallbackText(String emoji) {
        switch (emoji) {
            case "🔍": return "[S]";  // Search
            case "🏷️": return "[C]";  // Category
            case "📞": return "☎";     // Phone (works in more fonts)
            case "📧": return "✉";     // Email (works in more fonts)
            case "📅": return "◷";     // Created/Calendar
            case "🔄": return "⟲";     // Updated/Refresh
            case "📋": return "▢";     // Clipboard
            default: return "•";
        }
    }
}
