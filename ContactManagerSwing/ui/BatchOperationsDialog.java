package ui;

import service.ContactService;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.List;

/**
 * BatchOperationsDialog - Perform batch operations on multiple selected contacts.
 * 
 * Operations:
 * - Batch Delete (soft delete)
 * - Batch Export to CSV
 * - Batch Update Category
 */
public class BatchOperationsDialog extends JDialog {

    private ContactService contactService;
    private List<Integer> selectedIds;
    private int operationCount;
    private boolean operationPerformed = false;

    private JLabel lblSelectionInfo;
    private JButton btnBatchDelete, btnBatchExport, btnBatchCategory, btnClose;
    private JComboBox<String> cmbCategory;

    private static final String[] CATEGORIES = {"Friends", "Family", "Work", "Emergency"};

    public BatchOperationsDialog(JFrame parent, ContactService contactService, List<Integer> selectedIds) {
        super(parent, "Batch Operations", true);
        this.contactService = contactService;
        this.selectedIds = selectedIds;
        this.operationCount = selectedIds.size();

        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(15, 15));
        setSize(600, 450);

        // Apply theme to dialog background
        getContentPane().setBackground(UITheme.getPanelBackground());
        
        // Position at top-right corner of parent
        positionTopRight();

        // Title panel
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(UITheme.getPanelBackground());
        titlePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        JLabel lblTitle = new JLabel("Batch Operations");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(UITheme.getForeground());
        titlePanel.add(lblTitle, BorderLayout.NORTH);

        lblSelectionInfo = new JLabel(String.format("%d contacts selected for batch operations", operationCount));
        lblSelectionInfo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSelectionInfo.setForeground(UITheme.isDarkMode() ? new Color(180, 180, 180) : new Color(80, 80, 80));
        lblSelectionInfo.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        titlePanel.add(lblSelectionInfo, BorderLayout.CENTER);

        add(titlePanel, BorderLayout.NORTH);

        // Operations panel with scrolling
        JPanel operationsPanel = new JPanel();
        operationsPanel.setLayout(new BoxLayout(operationsPanel, BoxLayout.Y_AXIS));
        operationsPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        operationsPanel.setBackground(UITheme.getPanelBackground());

        // Batch Delete
        JPanel deletePanel = createOperationPanel(
            "Batch Delete",
            "Soft delete all selected contacts (can be restored from Recycle Bin)",
            "Delete All",
            () -> performBatchDelete()
        );
        operationsPanel.add(deletePanel);
        operationsPanel.add(Box.createVerticalStrut(15));

        // Batch Export to CSV
        JPanel exportPanel = createOperationPanel(
            "Export Selected to CSV",
            "Export only the selected contacts to a CSV file",
            "Export",
            () -> performBatchExport()
        );
        operationsPanel.add(exportPanel);
        operationsPanel.add(Box.createVerticalStrut(15));

        // Batch Update Category
        JPanel categoryPanel = createCategoryOperationPanel();
        operationsPanel.add(categoryPanel);

        // Wrap in scroll pane for better UX with many operations
        JScrollPane scrollPane = new JScrollPane(operationsPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        add(scrollPane, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 15));
        buttonPanel.setBackground(UITheme.getPanelBackground());

        btnClose = new JButton("Close");
        btnClose.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnClose.setPreferredSize(new Dimension(100, 35));
        btnClose.addActionListener(e -> dispose());

        buttonPanel.add(btnClose);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    /**
     * Position dialog at top-right corner of parent window.
     */
    private void positionTopRight() {
        try {
            Window parent = getOwner();
            if (parent != null && parent.isVisible()) {
                Point loc = parent.getLocationOnScreen();
                Dimension parentSize = parent.getSize();
                Dimension dialogSize = getSize();
                
                int x = loc.x + parentSize.width - dialogSize.width - 20;
                int y = loc.y + 60;
                
                setLocation(x, y);
            } else {
                setLocationRelativeTo(getParent());
            }
        } catch (Exception e) {
            setLocationRelativeTo(getParent());
        }
    }

    /**
     * Create a standard operation panel with description and action button.
     */
    private JPanel createOperationPanel(String title, String description, String buttonText, Runnable action) {
        Color panelBg = UITheme.getPanelBackground();
        Color borderCol = UITheme.getBorderColor();
        Color descFg = UITheme.isDarkMode() ? new Color(160, 160, 160) : new Color(100, 100, 100);

        JPanel panel = new JPanel(new BorderLayout(10, 5));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(borderCol),
            BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        panel.setBackground(panelBg);

        // Left: Title + Description
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitle.setForeground(UITheme.getForeground());
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblDesc = new JLabel("<html>" + description + "</html>");
        lblDesc.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblDesc.setForeground(descFg);
        lblDesc.setAlignmentX(Component.LEFT_ALIGNMENT);

        textPanel.add(lblTitle);
        textPanel.add(Box.createVerticalStrut(3));
        textPanel.add(lblDesc);

        panel.add(textPanel, BorderLayout.CENTER);

        // Right: Action button
        JButton btnAction = new JButton(buttonText);
        btnAction.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnAction.setPreferredSize(new Dimension(100, 30));
        btnAction.addActionListener(e -> action.run());

        panel.add(btnAction, BorderLayout.EAST);

        return panel;
    }

