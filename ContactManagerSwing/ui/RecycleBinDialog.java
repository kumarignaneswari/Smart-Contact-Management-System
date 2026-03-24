package ui;

import model.Contact;
import service.ContactService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 * RecycleBinDialog
 * 
 * Professional separate window for viewing and managing deleted contacts.
 * Provides restore, permanent delete, and purge all functionality.
 */
public class RecycleBinDialog extends JDialog {

    private ContactService contactService;
    private JTable deletedTable;
    private DefaultTableModel tableModel;
    private JButton btnRestore;
    private JButton btnPermanentDelete;
    private JButton btnPurgeAll;
    private JButton btnClose;
    private JLabel lblCount;

    public RecycleBinDialog(JFrame parent, ContactService service) {
        super(parent, "Recycle Bin - Deleted Contacts", true);
        this.contactService = service;
        
        initComponents();
        loadDeletedContacts();
        
        setSize(800, 500);
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        
        // Apply dark theme to dialog
        if (UITheme.isDarkMode()) {
            getContentPane().setBackground(new Color(50, 50, 50));
        }

        // Top Panel - Info
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        topPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        if (UITheme.isDarkMode()) {
            topPanel.setBackground(new Color(50, 50, 50));
        }
        
        lblCount = new JLabel("Deleted contacts: 0");
        lblCount.setFont(new Font("Arial", Font.BOLD, 12));
        if (UITheme.isDarkMode()) {
            lblCount.setForeground(Color.WHITE);
        }
        topPanel.add(lblCount);
        add(topPanel, BorderLayout.NORTH);

        // Center Panel - Table
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        if (UITheme.isDarkMode()) {
            centerPanel.setBackground(new Color(50, 50, 50));
        }
        
        String[] columns = {"ID", "Name", "Number", "Email", "Category", "Deleted At"};
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        deletedTable = new JTable(tableModel);
        deletedTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        deletedTable.getTableHeader().setReorderingAllowed(false);
        
        // Apply dark theme to table
        if (UITheme.isDarkMode()) {
            deletedTable.setBackground(new Color(43, 43, 43));
            deletedTable.setForeground(Color.WHITE);
            deletedTable.setGridColor(new Color(70, 70, 70));
            deletedTable.setSelectionBackground(new Color(75, 110, 175));
            deletedTable.setSelectionForeground(Color.WHITE);
            deletedTable.getTableHeader().setBackground(new Color(55, 55, 55));
            deletedTable.getTableHeader().setForeground(Color.WHITE);
            deletedTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        }
        
        // Add double-click listener for quick restore
        deletedTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    restoreContact();
                }
            }
        });
        
        // Enable/disable buttons based on selection
        deletedTable.getSelectionModel().addListSelectionListener(e -> {
            boolean hasSelection = deletedTable.getSelectedRow() != -1;
            btnRestore.setEnabled(hasSelection);
            btnPermanentDelete.setEnabled(hasSelection);
        });
        
        JScrollPane scrollPane = new JScrollPane(deletedTable);
        if (UITheme.isDarkMode()) {
            scrollPane.getViewport().setBackground(new Color(43, 43, 43));
        }
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // Bottom Panel - Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        if (UITheme.isDarkMode()) {
            buttonPanel.setBackground(new Color(50, 50, 50));
        }
        
        btnRestore = new JButton("♻ Restore");
        btnRestore.setPreferredSize(new Dimension(120, 35));
        btnRestore.setToolTipText("Restore selected contact");
        btnRestore.setEnabled(false);
        btnRestore.addActionListener(e -> restoreContact());
        
        btnPermanentDelete = new JButton("Delete Forever");
        btnPermanentDelete.setPreferredSize(new Dimension(140, 35));
        btnPermanentDelete.setToolTipText("Permanently delete selected contact");
        btnPermanentDelete.setEnabled(false);
        btnPermanentDelete.addActionListener(e -> permanentDelete());
        
        btnPurgeAll = new JButton("⚠ Purge All");
        btnPurgeAll.setPreferredSize(new Dimension(120, 35));
        btnPurgeAll.setToolTipText("Permanently delete all contacts in recycle bin");
        btnPurgeAll.addActionListener(e -> purgeAll());
        
        btnClose = new JButton("Close");
        btnClose.setPreferredSize(new Dimension(100, 35));
        btnClose.addActionListener(e -> dispose());
        
        buttonPanel.add(btnRestore);
        buttonPanel.add(btnPermanentDelete);
        buttonPanel.add(btnPurgeAll);
        buttonPanel.add(btnClose);
        add(buttonPanel, BorderLayout.SOUTH);

        // Escape key to close
        KeyStroke escapeKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        getRootPane().registerKeyboardAction(e -> dispose(), escapeKeyStroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private void loadDeletedContacts() {
        tableModel.setRowCount(0);
        List<Contact> deletedContacts = contactService.getDeletedContacts();
        
        for (Contact c : deletedContacts) {
            Object[] row = {
                c.getId(),
                c.getName(),
                c.getNumber(),
                c.getEmail() != null ? c.getEmail() : "",
                c.getCategory(),
                c.getUpdatedAt() // Deleted timestamp stored in updated_at
            };
            tableModel.addRow(row);
        }
        
        lblCount.setText("Deleted contacts: " + deletedContacts.size());
        btnPurgeAll.setEnabled(deletedContacts.size() > 0);
    }

    private void restoreContact() {
        int selectedRow = deletedTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a contact to restore!", 
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = (int) tableModel.getValueAt(selectedRow, 0);
        String name = (String) tableModel.getValueAt(selectedRow, 1);

        int confirm = JOptionPane.showConfirmDialog(this, 
            "Restore contact: " + name + "?", 
            "Confirm Restore", 
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                contactService.restoreContact(id);
                Toast.show(this, "✔ Contact restored!");
                loadDeletedContacts();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error restoring contact: " + e.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void permanentDelete() {
        int selectedRow = deletedTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a contact to delete!", 
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = (int) tableModel.getValueAt(selectedRow, 0);
        String name = (String) tableModel.getValueAt(selectedRow, 1);

        int confirm = JOptionPane.showConfirmDialog(this, 
            "⚠ WARNING: Permanently delete contact: " + name + "?\n" +
            "This action CANNOT be undone!", 
            "Confirm Permanent Delete", 
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                contactService.deleteById(id);
                Toast.show(this, "✔ Contact permanently deleted!");
                loadDeletedContacts();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error deleting contact: " + e.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void purgeAll() {
        List<Contact> deletedContacts = contactService.getDeletedContacts();
        if (deletedContacts.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No deleted contacts to purge!", 
                "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, 
            "⚠ WARNING: Permanently delete ALL " + deletedContacts.size() + " contacts?\n" +
            "This action CANNOT be undone!", 
            "Confirm Purge All", 
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                contactService.purgeDeletedContacts();
                Toast.show(this, "✔ All deleted contacts purged!");
                loadDeletedContacts();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error purging contacts: " + e.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
