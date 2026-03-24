package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Toast Notification System
 * 
 * Non-blocking floating notification that auto-disappears.
 * Replaces JOptionPane for success messages.
 * Supports fade in/out animation where platform allows.
 * Positioned at top-right corner for better visibility.
 * 
 * Usage: Toast.show(parentComponent, "✔ Success message!");
 */
public class Toast {

    private static final int DEFAULT_DURATION = 2500;

    public static void show(Component parent, String message) {
        show(parent, message, DEFAULT_DURATION);
    }

    public static void show(Component parent, String message, int duration) {
        SwingUtilities.invokeLater(() -> {
            // Find parent window
            Window parentWindow;
            if (parent instanceof Window) {
                parentWindow = (Window) parent;
            } else if (parent != null) {
                parentWindow = SwingUtilities.getWindowAncestor(parent);
            } else {
                parentWindow = null;
            }

            JWindow toast = new JWindow(parentWindow);
            toast.setAlwaysOnTop(true);

            // Build professional toast panel with icon
            JPanel panel = new JPanel(new BorderLayout(10, 0));
            
            // Determine type and color based on message content
            Color bg;
            String iconText;
            if (message.contains("✔") || message.toLowerCase().contains("success")) {
                bg = new Color(46, 125, 50); // Green for success
                iconText = "✓";
            } else if (message.contains("⚠") || message.toLowerCase().contains("warning")) {
                bg = new Color(237, 108, 2); // Orange for warning
                iconText = "⚠";
            } else if (message.contains("❌") || message.toLowerCase().contains("error")) {
                bg = new Color(198, 40, 40); // Red for error
                iconText = "✕";
            } else {
                bg = UITheme.getAccentColor(); // Default accent color
                iconText = "ℹ";
            }
            
            panel.setBackground(bg);
            panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(bg.darker(), 2),
                BorderFactory.createEmptyBorder(12, 15, 12, 20)
            ));

            // Icon label — painted with Graphics2D so it never shows as a rectangle box
            JLabel iconLabel = new JLabel(createToastIcon(iconText, 22));
            iconLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 2));
            panel.add(iconLabel, BorderLayout.WEST);

            // Message label (remove emoji from message if present)
            String displayMessage = message.replaceFirst("^[✔✓⚠❌ℹ️]+\\s*", "");
            JLabel messageLabel = new JLabel(displayMessage);
            messageLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
            messageLabel.setForeground(Color.WHITE);
            panel.add(messageLabel, BorderLayout.CENTER);

            toast.add(panel);
            toast.pack();

            // Position TOP-RIGHT of parent window (improved UX)
            try {
                if (parentWindow != null && parentWindow.isVisible()) {
                    Point loc = parentWindow.getLocationOnScreen();
                    Dimension ps = parentWindow.getSize();
                    Dimension ts = toast.getSize();
                    toast.setLocation(
                        loc.x + ps.width - ts.width - 20,
                        loc.y + 60  // Top-right, below title bar
                    );
                } else {
                    positionOnScreen(toast);
                }
            } catch (Exception e) {
                positionOnScreen(toast);
            }

            // Check fade support
            boolean canFade = false;
            try {
                toast.setOpacity(0.0f);
                canFade = true;
            } catch (Exception e) {
                // Opacity not supported on this platform
            }

            toast.setVisible(true);

            if (canFade) {
                // Fade in
                final float[] opacity = {0f};
                Timer fadeIn = new Timer(25, null);
                fadeIn.addActionListener(e -> {
                    opacity[0] = Math.min(1.0f, opacity[0] + 0.15f);
                    try {
                        toast.setOpacity(opacity[0]);
                    } catch (Exception ex) {
                        fadeIn.stop();
                    }
                    if (opacity[0] >= 1.0f) {
                        fadeIn.stop();
                        scheduleFadeOut(toast, duration);
                    }
                });
                fadeIn.start();
            } else {
                // No fade support - simple timer close
                Timer closeTimer = new Timer(duration, e -> toast.dispose());
                closeTimer.setRepeats(false);
                closeTimer.start();
            }
        });
    }

    /**
     * Creates a vector-drawn icon for toast notifications.
     * type: "✓" = checkmark, "⚠" = warning triangle, "✕" = X cross, "ℹ" = info circle
     */
    private static ImageIcon createToastIcon(String type, int size) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g2.setColor(new Color(255, 255, 255, 220));

        int m = size / 5; // margin
        switch (type) {
            case "✓": {
                // Checkmark
                g2.setStroke(new BasicStroke(size / 5.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int[] xs = {m, size / 3 + 1, size - m};
                int[] ys = {size / 2, size - m, m};
                g2.drawPolyline(xs, ys, 3);
                break;
            }
            case "⚠": {
                // Warning triangle with exclamation
                g2.setStroke(new BasicStroke(size / 6.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int[] xs = {size / 2, size - m, m};
                int[] ys = {m, size - m, size - m};
                g2.drawPolygon(xs, ys, 3);
                int cx = size / 2;
                int topY = m + size / 5;
                int botY = size - m - size / 4;
                g2.drawLine(cx, topY, cx, botY - size / 9);
                g2.fillOval(cx - size / 10, botY, size / 5, size / 5);
                break;
            }
            case "✕": {
                // X cross
                g2.setStroke(new BasicStroke(size / 5.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(m, m, size - m, size - m);
                g2.drawLine(size - m, m, m, size - m);
                break;
            }
            default: {
                // Info circle with "i"
                g2.setStroke(new BasicStroke(size / 7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawOval(m, m, size - 2 * m, size - 2 * m);
                int cx = size / 2;
                g2.fillOval(cx - size / 12, m + size / 5, size / 6, size / 6); // dot
                g2.setStroke(new BasicStroke(size / 6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(cx, size / 2, cx, size - m - size / 5);           // stem
                break;
            }
        }
        g2.dispose();
        return new ImageIcon(img);
    }

    private static void scheduleFadeOut(JWindow toast, int delay) {
        Timer waitTimer = new Timer(delay, e -> {
            final float[] opacity = {1.0f};
            Timer fadeOut = new Timer(25, null);
            fadeOut.addActionListener(e2 -> {
                opacity[0] = Math.max(0f, opacity[0] - 0.15f);
                try {
                    toast.setOpacity(opacity[0]);
                } catch (Exception ex) {
                    toast.dispose();
                    fadeOut.stop();
                    return;
                }
                if (opacity[0] <= 0f) {
                    toast.dispose();
                    fadeOut.stop();
                }
            });
            fadeOut.start();
        });
        waitTimer.setRepeats(false);
        waitTimer.start();
    }

    private static void positionOnScreen(JWindow toast) {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        toast.setLocation(
            screen.width - toast.getWidth() - 20,
            60  // Top-right of screen
        );
    }
}
