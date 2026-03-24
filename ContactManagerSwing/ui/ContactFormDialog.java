package ui;

import model.Contact;
import service.ContactService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 * ContactFormDialog
 * 
 * Professional reusable dialog for Add and Edit operations.
 * Modal dialog that blocks parent window until closed.
 * Validates input before saving.
 */
public class ContactFormDialog extends JDialog {

    private ContactService contactService;
    private Contact contactToEdit; // null for add, populated for edit
    private boolean saveSuccessful = false;

    // UI Components
    private JTextField txtName;
    private JTextField txtNumber;
    private JTextField txtEmail;
    private JComboBox<String> cmbCategory;
    private JButton btnSave;
    private JButton btnCancel;

    private static final String[] CATEGORIES = {"Friends", "Family", "Work", "Emergency"};

    /**
     * Constructor for Add mode
     */
    public ContactFormDialog(JFrame parent, ContactService service) {
        this(parent, service, null);
    }

    /**
     * Constructor for Edit mode
     */
    public ContactFormDialog(JFrame parent, ContactService service, Contact contactToEdit) {
        super(parent, contactToEdit == null ? "Add New Contact" : "Edit Contact", true);
        this.contactService = service;
        this.contactToEdit = contactToEdit;
        
        initComponents();
        if (contactToEdit != null) {
            populateFields();
        }
        
        setSize(430, 520);
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    private void initComponents() {
        setLayout(new BorderLayout(0, 0));

        Color bg = UITheme.isDarkMode() ? new Color(38, 38, 50) : new Color(248, 249, 252);
        getContentPane().setBackground(bg);

        // ===== Gradient Header =====
        JPanel headerPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c1 = UITheme.isDarkMode() ? new Color(35, 55, 100) : new Color(42, 108, 185);
                Color c2 = UITheme.isDarkMode() ? new Color(25, 40, 80)  : new Color(25, 55, 130);
                GradientPaint gp = new GradientPaint(0, 0, c1, getWidth(), getHeight(), c2);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        // Icon circle in header
        JPanel iconCircle = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 50));
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(Color.WHITE);
                // Draw person icon
                int cx = getWidth() / 2, cy = getHeight() / 2;
                g2.fillOval(cx - 7, cy - 13, 14, 14);
                g2.fillRoundRect(cx - 10, cy + 3, 20, 12, 8, 8);
                g2.dispose();
            }
        };
        iconCircle.setOpaque(false);
        iconCircle.setPreferredSize(new Dimension(44, 44));

        JLabel titleLabel = new JLabel(contactToEdit == null ? "Add New Contact" : "Edit Contact");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);

        JLabel subtitleLabel = new JLabel(contactToEdit == null
            ? "Fill in the details below"
            : "Update contact information");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitleLabel.setForeground(new Color(200, 220, 255));

        JPanel headingText = new JPanel(new GridLayout(2, 1, 0, 2));
        headingText.setOpaque(false);
        headingText.add(titleLabel);
        headingText.add(subtitleLabel);

        headerPanel.add(iconCircle, BorderLayout.WEST);
        headerPanel.add(Box.createHorizontalStrut(12), BorderLayout.CENTER);
        // Use a sub-panel to position text nicely next to icon
        JPanel headerContent = new JPanel(new BorderLayout());
        headerContent.setOpaque(false);
        headerContent.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 0));
        headerContent.add(headingText, BorderLayout.CENTER);
        headerPanel.add(headerContent, BorderLayout.CENTER);

        add(headerPanel, BorderLayout.NORTH);

        // ===== Form Panel =====
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setOpaque(true);
        mainPanel.setBackground(bg);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 24, 16, 24));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 0, 4, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // ---- Name ----
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        mainPanel.add(formLabel("Name *"), gbc);
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 1.0;
        txtName = new JTextField(22);
        txtName.setPreferredSize(new Dimension(300, 36));
        UITheme.styleTextField(txtName);
        mainPanel.add(txtName, gbc);

        // ---- Number ----
        gbc.gridx = 0; gbc.gridy = 2;
        mainPanel.add(formLabel("Phone Number *"), gbc);
        gbc.gridx = 0; gbc.gridy = 3;
        txtNumber = new JTextField(22);
        txtNumber.setPreferredSize(new Dimension(300, 36));
        UITheme.styleTextField(txtNumber);
        addNumericValidation(txtNumber);
        mainPanel.add(txtNumber, gbc);

        // ---- Email ----
        gbc.gridx = 0; gbc.gridy = 4;
        mainPanel.add(formLabel("Email Address"), gbc);
        gbc.gridx = 0; gbc.gridy = 5;
        txtEmail = new JTextField(22);
        txtEmail.setPreferredSize(new Dimension(300, 36));
        UITheme.styleTextField(txtEmail);
        mainPanel.add(txtEmail, gbc);

        // ---- Category ----
        gbc.gridx = 0; gbc.gridy = 6;
        mainPanel.add(formLabel("Category"), gbc);
        gbc.gridx = 0; gbc.gridy = 7;
        cmbCategory = new JComboBox<>(CATEGORIES);
        cmbCategory.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cmbCategory.setPreferredSize(new Dimension(300, 36));
        if (UITheme.isDarkMode()) {
            cmbCategory.setBackground(new Color(38, 38, 50));
            cmbCategory.setForeground(new Color(225, 225, 235));
        } else {
            cmbCategory.setBackground(Color.WHITE);
            cmbCategory.setForeground(new Color(20, 20, 40));
        }
        mainPanel.add(cmbCategory, gbc);

        add(mainPanel, BorderLayout.CENTER);

        // ===== Button Panel =====
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        buttonPanel.setBackground(bg);
        buttonPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0,
                UITheme.isDarkMode() ? new Color(60, 60, 75) : new Color(210, 210, 225)),
            BorderFactory.createEmptyBorder(0, 16, 0, 16)));

        btnCancel = UITheme.createSecondaryButton("Cancel");
        btnCancel.setPreferredSize(new Dimension(105, 36));
        btnCancel.addActionListener(e -> dispose());

        btnSave = UITheme.createAccentButton(contactToEdit == null ? "Add Contact" : "Update");
        btnSave.setPreferredSize(new Dimension(125, 36));
        btnSave.addActionListener(e -> saveContact());

        buttonPanel.add(btnCancel);
        buttonPanel.add(btnSave);
        add(buttonPanel, BorderLayout.SOUTH);

        // Keyboard shortcuts
        getRootPane().setDefaultButton(btnSave);
        KeyStroke escapeKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        getRootPane().registerKeyboardAction(e -> dispose(), escapeKeyStroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private JLabel formLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(UITheme.isDarkMode() ? new Color(170, 185, 220) : new Color(60, 75, 110));
        lbl.setBorder(BorderFactory.createEmptyBorder(4, 0, 1, 0));
        return lbl;
    }

    private void addNumericValidation(JTextField textField) {
        textField.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                // Allow only digits, backspace, delete, +, -, (, ), space
                if (!Character.isDigit(c) && c != KeyEvent.VK_BACK_SPACE && 
                    c != KeyEvent.VK_DELETE && c != '+' && c != '-' && 
                    c != '(' && c != ')' && c != ' ') {
                    e.consume();
                }
            }
        });
    }

    private void populateFields() {
        if (contactToEdit != null) {
            txtName.setText(contactToEdit.getName());
            txtNumber.setText(contactToEdit.getNumber());
            txtEmail.setText(contactToEdit.getEmail() != null ? contactToEdit.getEmail() : "");
            
            String category = contactToEdit.getCategory();
            for (int i = 0; i < CATEGORIES.length; i++) {
                if (CATEGORIES[i].equals(category)) {
                    cmbCategory.setSelectedIndex(i);
                    break;
                }
            }
        }
    }

    private void saveContact() {
        try {
            // Get values
            String name = txtName.getText().trim();
            String number = txtNumber.getText().trim();
            String email = txtEmail.getText().trim();
            String category = (String) cmbCategory.getSelectedItem();

            // Create contact object
            Contact contact;
            if (contactToEdit == null) {
                // Add mode
                contact = new Contact(name, number, email, category);
            } else {
                // Edit mode
                contact = new Contact(
                    contactToEdit.getId(),
                    name, number, email, category,
                    contactToEdit.isDeleted(),
                    contactToEdit.getCreatedAt(),
                    contactToEdit.getUpdatedAt()
                );
            }

            // Validate
            String validationError = contactService.validateContact(contact);
            if (validationError != null) {
                JOptionPane.showMessageDialog(this, validationError, "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Save
            if (contactToEdit == null) {
                // === SMART DUPLICATE DETECTION ===
                // First, check exact duplicates (hard block)
                if (contactService.existsByName(name)) {
                    JOptionPane.showMessageDialog(this, 
                        "A contact with name '" + name + "' already exists!", 
                        "Duplicate Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (contactService.existsByNumber(number)) {
                    JOptionPane.showMessageDialog(this, 
                        "A contact with number '" + number + "' already exists!", 
                        "Duplicate Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Now check for SIMILAR contacts (fuzzy detection with merge offer)
                List<Contact> similar = contactService.findSimilarContacts(name, number);
                if (!similar.isEmpty()) {
                    // Build a detailed message showing similar contacts
                    StringBuilder msg = new StringBuilder();
                    msg.append("Similar contact(s) found:\n\n");
                    int shown = Math.min(similar.size(), 3);
                    for (int i = 0; i < shown; i++) {
                        Contact s = similar.get(i);
                        msg.append("  \u2022 ").append(s.getName())
                           .append(" | ").append(s.getNumber())
                           .append(s.getEmail() != null && !s.getEmail().isEmpty() ? " | " + s.getEmail() : "")
                           .append(" [").append(s.getCategory()).append("]\n");
                    }
                    if (similar.size() > 3) {
                        msg.append("  ... and ").append(similar.size() - 3).append(" more\n");
                    }
                    msg.append("\nDo you want to:\n");
                    msg.append("  YES  \u2192  Merge (update existing contact)\n");
                    msg.append("  NO   \u2192  Add as new contact anyway\n");
                    msg.append("  CANCEL  \u2192  Go back and edit");

                    int choice = JOptionPane.showConfirmDialog(this,
                        msg.toString(),
                        "Similar Contact Detected",
                        JOptionPane.YES_NO_CANCEL_OPTION,
                        JOptionPane.QUESTION_MESSAGE);

                    if (choice == JOptionPane.YES_OPTION) {
                        // Merge: update the first similar contact with new data
                        Contact target = similar.get(0);
                        target.setName(name);
                        target.setNumber(number);
                        target.setEmail(email);
                        target.setCategory(category);
                        contactService.updateContact(target);
                        Toast.show(getOwner(), "\u2714 Contact merged with " + target.getName() + "!");
                        saveSuccessful = true;
                        dispose();
                        return;
                    } else if (choice == JOptionPane.CANCEL_OPTION) {
                        return; // Go back to editing
                    }
                    // NO = continue and add as new
                }
                
                contactService.addContact(contact);
                Toast.show(getOwner(), "\u2714 Contact added successfully!");
            } else {
                contactService.updateContact(contact);
                Toast.show(getOwner(), "\u2714 Contact updated successfully!");
            }

            saveSuccessful = true;
            dispose();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error saving contact: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSaveSuccessful() {
        return saveSuccessful;
    }
}
