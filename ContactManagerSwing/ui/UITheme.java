package ui;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * UITheme - Professional Dark/Light theme manager
 * 
 * Applies consistent theme across entire application.
 * Uses UIManager for system-wide changes.
 */
public class UITheme {
    
    // Current theme state
    private static boolean isDarkMode = false;
    
    // Light Theme Colors
    private static final Color LIGHT_BG = new Color(255, 255, 255);
    private static final Color LIGHT_FG = new Color(30, 30, 30);
    private static final Color LIGHT_PANEL_BG = new Color(245, 245, 245);
    private static final Color LIGHT_TOOLBAR_BG = new Color(240, 240, 240);
    private static final Color LIGHT_TABLE_HEADER_BG = new Color(230, 230, 230);
    private static final Color LIGHT_TABLE_GRID = new Color(220, 220, 220);
    private static final Color LIGHT_BORDER = new Color(180, 180, 180);
    private static final Color LIGHT_SELECTION_BG = new Color(184, 207, 229);
    private static final Color LIGHT_SELECTION_FG = new Color(0, 0, 0);
    
    // Dark Theme Colors
    private static final Color DARK_BG = new Color(43, 43, 43);
    private static final Color DARK_FG = new Color(230, 230, 230);
    private static final Color DARK_PANEL_BG = new Color(50, 50, 50);
    private static final Color DARK_TOOLBAR_BG = new Color(60, 60, 60);
    private static final Color DARK_TABLE_HEADER_BG = new Color(55, 55, 55);
    private static final Color DARK_TABLE_GRID = new Color(70, 70, 70);
    private static final Color DARK_BORDER = new Color(80, 80, 80);
    private static final Color DARK_SELECTION_BG = new Color(75, 110, 175);
    private static final Color DARK_SELECTION_FG = new Color(255, 255, 255);
    
    // Accent Colors
    public static final Color ACCENT_BLUE = new Color(66, 133, 244);
    public static final Color ACCENT_GREEN = new Color(52, 168, 83);
    public static final Color ACCENT_PURPLE = new Color(142, 68, 173);
    private static Color accentColor = ACCENT_BLUE;
    
    /**
     * Apply Light Theme
     */
    public static void applyLightTheme() {
        isDarkMode = false;
        
        // Set UIManager defaults
        UIManager.put("control", LIGHT_PANEL_BG);
        UIManager.put("Panel.background", LIGHT_PANEL_BG);
        UIManager.put("Button.background", LIGHT_PANEL_BG);
        UIManager.put("Button.foreground", LIGHT_FG);
        UIManager.put("Label.foreground", LIGHT_FG);
        UIManager.put("TextField.background", LIGHT_BG);
        UIManager.put("TextField.foreground", LIGHT_FG);
        UIManager.put("ComboBox.background", LIGHT_BG);
        UIManager.put("ComboBox.foreground", LIGHT_FG);
        UIManager.put("Table.background", LIGHT_BG);
        UIManager.put("Table.foreground", LIGHT_FG);
        UIManager.put("Table.gridColor", LIGHT_TABLE_GRID);
        UIManager.put("Table.selectionBackground", LIGHT_SELECTION_BG);
        UIManager.put("Table.selectionForeground", LIGHT_SELECTION_FG);
        UIManager.put("TableHeader.background", LIGHT_TABLE_HEADER_BG);
        UIManager.put("TableHeader.foreground", LIGHT_FG);
        UIManager.put("ScrollPane.background", LIGHT_BG);
        UIManager.put("TitledBorder.titleColor", LIGHT_FG);
    }
    
