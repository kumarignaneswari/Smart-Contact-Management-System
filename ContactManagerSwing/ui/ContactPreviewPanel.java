package ui;

import model.Contact;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.image.BufferedImage;
import java.text.SimpleDateFormat;

/**
 * Contact Preview Side Panel
 * 
 * Right-side panel that displays full details of the selected contact:
 * - Circular avatar with initials
 * - Name and category
 * - Phone, Email with copy buttons
 * - Created/Updated timestamps
 * 
 * Automatically updates when table selection changes.
 * Respects dark/light theme.
 */
public class ContactPreviewPanel extends JPanel {

    private AvatarPanel avatar;
    private JLabel lblName, lblNumber, lblEmail, lblCategory, lblCreatedAt, lblUpdatedAt;
    private JPanel detailsPanel;
    private JPanel emptyPanel;
    private CardLayout cardLayout;
    private Component parentFrame;
    private Contact currentContact;   // track selected contact for QR

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMM dd, yyyy HH:mm");

    public ContactPreviewPanel(Component parent) {
        this.parentFrame = parent;
        cardLayout = new CardLayout();
        setLayout(cardLayout);
        setPreferredSize(new Dimension(280, 0));

        // ===== Empty State =====
        emptyPanel = new JPanel(new GridBagLayout());
        JLabel emptyLabel = new JLabel("<html><center>Select a contact<br>to preview details</center></html>");
        emptyLabel.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        emptyLabel.setForeground(new Color(140, 140, 140));
        emptyLabel.setHorizontalAlignment(SwingConstants.CENTER);
        emptyPanel.add(emptyLabel);
        add(emptyPanel, "EMPTY");

        // ===== Details Panel =====
        detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        detailsPanel.setBorder(BorderFactory.createEmptyBorder(24, 16, 24, 16));

        // Avatar
        avatar = new AvatarPanel(80);
        JPanel avatarWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        avatarWrapper.setOpaque(false);
        avatarWrapper.add(avatar);
        avatarWrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        detailsPanel.add(avatarWrapper);
        detailsPanel.add(Box.createVerticalStrut(8));

        // QR Code button — opens QR dialog on click
        JButton btnQR = new JButton("Show QR Code");
        btnQR.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnQR.setFocusable(false);
        btnQR.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnQR.setToolTipText("Generate QR code for this contact");
        btnQR.addActionListener(e -> {
            if (currentContact != null) {
                Frame owner = (parentFrame instanceof Frame) ? (Frame) parentFrame
                           : (Frame) SwingUtilities.getWindowAncestor((Component) parentFrame);
                new QRCodeDialog(owner, currentContact).setVisible(true);
            }
        });
        JPanel qrWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        qrWrapper.setOpaque(false);
        qrWrapper.add(btnQR);
        qrWrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        detailsPanel.add(qrWrapper);
        detailsPanel.add(Box.createVerticalStrut(10));

        // Name
        lblName = new JLabel(" ");
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 18));
        JPanel nameWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        nameWrapper.setOpaque(false);
        nameWrapper.add(lblName);
        nameWrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        detailsPanel.add(nameWrapper);
        detailsPanel.add(Box.createVerticalStrut(4));

        // Category badge
        lblCategory = new JLabel(" ");
        lblCategory.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        JPanel catWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        catWrapper.setOpaque(false);
        catWrapper.add(lblCategory);
        catWrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        detailsPanel.add(catWrapper);
        detailsPanel.add(Box.createVerticalStrut(20));

        // Separator
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        detailsPanel.add(sep);
        detailsPanel.add(Box.createVerticalStrut(16));

        // Detail rows with copy buttons
        lblNumber = new JLabel("");
        detailsPanel.add(createDetailRow(IconRenderer.IconType.PHONE, "Phone", lblNumber, true));
        detailsPanel.add(Box.createVerticalStrut(14));

        lblEmail = new JLabel("");
        detailsPanel.add(createDetailRow(IconRenderer.IconType.EMAIL, "Email", lblEmail, true));
        detailsPanel.add(Box.createVerticalStrut(14));

        lblCreatedAt = new JLabel("");
        detailsPanel.add(createDetailRow(IconRenderer.IconType.CALENDAR, "Created", lblCreatedAt, false));
        detailsPanel.add(Box.createVerticalStrut(14));

        lblUpdatedAt = new JLabel("");
        detailsPanel.add(createDetailRow(IconRenderer.IconType.REFRESH, "Updated", lblUpdatedAt, false));

        detailsPanel.add(Box.createVerticalGlue());

        JScrollPane scrollPane = new JScrollPane(detailsPanel);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        add(scrollPane, "DETAILS");

        cardLayout.show(this, "EMPTY");
        applyTheme();
    }

    /**
     * Creates a detail row with label, value, and optional copy button.
     */
    private JPanel createDetailRow(IconRenderer.IconType iconType, String labelText, JLabel valueLabel, boolean hasCopy) {
        JPanel row = new JPanel(new BorderLayout(4, 2));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        JLabel titleLabel = new JLabel(labelText, IconRenderer.createSmallIcon(iconType), JLabel.LEFT);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        titleLabel.setForeground(new Color(130, 130, 130));

        valueLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JPanel valuePanel = new JPanel(new BorderLayout(4, 0));
        valuePanel.setOpaque(false);
        valuePanel.add(valueLabel, BorderLayout.CENTER);

        if (hasCopy) {
            JButton copyBtn = new JButton(createClipboardIcon(16));
            copyBtn.setPreferredSize(new Dimension(28, 22));
            copyBtn.setMargin(new Insets(0, 0, 0, 0));
            copyBtn.setFocusable(false);
            copyBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            copyBtn.setToolTipText("Copy to clipboard");
            copyBtn.addActionListener(e -> {
                String text = valueLabel.getText();
                if (text != null && !text.isEmpty() && !text.equals("N/A")) {
                    StringSelection selection = new StringSelection(text);
                    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
                    Toast.show(parentFrame, "\u2714 Copied to clipboard!");
                }
            });
            valuePanel.add(copyBtn, BorderLayout.EAST);
        }

        row.add(titleLabel, BorderLayout.NORTH);
        row.add(valuePanel, BorderLayout.CENTER);

        return row;
    }

    /**
     * Draws a vector clipboard icon. Never renders as a rectangle box.
     */
    private static ImageIcon createClipboardIcon(int size) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(new Color(100, 110, 140));
        int m = size / 8;
        // Clipboard body
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(m, size / 5, size - 2 * m, size * 4 / 5 - m, 2, 2);
        // Clip at top
        g2.fillRoundRect(size / 3, m, size / 3, size / 4, 2, 2);
        // Lines on board
        g2.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int lineLeft = m + size / 6;
        int lineRight = size - m - size / 6;
        g2.drawLine(lineLeft, size * 2 / 5, lineRight, size * 2 / 5);
        g2.drawLine(lineLeft, size * 3 / 5, lineRight, size * 3 / 5);
        g2.drawLine(lineLeft, size * 4 / 5 - m * 2, lineRight - size / 5, size * 4 / 5 - m * 2);
        g2.dispose();
        return new ImageIcon(img);
    }

    /**
     * Display contact details in the preview panel.
     */
    public void showContact(Contact contact) {
        if (contact == null) {
            cardLayout.show(this, "EMPTY");
            return;
        }

        currentContact = contact;

        avatar.setName(contact.getName());
        lblName.setText(contact.getName());

        // Category pill badge — colored text on semi-transparent background
        String cat = contact.getCategory() != null ? contact.getCategory() : "Unknown";
        Color catColor = getCategoryColor(cat);
        String hex = String.format("#%02x%02x%02x", catColor.getRed(), catColor.getGreen(), catColor.getBlue());
        lblCategory.setText("<html><span style='background-color:" + hex + ";color:white;"
            + "padding:2px 8px;border-radius:8px;font-size:11px;'>&nbsp;" + cat + "&nbsp;</span></html>");
        lblCategory.setForeground(catColor);
        lblNumber.setText(contact.getNumber() != null ? contact.getNumber() : "N/A");
        lblEmail.setText(contact.getEmail() != null && !contact.getEmail().isEmpty()
                ? contact.getEmail() : "N/A");
        lblCreatedAt.setText(contact.getCreatedAt() != null
                ? DATE_FORMAT.format(contact.getCreatedAt()) : "N/A");
        lblUpdatedAt.setText(contact.getUpdatedAt() != null
                ? DATE_FORMAT.format(contact.getUpdatedAt()) : "N/A");

        // Category color
        lblCategory.setForeground(getCategoryColor(contact.getCategory()));

        cardLayout.show(this, "DETAILS");
        revalidate();
        repaint();
    }

    /**
     * Clear the preview and show empty state.
     */
    public void clearPreview() {
        currentContact = null;
        cardLayout.show(this, "EMPTY");
    }

    private Color getCategoryColor(String category) {
        if (category == null) return Color.GRAY;
        switch (category) {
            case "Friends":   return new Color(52, 168, 83);
            case "Family":    return new Color(251, 188, 4);
            case "Work":      return new Color(234, 67, 53);
            case "Emergency": return new Color(255, 109, 0);
            default:          return Color.GRAY;
        }
    }

    /**
     * Apply current theme to the preview panel.
     */
    public void applyTheme() {
        Color panelBg   = UITheme.isDarkMode() ? new Color(42, 44, 56) : new Color(250, 250, 254);
        Color textColor = UITheme.isDarkMode() ? new Color(225, 225, 235) : new Color(20, 20, 40);
        Color borderClr = UITheme.isDarkMode() ? new Color(58, 58, 75)  : new Color(210, 210, 228);

        setBackground(panelBg);
        setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, borderClr));

        emptyPanel.setBackground(panelBg);
        detailsPanel.setBackground(panelBg);
        lblName.setForeground(textColor);
        lblNumber.setForeground(textColor);
        lblEmail.setForeground(textColor);
        lblCreatedAt.setForeground(textColor);
        lblUpdatedAt.setForeground(textColor);

        // Update scroll pane viewport
        for (Component c : getComponents()) {
            if (c instanceof JScrollPane) {
                JScrollPane sp = (JScrollPane) c;
                sp.getViewport().setBackground(panelBg);
                sp.setBackground(panelBg);
            }
        }

        revalidate();
        repaint();
    }
}
