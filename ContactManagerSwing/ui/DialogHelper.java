package ui;

import javax.swing.*;
import java.awt.*;

/**
 * DialogHelper - Professional dialog utilities with proper positioning and icons.
 * 
 * All dialogs appear at top-right corner of parent window.
 * All dialogs use proper icons (info, warning, error, question, success).
 */
public class DialogHelper {

    /**
     * Show information message at top-right corner.
     */
    public static void showInfo(Component parent, String message, String title) {
        showDialog(parent, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Show warning message at top-right corner.
     */
    public static void showWarning(Component parent, String message, String title) {
        showDialog(parent, message, title, JOptionPane.WARNING_MESSAGE);
    }

    /**
     * Show error message at top-right corner.
     */
    public static void showError(Component parent, String message, String title) {
        showDialog(parent, message, title, JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Show success message at top-right corner (using information icon with green text).
     */
    public static void showSuccess(Component parent, String message, String title) {
        // Create custom panel with green success styling
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        
        // Success icon (checkmark)
        JLabel iconLabel = new JLabel("✓");
        iconLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        iconLabel.setForeground(new Color(46, 125, 50)); // Green
        panel.add(iconLabel, BorderLayout.WEST);
        
        // Message
        JLabel messageLabel = new JLabel("<html><div style='width: 250px;'>" + message + "</div></html>");
        messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        panel.add(messageLabel, BorderLayout.CENTER);
        
        showCustomDialog(parent, panel, title, JOptionPane.PLAIN_MESSAGE);
    }

    /**
     * Show confirmation dialog (YES/NO) at top-right corner.
     * Returns true if user clicks YES, false otherwise.
     */
    public static boolean showConfirm(Component parent, String message, String title) {
        int result = showConfirmDialog(parent, message, title, 
            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        return result == JOptionPane.YES_OPTION;
    }

    /**
     * Show confirmation dialog (YES/NO/CANCEL) at top-right corner.
     * Returns JOptionPane.YES_OPTION, NO_OPTION, or CANCEL_OPTION.
     */
    public static int showConfirmYesNoCancel(Component parent, String message, String title) {
        return showConfirmDialog(parent, message, title, 
            JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
    }

    /**
     * Internal method to show standard dialog positioned at top-right.
     */
    private static void showDialog(Component parent, String message, String title, int messageType) {
        // Create styled message panel
        JPanel panel = createMessagePanel(message);
        
        showCustomDialog(parent, panel, title, messageType);
    }

    /**
     * Internal method to show custom dialog positioned at top-right.
     */
    private static void showCustomDialog(Component parent, Object message, String title, int messageType) {
        JOptionPane pane = new JOptionPane(message, messageType);
        
        JDialog dialog = pane.createDialog(parent, title);
        
        // Position at top-right corner
        positionDialogTopRight(dialog, parent);
        
        // Make it look professional
        dialog.setAlwaysOnTop(true);
        dialog.setVisible(true);
        dialog.dispose();
    }

    /**
     * Internal method to show confirmation dialog positioned at top-right.
     */
    private static int showConfirmDialog(Component parent, String message, String title, 
                                        int optionType, int messageType) {
        // Create styled message panel
        JPanel panel = createMessagePanel(message);
        
        JOptionPane pane = new JOptionPane(panel, messageType, optionType);
        
        JDialog dialog = pane.createDialog(parent, title);
        
        // Position at top-right corner
        positionDialogTopRight(dialog, parent);
        
        // Make it look professional
        dialog.setAlwaysOnTop(true);
        dialog.setVisible(true);
        
        // Get result
        Object result = pane.getValue();
        dialog.dispose();
        
        if (result == null || !(result instanceof Integer)) {
            return JOptionPane.CLOSED_OPTION;
        }
        return (Integer) result;
    }

    /**
     * Create a professionally styled message panel.
     */
    private static JPanel createMessagePanel(String message) {
        JPanel panel = new JPanel(new BorderLayout());
        
        JLabel label = new JLabel("<html><div style='width: 280px; padding: 5px;'>" + 
                                 message + "</div></html>");
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        panel.add(label, BorderLayout.CENTER);
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        return panel;
    }

    /**
     * Position dialog at top-right corner of parent window or screen.
     */
    private static void positionDialogTopRight(JDialog dialog, Component parent) {
        try {
            // Pack dialog first to get actual size
            dialog.pack();
            
            if (parent != null) {
                Window parentWindow = SwingUtilities.getWindowAncestor(parent);
                if (parentWindow != null && parentWindow.isVisible()) {
                    Point loc = parentWindow.getLocationOnScreen();
                    Dimension parentSize = parentWindow.getSize();
                    Dimension dialogSize = dialog.getSize();
                    
                    // Position at top-right corner with some padding
                    int x = loc.x + parentSize.width - dialogSize.width - 20;
                    int y = loc.y + 60; // Below title bar
                    
                    dialog.setLocation(x, y);
                    return;
                }
            }
            
            // Fallback: position at screen top-right
            Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
            Dimension dialogSize = dialog.getSize();
            int x = screen.width - dialogSize.width - 20;
            int y = 60;
            dialog.setLocation(x, y);
            
        } catch (Exception e) {
            // If positioning fails, let system decide
            dialog.setLocationRelativeTo(parent);
        }
    }
}