    /**
     * Create the batch category update panel with dropdown.
     */
    private JPanel createCategoryOperationPanel() {
        Color panelBg = UITheme.getPanelBackground();
        Color borderCol = UITheme.getBorderColor();
        Color descFg = UITheme.isDarkMode() ? new Color(160, 160, 160) : new Color(100, 100, 100);

        JPanel panel = new JPanel(new BorderLayout(10, 5));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(borderCol),
            BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        panel.setBackground(panelBg);

        // Left: Title + Description + Category selector
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Batch Update Category");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitle.setForeground(UITheme.getForeground());
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblDesc = new JLabel("Assign a category to all selected contacts");
        lblDesc.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblDesc.setForeground(descFg);
        lblDesc.setAlignmentX(Component.LEFT_ALIGNMENT);

        textPanel.add(lblTitle);
        textPanel.add(Box.createVerticalStrut(3));
        textPanel.add(lblDesc);
        textPanel.add(Box.createVerticalStrut(8));

        // Category dropdown
        JPanel categoryInputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        categoryInputPanel.setOpaque(false);
        categoryInputPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblCategory = new JLabel("Category: ");
        lblCategory.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblCategory.setForeground(UITheme.getForeground());

        cmbCategory = new JComboBox<>(CATEGORIES);
        cmbCategory.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        cmbCategory.setPreferredSize(new Dimension(120, 26));

        categoryInputPanel.add(lblCategory);
        categoryInputPanel.add(cmbCategory);
        textPanel.add(categoryInputPanel);

        panel.add(textPanel, BorderLayout.CENTER);

        // Right: Action button
        JButton btnUpdate = new JButton("Update All");
        btnUpdate.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnUpdate.setPreferredSize(new Dimension(100, 30));
        btnUpdate.addActionListener(e -> performBatchCategoryUpdate());

        panel.add(btnUpdate, BorderLayout.EAST);

        return panel;
    }

    /**
     * Perform batch soft delete.
     */
    private void performBatchDelete() {
        boolean confirmed = DialogHelper.showConfirm(this,
            String.format("Delete %d selected contacts?\n\n" +
                "Contacts will be moved to Recycle Bin and can be restored.",
                operationCount),
            "Confirm Batch Delete");

        if (!confirmed) {
            return;
        }

        try {
            contactService.batchDelete(selectedIds);
            Toast.show(this, String.format("✔ %d contacts moved to Recycle Bin!", operationCount));
            operationPerformed = true;
            dispose();

        } catch (Exception ex) {
            DialogHelper.showError(this,
                "Error during batch delete:\n" + ex.getMessage(),
                "Batch Delete Error");
        }
    }

    /**
     * Perform batch export to CSV.
     */
    private void performBatchExport() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Selected Contacts to CSV");
        fileChooser.setSelectedFile(new File("selected_contacts.csv"));

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection != JFileChooser.APPROVE_OPTION) {
            return;
        }

        String filePath = fileChooser.getSelectedFile().getAbsolutePath();

        try {
            contactService.exportSelected(filePath, selectedIds);
            Toast.show(this, String.format("✔ %d contacts exported to CSV!", operationCount));
            operationPerformed = true;

        } catch (Exception ex) {
            DialogHelper.showError(this,
                "Error during export:\n" + ex.getMessage(),
                "Export Error");
        }
    }

    /**
     * Perform batch category update.
     */
    private void performBatchCategoryUpdate() {
        String selectedCategory = (String) cmbCategory.getSelectedItem();

        boolean confirmed = DialogHelper.showConfirm(this,
            String.format("Update category to '%s' for %d selected contacts?",
                selectedCategory, operationCount),
            "Confirm Batch Category Update");

        if (!confirmed) {
            return;
        }

        try {
            contactService.batchUpdateCategory(selectedIds, selectedCategory);
            Toast.show(this, String.format("✔ Category updated for %d contacts!", operationCount));
            operationPerformed = true;
            dispose();

        } catch (Exception ex) {
            DialogHelper.showError(this,
                "Error during batch category update:\n" + ex.getMessage(),
                "Batch Update Error");
        }
    }

    /**
     * Check if any operation was performed.
     */
    public boolean isOperationPerformed() {
        return operationPerformed;
    }
}