    /**
     * Apply Dark Theme
     */
    public static void applyDarkTheme() {
        isDarkMode = true;
        
        // Set UIManager defaults for all components
        UIManager.put("control", DARK_PANEL_BG);
        UIManager.put("Panel.background", DARK_PANEL_BG);
        UIManager.put("Button.background", DARK_TOOLBAR_BG);
        UIManager.put("Button.foreground", DARK_FG);
        UIManager.put("Label.foreground", DARK_FG);
        UIManager.put("TextField.background", DARK_BG);
        UIManager.put("TextField.foreground", DARK_FG);
        UIManager.put("TextField.caretForeground", DARK_FG);
        UIManager.put("ComboBox.background", DARK_BG);
        UIManager.put("ComboBox.foreground", DARK_FG);
        UIManager.put("Table.background", DARK_BG);
        UIManager.put("Table.foreground", DARK_FG);
        UIManager.put("Table.gridColor", DARK_TABLE_GRID);
        UIManager.put("Table.selectionBackground", DARK_SELECTION_BG);
        UIManager.put("Table.selectionForeground", DARK_SELECTION_FG);
        UIManager.put("TableHeader.background", DARK_TABLE_HEADER_BG);
        UIManager.put("TableHeader.foreground", DARK_FG);
        UIManager.put("ScrollPane.background", DARK_BG);
        UIManager.put("Viewport.background", DARK_BG);
        UIManager.put("TitledBorder.titleColor", DARK_FG);
    }
    
    /**
     * Apply theme to specific components
     */
    public static void applyThemeToFrame(JFrame frame) {
        if (isDarkMode) {
            frame.getContentPane().setBackground(DARK_PANEL_BG);
        } else {
            frame.getContentPane().setBackground(LIGHT_PANEL_BG);
        }
    }
    
