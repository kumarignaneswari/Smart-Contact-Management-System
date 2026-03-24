package ui;

import service.ContactService;
import model.Contact;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Statistics Dashboard Panel — Professional Gradient Cards
 *
 * Collapsible panel displaying gradient statistic cards for:
 * Total Contacts, Friends, Family, Work, Emergency, Deleted.
 * Cards have hover animation and update dynamically via refreshStatistics().
 */
public class StatisticsPanel extends JPanel {

    private ContactService contactService;
    private JPanel cardsPanel;
    private JPanel contentPanel;
    private JPanel headerPanel;
    private JButton toggleButton;
    private JLabel headerLabel;
    private boolean collapsed = false;

    // Card count labels for dynamic updates
    private JLabel lblTotal, lblFriends, lblFamily, lblWork, lblEmergency, lblDeleted;

    // Category colors (top/bottom gradient pairs)
    private static final Color[][] CARD_GRADIENTS = {
        { new Color(52, 120, 232),  new Color(30,  85, 190)  },  // Total – Blue
        { new Color(46, 184,  98),  new Color(28, 140,  65)  },  // Friends – Green
        { new Color(240, 172,  20), new Color(195, 128,   5)  },  // Family – Gold
        { new Color(230,  55,  55), new Color(180,  28,  28)  },  // Work – Red
        { new Color(255, 115,  20), new Color(200,  75,   5)  },  // Emergency – Orange
        { new Color(110, 110, 125), new Color( 75,  75,  90)  }   // Deleted – Gray
    };

