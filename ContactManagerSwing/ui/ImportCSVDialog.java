package ui;

import model.ImportResult;
import service.ContactService;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

/**
 * ImportCSVDialog
 * 
 * Professional dialog for CSV import with file selection and preview.
 * Shows import summary after completion.
 */
public class ImportCSVDialog extends JDialog {

    private ContactService contactService;
    private JFrame parent;
    private boolean importSuccessful = false;

    // UI Components
    private JTextField txtFilePath;
    private JButton btnBrowse;
    private JButton btnImport;
    private JButton btnCancel;
    private JTextArea txtPreview;

    private File selectedFile;

    public ImportCSVDialog(JFrame parent, ContactService service) {
        super(parent, "Import Contacts from CSV", true);
        this.contactService = service;
        this.parent = parent;
        
        initComponents();
        
        setSize(600, 450);
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        
        // Apply dark theme to dialog
        if (UITheme.isDarkMode()) {
            getContentPane().setBackground(new Color(50, 50, 50));
        }

        // Top Panel - File Selection
        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        topPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 10, 15));
        if (UITheme.isDarkMode()) {
            topPanel.setBackground(new Color(50, 50, 50));
        }
        
        JLabel lblInfo = new JLabel("<html><b>Select a CSV file to import contacts</b><br>"
            + "Expected format: Name,Number,Email,Category<br>"
            + "Categories: Friends, Family, Work, Emergency (default: Friends)</html>");
        if (UITheme.isDarkMode()) {
            lblInfo.setForeground(Color.WHITE);
        }
        topPanel.add(lblInfo, BorderLayout.NORTH);
        
        JPanel filePanel = new JPanel(new BorderLayout(5, 5));
        filePanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        if (UITheme.isDarkMode()) {
            filePanel.setBackground(new Color(50, 50, 50));
        }
        
        txtFilePath = new JTextField();
        txtFilePath.setEditable(false);
        if (UITheme.isDarkMode()) {
            txtFilePath.setBackground(new Color(43, 43, 43));
            txtFilePath.setForeground(Color.WHITE);
        }
        
        btnBrowse = new JButton("Browse...");
        btnBrowse.setPreferredSize(new Dimension(100, 25));
        btnBrowse.addActionListener(e -> browseFile());
        
        JLabel lblFile = new JLabel("File:");
        if (UITheme.isDarkMode()) {
            lblFile.setForeground(Color.WHITE);
        }
        
        filePanel.add(lblFile, BorderLayout.WEST);
        filePanel.add(txtFilePath, BorderLayout.CENTER);
        filePanel.add(btnBrowse, BorderLayout.EAST);
        topPanel.add(filePanel, BorderLayout.CENTER);
        
        add(topPanel, BorderLayout.NORTH);

        // Center Panel - Preview
        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(0, 15, 10, 15));
        if (UITheme.isDarkMode()) {
            centerPanel.setBackground(new Color(50, 50, 50));
        }
        
        JLabel lblPreview = new JLabel("File Preview (first 10 lines):");
        if (UITheme.isDarkMode()) {
            lblPreview.setForeground(Color.WHITE);
        }
        centerPanel.add(lblPreview, BorderLayout.NORTH);
        
        txtPreview = new JTextArea();
        txtPreview.setEditable(false);
        txtPreview.setFont(new Font("Monospaced", Font.PLAIN, 12));
        if (UITheme.isDarkMode()) {
            txtPreview.setBackground(new Color(43, 43, 43));
            txtPreview.setForeground(Color.WHITE);
        } else {
            txtPreview.setBackground(new Color(245, 245, 245));
        }
        JScrollPane scrollPane = new JScrollPane(txtPreview);
        scrollPane.setPreferredSize(new Dimension(550, 250));
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
        
        btnImport = new JButton("Import");
        btnImport.setPreferredSize(new Dimension(100, 30));
        btnImport.setEnabled(false);
        btnImport.addActionListener(e -> importCSV());
        
        btnCancel = new JButton("Cancel");
        btnCancel.setPreferredSize(new Dimension(100, 30));
        btnCancel.addActionListener(e -> dispose());
        
        buttonPanel.add(btnImport);
        buttonPanel.add(btnCancel);
        add(buttonPanel, BorderLayout.SOUTH);

        // Escape key to cancel
        KeyStroke escapeKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        getRootPane().registerKeyboardAction(e -> dispose(), escapeKeyStroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private void browseFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select CSV File to Import");
        FileNameExtensionFilter filter = new FileNameExtensionFilter("CSV Files (*.csv)", "csv");
        fileChooser.setFileFilter(filter);

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            selectedFile = fileChooser.getSelectedFile();
            txtFilePath.setText(selectedFile.getAbsolutePath());
            previewFile();
            btnImport.setEnabled(true);
        }
    }

    private void previewFile() {
        if (selectedFile == null || !selectedFile.exists()) {
            txtPreview.setText("File not found!");
            return;
        }

        try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(selectedFile))) {
            StringBuilder preview = new StringBuilder();
            String line;
            int lineCount = 0;
            
            while ((line = reader.readLine()) != null && lineCount < 10) {
                preview.append(line).append("\n");
                lineCount++;
            }
            
            if (lineCount == 0) {
                txtPreview.setText("File is empty!");
            } else {
                txtPreview.setText(preview.toString());
            }
            
        } catch (Exception e) {
            txtPreview.setText("Error reading file: " + e.getMessage());
            btnImport.setEnabled(false);
        }
    }

    private void importCSV() {
        if (selectedFile == null) {
            JOptionPane.showMessageDialog(this, "Please select a file first!", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Confirm before import
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to import contacts from this file?\n" +
            "Duplicates will be automatically skipped.", 
            "Confirm Import", 
            JOptionPane.YES_NO_OPTION, 
            JOptionPane.QUESTION_MESSAGE);
        
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            // Show progress dialog
            btnImport.setEnabled(false);
            btnBrowse.setEnabled(false);
            btnCancel.setEnabled(false);
            txtPreview.setText("Importing... Please wait...");

            // Import contacts
            ImportResult result = contactService.importFromCSV(selectedFile.getAbsolutePath());

            // Show results
            String message = String.format(
                "Import Completed!\n\n" +
                "Imported: %d\n" +
                "Duplicates Skipped: %d\n" +
                "Invalid Rows: %d\n" +
                "Total Processed: %d",
                result.getImported(),
                result.getDuplicatesSkipped(),
                result.getInvalidRows(),
                result.getTotal()
            );

            JOptionPane.showMessageDialog(this, message, "Import Results", 
                result.getImported() > 0 ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE);

            importSuccessful = true;
            dispose();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error importing CSV: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
            
            btnImport.setEnabled(true);
            btnBrowse.setEnabled(true);
            btnCancel.setEnabled(true);
        }
    }

    public boolean isImportSuccessful() {
        return importSuccessful;
    }
}