    public static void applyThemeToToolBar(JToolBar toolBar) {
        if (isDarkMode) {
            toolBar.setBackground(DARK_TOOLBAR_BG);
            toolBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, DARK_BORDER));
        } else {
            toolBar.setBackground(LIGHT_TOOLBAR_BG);
            toolBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, LIGHT_BORDER));
        }
    }
    
    public static void applyThemeToTable(JTable table) {
        if (isDarkMode) {
            table.setBackground(DARK_BG);
            table.setForeground(DARK_FG);
            table.setGridColor(DARK_TABLE_GRID);
            table.setSelectionBackground(DARK_SELECTION_BG);
            table.setSelectionForeground(DARK_SELECTION_FG);
            
            JTableHeader header = table.getTableHeader();
            header.setOpaque(true);
            header.setBackground(new Color(45, 45, 48));
            header.setForeground(new Color(240, 240, 240));
            header.setFont(new Font("Segoe UI", Font.BOLD, 12));
            
            // Custom renderer forces correct colors on Windows LAF where
            // setBackground/setForeground on JTableHeader are ignored.
            header.setDefaultRenderer(new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable t, Object value,
                        boolean isSelected, boolean hasFocus, int row, int column) {
                    JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
                    lbl.setBackground(new Color(45, 45, 48));
                    lbl.setForeground(new Color(240, 240, 240));
                    lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
                    lbl.setHorizontalAlignment(SwingConstants.LEFT);
                    lbl.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 1, new Color(65, 65, 68)),
                        BorderFactory.createEmptyBorder(4, 8, 4, 8)
                    ));
                    lbl.setOpaque(true);
                    return lbl;
                }
            });
        } else {
            table.setBackground(LIGHT_BG);
            table.setForeground(LIGHT_FG);
            table.setGridColor(LIGHT_TABLE_GRID);
            table.setSelectionBackground(LIGHT_SELECTION_BG);
            table.setSelectionForeground(LIGHT_SELECTION_FG);
            
            JTableHeader header = table.getTableHeader();
            header.setOpaque(true);
            header.setBackground(LIGHT_TABLE_HEADER_BG);
            header.setForeground(LIGHT_FG);
            header.setFont(new Font("Segoe UI", Font.BOLD, 12));
            
            // Matching custom renderer for light mode consistency
            header.setDefaultRenderer(new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable t, Object value,
                        boolean isSelected, boolean hasFocus, int row, int column) {
                    JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
                    lbl.setBackground(LIGHT_TABLE_HEADER_BG);
                    lbl.setForeground(LIGHT_FG);
                    lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
                    lbl.setHorizontalAlignment(SwingConstants.LEFT);
                    lbl.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 1, new Color(200, 200, 200)),
                        BorderFactory.createEmptyBorder(4, 8, 4, 8)
                    ));
                    lbl.setOpaque(true);
                    return lbl;
                }
            });
        }
    }
    
    public static void applyThemeToStatusBar(JLabel statusBar) {
        if (isDarkMode) {
            statusBar.setForeground(DARK_FG);
            statusBar.setBackground(DARK_PANEL_BG);
            statusBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, DARK_BORDER),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
            ));
        } else {
            statusBar.setForeground(LIGHT_FG);
            statusBar.setBackground(LIGHT_PANEL_BG);
            statusBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, LIGHT_BORDER),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
            ));
        }
    }
    
    /**
     * Refresh all components in a container
     */
    public static void refreshUI(Container container) {
        SwingUtilities.updateComponentTreeUI(container);
        container.repaint();
    }
    
    /**
     * Check if dark mode is active
     */
    public static boolean isDarkMode() {
        return isDarkMode;
    }
    
    /**
     * Toggle between themes
     */
    public static void toggleTheme() {
        if (isDarkMode) {
            applyLightTheme();
        } else {
            applyDarkTheme();
        }
    }
    
    // ==================== ACCENT COLOR SYSTEM ====================
    
    public static Color getAccentColor() { return accentColor; }
    
    public static void setAccentColor(Color c) { accentColor = c; }
    
    // ==================== THEME COLOR GETTERS ====================
    
    public static Color getBackground() {
        return isDarkMode ? DARK_BG : LIGHT_BG;
    }
    
    public static Color getForeground() {
        return isDarkMode ? DARK_FG : LIGHT_FG;
    }
    
    public static Color getPanelBackground() {
        return isDarkMode ? DARK_PANEL_BG : LIGHT_PANEL_BG;
    }
    
    public static Color getToolbarBackground() {
        return isDarkMode ? DARK_TOOLBAR_BG : LIGHT_TOOLBAR_BG;
    }
    
    public static Color getBorderColor() {
        return isDarkMode ? DARK_BORDER : LIGHT_BORDER;
    }
    
    public static Color getSelectionBackground() {
        return isDarkMode ? DARK_SELECTION_BG : LIGHT_SELECTION_BG;
    }
    
    public static Color getSelectionForeground() {
        return isDarkMode ? DARK_SELECTION_FG : LIGHT_SELECTION_FG;
    }
    
    public static Color getTableHeaderBackground() {
        return isDarkMode ? DARK_TABLE_HEADER_BG : LIGHT_TABLE_HEADER_BG;
    }
    
    public static Color getTableGridColor() {
        return isDarkMode ? DARK_TABLE_GRID : LIGHT_TABLE_GRID;
    }

    // ==================== PROFESSIONAL COMPONENT FACTORIES ====================

    /**
     * Creates a modern gradient accent button (primary action style).
     * Rounded corners, gradient fill, white text.
     */
    public static JButton createAccentButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color top, bot;
                if (getModel().isPressed()) {
                    top = bot = accentColor.darker().darker();
                } else if (getModel().isRollover()) {
                    top = accentColor.brighter();
                    bot = accentColor;
                } else {
                    top = accentColor;
                    bot = accentColor.darker();
                }
                GradientPaint gp = new GradientPaint(0, 0, top, 0, getHeight(), bot);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    /**
     * Creates a secondary outlined button (cancel/secondary action style).
     */
    public static JButton createSecondaryButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg;
                if (getModel().isPressed()) {
                    bg = isDarkMode ? new Color(80, 80, 90) : new Color(215, 215, 228);
                } else if (getModel().isRollover()) {
                    bg = isDarkMode ? new Color(72, 72, 82) : new Color(228, 228, 240);
                } else {
                    bg = isDarkMode ? new Color(62, 62, 72) : new Color(240, 240, 250);
                }
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(isDarkMode ? new Color(100, 100, 120) : new Color(170, 170, 200));
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setForeground(isDarkMode ? new Color(200, 200, 215) : new Color(50, 50, 75));
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    /**
     * Applies professional rounded-border + focus highlight to a text field.
     */
    public static void styleTextField(JTextField field) {
        Color normalBorder = isDarkMode ? new Color(80, 80, 95) : new Color(195, 195, 215);
        applyRoundedFieldStyle(field, normalBorder);
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    new RoundedBorder(7, accentColor, 1.8f),
                    BorderFactory.createEmptyBorder(5, 10, 5, 10)));
            }
            @Override
            public void focusLost(FocusEvent e) {
                applyRoundedFieldStyle(field, normalBorder);
            }
        });
    }

    /**
     * Applies professional rounded-border + focus highlight to a password field.
     */
    public static void stylePasswordField(JPasswordField field) {
        Color normalBorder = isDarkMode ? new Color(80, 80, 95) : new Color(195, 195, 215);
        applyRoundedFieldStyle(field, normalBorder);
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    new RoundedBorder(7, accentColor, 1.8f),
                    BorderFactory.createEmptyBorder(5, 10, 5, 10)));
            }
            @Override
            public void focusLost(FocusEvent e) {
                applyRoundedFieldStyle(field, normalBorder);
            }
        });
    }

    private static void applyRoundedFieldStyle(JComponent field, Color borderColor) {
        field.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(7, borderColor, 1.4f),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        if (isDarkMode) {
            field.setBackground(new Color(38, 38, 50));
            field.setForeground(new Color(225, 225, 235));
            if (field instanceof JTextField) ((JTextField) field).setCaretColor(new Color(200, 200, 220));
            if (field instanceof JPasswordField) ((JPasswordField) field).setCaretColor(new Color(200, 200, 220));
        } else {
            field.setBackground(Color.WHITE);
            field.setForeground(new Color(20, 20, 40));
            if (field instanceof JTextField) ((JTextField) field).setCaretColor(new Color(40, 40, 80));
            if (field instanceof JPasswordField) ((JPasswordField) field).setCaretColor(new Color(40, 40, 80));
        }
    }

    /**
     * Creates a panel with a subtle drop shadow and rounded border background.
     * Used for login card, form sections, etc.
     */
    public static JPanel createCardPanel(Color background, int arcRadius) {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Shadow
                g2.setColor(new Color(0, 0, 0, 22));
                g2.fillRoundRect(4, 6, getWidth() - 6, getHeight() - 6, arcRadius + 4, arcRadius + 4);
                g2.setColor(background);
                g2.fillRoundRect(0, 0, getWidth() - 4, getHeight() - 4, arcRadius, arcRadius);
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setBackground(background);
        return panel;
    }

    /**
     * Rounded border with configurable stroke width and color.
     */
    public static class RoundedBorder extends AbstractBorder {
        private final int radius;
        private final Color color;
        private final float strokeWidth;

        public RoundedBorder(int radius, Color color) {
            this(radius, color, 1.5f);
        }

        public RoundedBorder(int radius, Color color, float strokeWidth) {
            this.radius = radius;
            this.color = color;
            this.strokeWidth = strokeWidth;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(strokeWidth));
            g2.drawRoundRect(x, y, width - 1, height - 1, radius * 2, radius * 2);
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(radius + 1, radius + 1, radius + 1, radius + 1);
        }
    }

    /**
     * Returns the alternate row color for table zebra striping.
     */
    public static Color getAlternateRowColor() {
        return isDarkMode ? new Color(48, 48, 55) : new Color(248, 248, 252);
    }
}