    public StatisticsPanel(ContactService contactService) {
        this.contactService = contactService;
        setLayout(new BorderLayout());
        setOpaque(true);

        // ===== Header =====
        headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        headerPanel.setOpaque(true);

        headerLabel = new JLabel("  Dashboard", IconRenderer.createSmallIcon(IconRenderer.IconType.STATS), JLabel.LEFT);
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));

        toggleButton = new JButton("▼ Hide");
        toggleButton.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        toggleButton.setPreferredSize(new Dimension(65, 22));
        toggleButton.setFocusable(false);
        toggleButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        toggleButton.addActionListener(e -> toggleCollapse());

        headerPanel.add(headerLabel, BorderLayout.WEST);
        headerPanel.add(toggleButton, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // ===== Content with Cards =====
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(6, 14, 12, 14));

        cardsPanel = new JPanel(new GridLayout(1, 6, 10, 0));
        cardsPanel.setOpaque(false);

        lblTotal     = addStatCard("Total",     "0", CARD_GRADIENTS[0], 0);  // heart
        lblFriends   = addStatCard("Friends",   "0", CARD_GRADIENTS[1], 1);  // person
        lblFamily    = addStatCard("Family",    "0", CARD_GRADIENTS[2], 2);  // house
        lblWork      = addStatCard("Work",      "0", CARD_GRADIENTS[3], 3);  // briefcase
        lblEmergency = addStatCard("Emergency", "0", CARD_GRADIENTS[4], 4);  // alert triangle
        lblDeleted   = addStatCard("Deleted",   "0", CARD_GRADIENTS[5], 5);  // recycle

        contentPanel.add(cardsPanel, BorderLayout.CENTER);
        add(contentPanel, BorderLayout.CENTER);

        applyTheme();
    }

    /**
     * Creates a gradient statistic card with hover lift effect.
     * iconType: 0=heart, 1=person, 2=house, 3=briefcase, 4=alert, 5=recycle
     */
    private JLabel addStatCard(String title, String count, Color[] gradient, int iconType) {
        JPanel card = new JPanel(new GridBagLayout()) {
            private boolean hovered = false;

            {
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { hovered = true;  repaint(); }
                    @Override public void mouseExited(MouseEvent e)  { hovered = false; repaint(); }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int arc = 14;
                int shadowAlpha = hovered ? 55 : 28;
                int shadowOffset = hovered ? 4 : 2;

                // Shadow
                g2.setColor(new Color(0, 0, 0, shadowAlpha));
                g2.fillRoundRect(shadowOffset, shadowOffset + 1,
                    getWidth() - shadowOffset - 1, getHeight() - shadowOffset - 1, arc + 2, arc + 2);

                // Gradient fill
                GradientPaint gp = new GradientPaint(
                    0, 0, hovered ? gradient[0].brighter() : gradient[0],
                    0, getHeight(), gradient[1]);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth() - shadowOffset, getHeight() - shadowOffset, arc, arc);

                // Subtle shine at top
                GradientPaint shine = new GradientPaint(0, 0, new Color(255, 255, 255, 40),
                    0, getHeight() / 2, new Color(255, 255, 255, 0));
                g2.setPaint(shine);
                g2.fillRoundRect(0, 0, getWidth() - shadowOffset, getHeight() / 2, arc, arc);

                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = GridBagConstraints.RELATIVE;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(1, 0, 1, 0);

        // Icon label — vector drawn, never shows as a rectangle box
        JLabel lblIcon = new JLabel(createStatCardIcon(iconType, 16), SwingConstants.CENTER);
        card.add(lblIcon, gbc);

        // Count label
        JLabel lblCount = new JLabel(count, SwingConstants.CENTER);
        lblCount.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblCount.setForeground(Color.WHITE);
        card.add(lblCount, gbc);

        // Title label
        JLabel lblTitle = new JLabel(title.toUpperCase(), SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 9));
        lblTitle.setForeground(new Color(255, 255, 255, 200));
        card.add(lblTitle, gbc);

        cardsPanel.add(card);
        return lblCount;
    }

    /**
     * Paints a small vector icon for a stat card. Never renders as a box.
     * iconType: 0=heart, 1=person, 2=house, 3=briefcase, 4=alert, 5=recycle
     */
    private static ImageIcon createStatCardIcon(int iconType, int size) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(new Color(255, 255, 255, 200));
        int m = size / 6;
        BasicStroke stroke = new BasicStroke(size / 6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        g2.setStroke(stroke);

        switch (iconType) {
            case 0: { // Heart
                int cx = size / 2, w2 = size / 4, top = m + 1, bottom = size - m;
                g2.fillOval(m, top, w2 + 1, w2 + 1);
                g2.fillOval(size / 2 - 1, top, w2 + 1, w2 + 1);
                int[] hx = {m, cx, size - m};
                int[] hy = {top + w2 / 2, bottom, top + w2 / 2};
                g2.fillPolygon(hx, hy, 3);
                break;
            }
            case 1: { // Person (head + body)
                int r = size / 5;
                int cx = size / 2;
                g2.fillOval(cx - r, m, r * 2, r * 2);
                g2.fillArc(m, m + r * 2, size - 2 * m, size - m, 0, 180);
                break;
            }
            case 2: { // House
                int[] rx = {m, size - m, size - m, m};
                int[] ry = {size / 2, size / 2, size - m, size - m};
                g2.fillPolygon(rx, ry, 4);
                int[] roofX = {m - 1, size / 2, size - m + 1};
                int[] roofY = {size / 2 + 1, m, size / 2 + 1};
                g2.fillPolygon(roofX, roofY, 3);
                break;
            }
            case 3: { // Briefcase
                g2.drawRoundRect(m, size / 3, size - 2 * m, size - size / 3 - m, 3, 3);
                g2.drawLine(size / 3, size / 3, size / 3, size / 4 + 1);
                g2.drawLine(2 * size / 3, size / 3, 2 * size / 3, size / 4 + 1);
                g2.drawRoundRect(size / 3, m + 1, size / 3, size / 4, 2, 2);
                g2.drawLine(m, size / 2, size - m, size / 2);
                break;
            }
            case 4: { // Alert triangle
                int[] tx = {size / 2, size - m, m};
                int[] ty = {m, size - m, size - m};
                g2.drawPolygon(tx, ty, 3);
                int cx = size / 2;
                g2.setStroke(new BasicStroke(size / 6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(cx, m + size / 4, cx, size - m - size / 4);
                g2.fillOval(cx - size / 10, size - m - size / 5, size / 5, size / 5);
                break;
            }
            default: { // Recycle (3 arrows in a triangle)
                int r2 = size / 2 - m;
                int cx = size / 2, cy = size / 2;
                g2.setStroke(new BasicStroke(size / 7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawArc(cx - r2, cy - r2, r2 * 2, r2 * 2, 90, 240);
                g2.drawLine(cx, m, cx + size / 4, m + size / 4);
                g2.drawLine(cx, m, cx - size / 4, m + size / 4);
                break;
            }
        }
        g2.dispose();
        return new ImageIcon(img);
    }

    /** Toggle collapse/expand of the statistics content. */
    private void toggleCollapse() {
        collapsed = !collapsed;
        contentPanel.setVisible(!collapsed);
        toggleButton.setText(collapsed ? "▶ Show" : "▼ Hide");
        revalidate();
        repaint();
    }

    /** Refresh all statistic counts from the database. */
    public void refreshStatistics() {
        try {
            List<Contact> all     = contactService.getAllContacts();
            List<Contact> deleted = contactService.getDeletedContacts();

            int friends = 0, family = 0, work = 0, emergency = 0;
            for (Contact c : all) {
                if (c.getCategory() != null) {
                    switch (c.getCategory()) {
                        case "Friends":   friends++;   break;
                        case "Family":    family++;    break;
                        case "Work":      work++;      break;
                        case "Emergency": emergency++; break;
                    }
                }
            }

            lblTotal.setText(String.valueOf(all.size()));
            lblFriends.setText(String.valueOf(friends));
            lblFamily.setText(String.valueOf(family));
            lblWork.setText(String.valueOf(work));
            lblEmergency.setText(String.valueOf(emergency));
            lblDeleted.setText(String.valueOf(deleted.size()));
        } catch (Exception e) {
            // Silent fail - don't break UI for stats
        }
    }

    /** Apply current theme colors to the panel. */
    public void applyTheme() {
        Color panelBg   = UITheme.isDarkMode() ? new Color(38, 38, 50)  : new Color(244, 245, 252);
        Color textColor = UITheme.isDarkMode() ? Color.WHITE            : new Color(30, 30, 50);
        Color borderClr = UITheme.isDarkMode() ? new Color(58, 58, 72)  : new Color(210, 210, 228);

        setBackground(panelBg);
        setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, borderClr));

        headerPanel.setBackground(panelBg);
        headerLabel.setForeground(textColor);

        contentPanel.setBackground(panelBg);
        cardsPanel.setBackground(panelBg);
    }
}
