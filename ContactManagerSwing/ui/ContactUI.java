package ui;

import model.Contact;
import model.User;
import service.ContactService;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.datatransfer.*;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Contact Management Dashboard UI - Modern CRM Style
 * 
 * Features:
 * - Statistics dashboard panel (collapsible)
 * - Contact preview side panel with avatar
 * - Toast notifications (replaces JOptionPane success messages)
 * - Table sorting with click-to-sort column headers
 * - Empty state screen
 * - Column visibility toggle (View menu)
 * - Right-click context menu
 * - Smart search suggestions
 * - Accent color system (Blue / Green / Purple)
 * - Animated row highlight on add/edit/restore
 * - Dark / Light theme support
 */
public class ContactUI extends JFrame {

    private ContactService contactService;
    private User currentUser;   // NEW: logged-in user

    // ===== Core UI Components =====
    private JTextField txtSearch;
    private JComboBox<String> cmbFilterCategory;
    private JTable contactTable;
    private DefaultTableModel tableModel;
    private JButton btnAdd, btnUpdate, btnDelete, btnRefresh;
    private JButton btnRecycleBin, btnExport, btnImport, btnThemeToggle, btnLogout;
    private JButton btnBatchOps;  // Batch Operations
    private JLabel lblStatus;
    private JToolBar toolBar;
    private JPanel searchPanel;

    // ===== New Dashboard Components =====
    private StatisticsPanel statisticsPanel;
    private ContactPreviewPanel previewPanel;
    private TableRowSorter<DefaultTableModel> tableSorter;
    private JPanel emptyStatePanel;
    private JPanel tableCardPanel;
    private CardLayout tableCardLayout;
    private JPopupMenu searchSuggestionPopup;
    private JSplitPane mainSplitPane;

    // ===== Advanced Search Components =====
    private JCheckBox chkIncludeDeleted;
    private JLabel lblResultCount;
    private JButton btnClearSearch;
    private Timer debounceTimer;
    private String currentSearchKeyword = "";
    private static final int DEBOUNCE_DELAY = 300; // milliseconds

    // ===== Column Visibility =====
    private LinkedHashMap<String, TableColumn> savedColumns = new LinkedHashMap<>();
    private LinkedHashMap<String, Boolean> columnVisibility = new LinkedHashMap<>();
    private JCheckBoxMenuItem chkEmail, chkCategory, chkCreatedAt;

    // ===== Row Highlight =====
    private int highlightedViewRow = -1;
    private Color highlightColor = null;

    private int selectedContactId = -1;

    private static final String[] CATEGORIES = {"Friends", "Family", "Work", "Emergency"};
    private static final String[] COLUMN_NAMES = {"ID", "Name", "Number", "Email", "Category", "Created At"};

    public ContactUI(User user) {
        this.currentUser = user;
        this.contactService = new ContactService();
        this.contactService.setCurrentUserId(user.getId());  // Scope all queries to this user

        // Apply default light theme
        UITheme.applyLightTheme();

        initComponents();
        loadAllContacts();
        refreshStatistics();
        updateStatusBar();
        updateEmptyState();

        setupKeyboardShortcuts();
    }

    // ==================== INITIALIZATION ====================

    private void initComponents() {
        setTitle("Contact Management System - " + currentUser.getUsername());
        setSize(1300, 780);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(0, 0));

        UITheme.applyThemeToFrame(this);

        // Menu Bar
        setJMenuBar(createMenuBar());

        // Top Container: ToolBar + Statistics + Search
        JPanel topContainer = new JPanel(new BorderLayout(0, 0));
        toolBar = createToolBar();
        topContainer.add(toolBar, BorderLayout.NORTH);

        statisticsPanel = new StatisticsPanel(contactService);
        topContainer.add(statisticsPanel, BorderLayout.CENTER);

        searchPanel = createSearchPanel();
        topContainer.add(searchPanel, BorderLayout.SOUTH);
        add(topContainer, BorderLayout.NORTH);

        // Center: Split Pane with Table + Preview
        JPanel tablePanel = createTablePanel();
        previewPanel = new ContactPreviewPanel(this);

        mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tablePanel, previewPanel);
        mainSplitPane.setResizeWeight(1.0);
        mainSplitPane.setDividerSize(5);
        mainSplitPane.setOneTouchExpandable(true);
        add(mainSplitPane, BorderLayout.CENTER);

        // Status Bar — professional panel with user info + status text
        JPanel statusBarPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                Color bg = UITheme.isDarkMode() ? new Color(40, 42, 54) : new Color(248, 249, 253);
                g2.setColor(bg);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        statusBarPanel.setOpaque(false);
        statusBarPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0,
                UITheme.isDarkMode() ? new Color(58, 58, 72) : new Color(210, 210, 228)),
            BorderFactory.createEmptyBorder(4, 12, 4, 12)));

        lblStatus = new JLabel("Ready  \u2022  Select a contact to get started");
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblStatus.setForeground(UITheme.isDarkMode() ? new Color(160, 165, 190) : new Color(90, 100, 130));
        statusBarPanel.add(lblStatus, BorderLayout.CENTER);

        // User badge on right side of status bar
        JLabel userBadge = new JLabel("  \u25CF  " + currentUser.getUsername() + "  ");
        userBadge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        userBadge.setForeground(UITheme.getAccentColor());
        statusBarPanel.add(userBadge, BorderLayout.EAST);

        add(statusBarPanel, BorderLayout.PAGE_END);
    }

    // ==================== MENU BAR ====================

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // View Menu
        JMenu viewMenu = new JMenu("View");

        // Columns submenu
        JMenu columnsMenu = new JMenu("Columns");
        chkEmail = new JCheckBoxMenuItem("Email", true);
        chkEmail.addActionListener(e -> toggleColumn("Email", chkEmail.isSelected()));
        chkCategory = new JCheckBoxMenuItem("Category", true);
        chkCategory.addActionListener(e -> toggleColumn("Category", chkCategory.isSelected()));
        chkCreatedAt = new JCheckBoxMenuItem("Created At", true);
        chkCreatedAt.addActionListener(e -> toggleColumn("Created At", chkCreatedAt.isSelected()));
        columnsMenu.add(chkEmail);
        columnsMenu.add(chkCategory);
        columnsMenu.add(chkCreatedAt);
        viewMenu.add(columnsMenu);

        viewMenu.addSeparator();

        // Accent color submenu
        JMenu accentMenu = new JMenu("Accent Color");
        ButtonGroup accentGroup = new ButtonGroup();
        JRadioButtonMenuItem blueItem = new JRadioButtonMenuItem("Blue", true);
        blueItem.addActionListener(e -> updateAccentColor(UITheme.ACCENT_BLUE));
        JRadioButtonMenuItem greenItem = new JRadioButtonMenuItem("Green");
        greenItem.addActionListener(e -> updateAccentColor(UITheme.ACCENT_GREEN));
        JRadioButtonMenuItem purpleItem = new JRadioButtonMenuItem("Purple");
        purpleItem.addActionListener(e -> updateAccentColor(UITheme.ACCENT_PURPLE));
        accentGroup.add(blueItem);
        accentGroup.add(greenItem);
        accentGroup.add(purpleItem);
        accentMenu.add(blueItem);
        accentMenu.add(greenItem);
        accentMenu.add(purpleItem);
        viewMenu.add(accentMenu);

        menuBar.add(viewMenu);
        return menuBar;
    }

    // ==================== TOOLBAR ====================

    private JToolBar createToolBar() {
        JToolBar toolBar = new JToolBar("Main Actions") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c1 = UITheme.isDarkMode() ? new Color(50, 52, 65) : new Color(255, 255, 255);
                Color c2 = UITheme.isDarkMode() ? new Color(42, 44, 56) : new Color(245, 246, 252);
                GradientPaint gp = new GradientPaint(0, 0, c1, 0, getHeight(), c2);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        toolBar.setOpaque(false);
        toolBar.setFloatable(false);
        toolBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0,
                UITheme.isDarkMode() ? new Color(60, 60, 75) : new Color(210, 210, 228)),
            BorderFactory.createEmptyBorder(7, 10, 7, 10)));

        Dimension btnSize = new Dimension(95, 34);

        // Add Button
        btnAdd = createToolBarButton("Add", IconFactory.getAddIcon(), "Add new contact (Ctrl+N)");
        btnAdd.setPreferredSize(btnSize);
        btnAdd.addActionListener(e -> openAddDialog());
        toolBar.add(btnAdd);

        toolBar.add(Box.createHorizontalStrut(3));

        // Edit Button
        btnUpdate = createToolBarButton("Edit", IconFactory.getEditIcon(), "Edit selected contact (Double-click row)");
        btnUpdate.setPreferredSize(btnSize);
        btnUpdate.setEnabled(false);
        btnUpdate.addActionListener(e -> openEditDialog());
        toolBar.add(btnUpdate);

        toolBar.add(Box.createHorizontalStrut(3));

        // Delete Button
        btnDelete = createToolBarButton("Delete", IconFactory.getDeleteIcon(), "Soft delete selected contact (Del)");
        btnDelete.setPreferredSize(btnSize);
        btnDelete.setEnabled(false);
        btnDelete.addActionListener(e -> deleteContact());
        toolBar.add(btnDelete);

        toolBar.addSeparator(new Dimension(10, 32));

        // Recycle Bin Button
        btnRecycleBin = createToolBarButton("Recycle", IconFactory.getRecycleIcon(), "View and restore deleted contacts");
        btnRecycleBin.setPreferredSize(new Dimension(100, 32));
        btnRecycleBin.addActionListener(e -> openRecycleBin());
        toolBar.add(btnRecycleBin);

        toolBar.addSeparator(new Dimension(10, 32));

        // Import Button
        btnImport = createToolBarButton("Import", IconFactory.getImportIcon(), "Import contacts from CSV file");
        btnImport.setPreferredSize(btnSize);
        btnImport.addActionListener(e -> openImportDialog());
        toolBar.add(btnImport);

        toolBar.add(Box.createHorizontalStrut(3));

        // Export Button
        btnExport = createToolBarButton("Export", IconFactory.getExportIcon(), "Export contacts to CSV file");
        btnExport.setPreferredSize(btnSize);
        btnExport.addActionListener(e -> exportToCSV());
        toolBar.add(btnExport);

        toolBar.addSeparator(new Dimension(10, 32));

        // Batch Operations Button
        btnBatchOps = createToolBarButton("Batch", IconFactory.getBatchIcon(), "Batch operations on selected contacts");
        btnBatchOps.setPreferredSize(btnSize);
        btnBatchOps.setEnabled(false);  // Enabled when multiple rows selected
        btnBatchOps.addActionListener(e -> openBatchOperationsDialog());
        toolBar.add(btnBatchOps);

        toolBar.addSeparator(new Dimension(10, 32));

        // Refresh Button
        btnRefresh = createToolBarButton("Refresh", IconFactory.getRefreshIcon(), "Reload all contacts (F5)");
        btnRefresh.setPreferredSize(btnSize);
        btnRefresh.addActionListener(e -> {
            loadAllContacts();
            refreshStatistics();
            updateStatusBar();
        });
        toolBar.add(btnRefresh);

        // Push right section to the end
        toolBar.add(Box.createHorizontalGlue());

        // Accent color quick selector
        toolBar.addSeparator(new Dimension(10, 32));
        JLabel accentLabel = new JLabel("Accent:");
        accentLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        toolBar.add(accentLabel);
        toolBar.add(Box.createHorizontalStrut(4));

        JComboBox<String> accentCombo = new JComboBox<>(new String[]{"Blue", "Green", "Purple"});
        accentCombo.setPreferredSize(new Dimension(80, 28));
        accentCombo.setMaximumSize(new Dimension(80, 28));
        accentCombo.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        accentCombo.addActionListener(e -> {
            String selected = (String) accentCombo.getSelectedItem();
            if (selected == null) return;
            switch (selected) {
                case "Blue":   updateAccentColor(UITheme.ACCENT_BLUE);   break;
                case "Green":  updateAccentColor(UITheme.ACCENT_GREEN);  break;
                case "Purple": updateAccentColor(UITheme.ACCENT_PURPLE); break;
            }
        });
        toolBar.add(accentCombo);

        toolBar.addSeparator(new Dimension(10, 32));

        // Theme Toggle Button
        btnThemeToggle = createToolBarButton("Theme", IconFactory.getThemeIcon(), "Toggle Dark/Light theme");
        btnThemeToggle.setPreferredSize(new Dimension(95, 32));
        btnThemeToggle.addActionListener(e -> toggleTheme());
        toolBar.add(btnThemeToggle);

        toolBar.addSeparator(new Dimension(10, 32));

        // Logout Button
        btnLogout = new JButton("Logout") {
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
                Color base = new Color(210, 50, 50);
                Color top  = hovered ? base.brighter() : base;
                Color bot  = hovered ? base : base.darker();
                GradientPaint gp = new GradientPaint(0, 0, top, 0, getHeight(), bot);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnLogout.setPreferredSize(new Dimension(85, 32));
        btnLogout.setOpaque(false);
        btnLogout.setContentAreaFilled(false);
        btnLogout.setBorderPainted(false);
        btnLogout.setFocusable(false);
        btnLogout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLogout.setToolTipText("Logout and return to login screen");
        btnLogout.addActionListener(e -> doLogout());
        toolBar.add(btnLogout);

        return toolBar;
    }

    /**
     * Creates a professional toolbar button with rounded corners and hover gradient.
     */
    private JButton createToolBarButton(String text, ImageIcon icon, String tooltip) {
        JButton button = new JButton(text, icon) {
            private boolean hovered = false;

            {
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { if (isEnabled()) { hovered = true;  repaint(); } }
                    @Override public void mouseExited(MouseEvent e)  { hovered = false; repaint(); }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (getModel().isPressed()) {
                    g2.setColor(UITheme.isDarkMode() ? new Color(55, 90, 160) : new Color(UITheme.getAccentColor().getRed(),
                        UITheme.getAccentColor().getGreen(), UITheme.getAccentColor().getBlue(), 70));
                    g2.fillRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 8, 8);
                } else if (hovered && isEnabled()) {
                    Color ac = UITheme.getAccentColor();
                    g2.setColor(new Color(ac.getRed(), ac.getGreen(), ac.getBlue(), 35));
                    g2.fillRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 8, 8);
                    g2.setColor(new Color(ac.getRed(), ac.getGreen(), ac.getBlue(), 90));
                    g2.setStroke(new BasicStroke(1.2f));
                    g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 8, 8);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        button.setFocusable(false);
        button.setToolTipText(tooltip);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        button.setHorizontalTextPosition(SwingConstants.RIGHT);
        button.setIconTextGap(6);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setOpaque(false);
        return button;
    }

    // ==================== SEARCH PANEL ====================

    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        Color borderColor = UITheme.isDarkMode() ? new Color(80, 80, 80) : new Color(200, 200, 200);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, borderColor),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));

        if (UITheme.isDarkMode()) {
            panel.setBackground(new Color(50, 50, 50));
        }

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Search label
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        JLabel lblSearch = new JLabel("Search:", IconRenderer.createSmallIcon(IconRenderer.IconType.SEARCH), JLabel.LEFT);
        lblSearch.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        if (UITheme.isDarkMode()) lblSearch.setForeground(Color.WHITE);
        panel.add(lblSearch, gbc);

        // Search field with clear button overlay
        gbc.gridx = 1; gbc.weightx = 1.0;
        JPanel searchFieldPanel = new JPanel(new BorderLayout());
        searchFieldPanel.setOpaque(false);
        txtSearch = new JTextField(30);
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtSearch.setPreferredSize(new Dimension(200, 28));
        txtSearch.setToolTipText("Search across Name, Phone, Email, Category (live search)");
        searchFieldPanel.add(txtSearch, BorderLayout.CENTER);

        // Clear search button (X icon)
        btnClearSearch = new JButton("\u2716");
        btnClearSearch.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        btnClearSearch.setPreferredSize(new Dimension(28, 28));
        btnClearSearch.setMargin(new Insets(0, 0, 0, 0));
        btnClearSearch.setFocusable(false);
        btnClearSearch.setToolTipText("Clear search");
        btnClearSearch.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnClearSearch.setVisible(false);
        btnClearSearch.addActionListener(e -> {
            txtSearch.setText("");
            currentSearchKeyword = "";
            btnClearSearch.setVisible(false);
            performSearch();
            txtSearch.requestFocusInWindow();
        });
        searchFieldPanel.add(btnClearSearch, BorderLayout.EAST);
        panel.add(searchFieldPanel, gbc);

        // Category filter label
        gbc.gridx = 2; gbc.weightx = 0;
        JLabel lblCategory = new JLabel("Category:", IconRenderer.createSmallIcon(IconRenderer.IconType.CATEGORY), JLabel.LEFT);
        lblCategory.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        if (UITheme.isDarkMode()) lblCategory.setForeground(Color.WHITE);
        panel.add(lblCategory, gbc);

        // Category dropdown
        gbc.gridx = 3; gbc.weightx = 0.3;
        String[] filterOptions = new String[CATEGORIES.length + 1];
        filterOptions[0] = "All";
        System.arraycopy(CATEGORIES, 0, filterOptions, 1, CATEGORIES.length);
        cmbFilterCategory = new JComboBox<>(filterOptions);
        cmbFilterCategory.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cmbFilterCategory.setPreferredSize(new Dimension(120, 28));
        cmbFilterCategory.setToolTipText("Filter by category");
        cmbFilterCategory.addActionListener(e -> performSearch());
        panel.add(cmbFilterCategory, gbc);

        // Include Deleted checkbox
        gbc.gridx = 4; gbc.weightx = 0;
        chkIncludeDeleted = new JCheckBox("Include Deleted");
        chkIncludeDeleted.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        chkIncludeDeleted.setToolTipText("Show soft-deleted contacts in results");
        chkIncludeDeleted.setOpaque(false);
        if (UITheme.isDarkMode()) chkIncludeDeleted.setForeground(Color.WHITE);
        chkIncludeDeleted.addActionListener(e -> performSearch());
        panel.add(chkIncludeDeleted, gbc);

        // Result count label
        gbc.gridx = 5; gbc.weightx = 0;
        lblResultCount = new JLabel("");
        lblResultCount.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblResultCount.setForeground(UITheme.getAccentColor());
        panel.add(lblResultCount, gbc);

        // Smart search suggestions popup
        searchSuggestionPopup = new JPopupMenu();
        searchSuggestionPopup.setFocusable(false);

        // === LIVE SEARCH with DocumentListener + Debounce ===
        debounceTimer = new Timer(DEBOUNCE_DELAY, e -> {
            String keyword = txtSearch.getText().trim();
            currentSearchKeyword = keyword;
            btnClearSearch.setVisible(!keyword.isEmpty());
            performSearch();
            if (!keyword.isEmpty() && keyword.length() >= 1) {
                showSearchSuggestions(keyword);
            } else {
                searchSuggestionPopup.setVisible(false);
            }
        });
        debounceTimer.setRepeats(false);

        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { restartDebounce(); }
            public void removeUpdate(DocumentEvent e) { restartDebounce(); }
            public void changedUpdate(DocumentEvent e) { restartDebounce(); }
            private void restartDebounce() {
                debounceTimer.restart();
            }
        });

        // Escape to dismiss suggestions
        txtSearch.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    searchSuggestionPopup.setVisible(false);
                }
            }
        });

        // Dismiss suggestions on focus lost
        txtSearch.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                Timer t = new Timer(200, evt -> {
                    if (!txtSearch.hasFocus()) searchSuggestionPopup.setVisible(false);
                });
                t.setRepeats(false);
                t.start();
            }
        });

        return panel;
    }

    // ==================== TABLE PANEL ====================

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        if (UITheme.isDarkMode()) {
            panel.setBackground(new Color(50, 50, 50));
        }

        // Table model with proper column types
        tableModel = new DefaultTableModel(COLUMN_NAMES, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            public Class<?> getColumnClass(int column) {
                if (column == 0) return Integer.class; // ID - sort numerically
                return String.class;
            }
        };

        // Create table
        contactTable = new JTable(tableModel);
        contactTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);  // CHANGED: Allow multi-selection
        contactTable.getTableHeader().setReorderingAllowed(false);
        contactTable.setRowHeight(28);
        contactTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        contactTable.setShowHorizontalLines(true);
        contactTable.setShowVerticalLines(false);
        contactTable.setIntercellSpacing(new Dimension(0, 1));

        // Row sorter - enables click-to-sort on column headers
        tableSorter = new TableRowSorter<>(tableModel);
        contactTable.setRowSorter(tableSorter);

        // Custom renderer for row highlighting + alternating rows
        DefaultTableCellRenderer customRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected && row == highlightedViewRow && highlightColor != null) {
                    c.setBackground(highlightColor);
                    c.setForeground(UITheme.getForeground());
                } else if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? UITheme.getBackground() : getAlternateRowColor());
                    c.setForeground(UITheme.getForeground());
                }
                ((JLabel) c).setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return c;
            }
        };
        contactTable.setDefaultRenderer(Object.class, customRenderer);
        contactTable.setDefaultRenderer(Integer.class, customRenderer);

        // Table header styling
        JTableHeader header = contactTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setPreferredSize(new Dimension(header.getWidth(), 34));

        UITheme.applyThemeToTable(contactTable);

        // Save column references for visibility toggle
        for (int i = 0; i < COLUMN_NAMES.length; i++) {
            TableColumn col = contactTable.getColumnModel().getColumn(i);
            savedColumns.put(COLUMN_NAMES[i], col);
            columnVisibility.put(COLUMN_NAMES[i], true);
        }

        // Set column widths
        contactTable.getColumnModel().getColumn(0).setPreferredWidth(50);   // ID
        contactTable.getColumnModel().getColumn(1).setPreferredWidth(130);  // Name
        contactTable.getColumnModel().getColumn(2).setPreferredWidth(120);  // Number
        contactTable.getColumnModel().getColumn(3).setPreferredWidth(170);  // Email
        contactTable.getColumnModel().getColumn(4).setPreferredWidth(90);   // Category
        contactTable.getColumnModel().getColumn(5).setPreferredWidth(150);  // Created At

        // Selection listener - update buttons + preview panel
        // UPDATED: Handle both single and multi-selection
        contactTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectionCount = contactTable.getSelectedRowCount();
                
                if (selectionCount == 0) {
                    // Nothing selected
                    selectedContactId = -1;
                    btnUpdate.setEnabled(false);
                    btnDelete.setEnabled(false);
                    btnBatchOps.setEnabled(false);
                    previewPanel.clearPreview();
                    updateStatusBar();
                    
                } else if (selectionCount == 1) {
                    // Single selection - enable edit/delete, disable batch
                    int viewRow = contactTable.getSelectedRow();
                    int modelRow = contactTable.convertRowIndexToModel(viewRow);
                    selectedContactId = (int) tableModel.getValueAt(modelRow, 0);
                    btnUpdate.setEnabled(true);
                    btnDelete.setEnabled(true);
                    btnBatchOps.setEnabled(false);

                    String name = (String) tableModel.getValueAt(modelRow, 1);
                    lblStatus.setText("Selected: " + name + " | Double-click to edit");

                    // Update preview panel
                    Contact contact = contactService.getById(selectedContactId);
                    if (contact != null) {
                        previewPanel.showContact(contact);
                    }
                } else {
                    // Multiple selection - disable edit, enable delete & batch
                    selectedContactId = -1;
                    btnUpdate.setEnabled(false);  // Can't edit multiple at once
                    btnDelete.setEnabled(true);   // Can batch delete
                    btnBatchOps.setEnabled(true); // Enable batch operations
                    previewPanel.clearPreview();
                    lblStatus.setText(String.format("✔️ %d contacts selected | Use Batch Operations", selectionCount));
                }
            }
        });

        // Double-click to edit + right-click context menu
        contactTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && contactTable.getSelectedRow() != -1) {
                    openEditDialog();
                }
            }
            public void mousePressed(MouseEvent e) { handleContextMenu(e); }
            public void mouseReleased(MouseEvent e) { handleContextMenu(e); }
        });

        // Table in scroll pane
        JScrollPane scrollPane = new JScrollPane(contactTable);
        Color borderColor = UITheme.isDarkMode() ? new Color(80, 80, 80) : new Color(200, 200, 200);
        scrollPane.setBorder(BorderFactory.createLineBorder(borderColor));

        if (UITheme.isDarkMode()) {
            scrollPane.getViewport().setBackground(new Color(43, 43, 43));
        }

        // Empty state panel
        emptyStatePanel = createEmptyStatePanel();

        // Card layout to switch between table and empty state
        tableCardLayout = new CardLayout();
        tableCardPanel = new JPanel(tableCardLayout);
        tableCardPanel.add(scrollPane, "TABLE");
        tableCardPanel.add(emptyStatePanel, "EMPTY");

        panel.add(tableCardPanel, BorderLayout.CENTER);
        return panel;
    }

    /**
     * Creates the empty state panel shown when no contacts exist.
     */
    private JPanel createEmptyStatePanel() {
        JPanel panel = new JPanel(new GridBagLayout());

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);

        JLabel iconLabel = new JLabel(createEmptyStateIcon(64));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(iconLabel);
        content.add(Box.createVerticalStrut(12));

        JLabel titleLabel = new JLabel("No contacts yet.");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(titleLabel);
        content.add(Box.createVerticalStrut(8));

        JLabel subtitleLabel = new JLabel("Click + Add to create one.");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(130, 130, 130));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(subtitleLabel);

        panel.add(content);
        return panel;
    }

    /**
     * Draws a vector "person with contact card" icon for the empty state.
     * Reliably renders on all Java Swing platforms — no emoji/font issues.
     */
    private ImageIcon createEmptyStateIcon(int size) {
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(
                size, size, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color accent = UITheme.getAccentColor();
        Color soft   = new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 160);
        Color fill   = UITheme.isDarkMode() ? new Color(200, 205, 225) : new Color(90, 110, 160);

        int cx = size / 2;
        int headR = size / 6;

        // Head circle
        g2.setColor(fill);
        g2.fillOval(cx - headR, size / 5, headR * 2, headR * 2);

        // Body arc (shoulders)
        g2.setStroke(new BasicStroke(size / 10f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawArc(size / 8, size / 5 + headR * 2, 3 * size / 4, size / 3, 0, 180);

        // Card/list lines below person (simulating a contact card)
        g2.setStroke(new BasicStroke(size / 16f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(soft);
        int cardTop = size * 57 / 100;
        int cardLeft = size / 6;
        int cardRight = 5 * size / 6;
        // 3 horizontal lines representing contact details
        for (int row = 0; row < 3; row++) {
            int y = cardTop + row * (size / 9);
            int lineRight = (row == 2) ? cardLeft + (cardRight - cardLeft) * 2 / 3 : cardRight;
            g2.drawLine(cardLeft, y, lineRight, y);
        }

        g2.dispose();
        return new ImageIcon(img);
    }

    /**
     * Alternate row color for striped table look.
     */
    private Color getAlternateRowColor() {
        if (UITheme.isDarkMode()) {
            return new Color(48, 48, 48);
        }
        return new Color(248, 249, 250);
    }

    // ==================== CONTEXT MENU ====================

    private void handleContextMenu(MouseEvent e) {
        if (!e.isPopupTrigger()) return;

        int row = contactTable.rowAtPoint(e.getPoint());
        if (row >= 0) {
            contactTable.setRowSelectionInterval(row, row);
            int modelRow = contactTable.convertRowIndexToModel(row);

            JPopupMenu contextMenu = new JPopupMenu();

            JMenuItem editItem = new JMenuItem("\u270f Edit");
            editItem.addActionListener(ev -> openEditDialog());
            contextMenu.add(editItem);

            JMenuItem deleteItem = new JMenuItem("\ud83d\uddd1 Delete");
            deleteItem.addActionListener(ev -> deleteContact());
            contextMenu.add(deleteItem);

            contextMenu.addSeparator();

            String number = (String) tableModel.getValueAt(modelRow, 2);
            JMenuItem copyNumberItem = new JMenuItem("\ud83d\udcde Copy Number");
            copyNumberItem.addActionListener(ev -> {
                copyToClipboard(number);
                Toast.show(this, "\u2714 Number copied!");
            });
            contextMenu.add(copyNumberItem);

            String email = (String) tableModel.getValueAt(modelRow, 3);
            JMenuItem copyEmailItem = new JMenuItem("\ud83d\udce7 Copy Email");
            copyEmailItem.setEnabled(email != null && !email.isEmpty());
            copyEmailItem.addActionListener(ev -> {
                copyToClipboard(email);
                Toast.show(this, "\u2714 Email copied!");
            });
            contextMenu.add(copyEmailItem);

            contextMenu.show(contactTable, e.getX(), e.getY());
        }
    }

    private void copyToClipboard(String text) {
        if (text != null && !text.isEmpty()) {
            StringSelection selection = new StringSelection(text);
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
        }
    }

    // ==================== SEARCH SUGGESTIONS (AUTOCOMPLETE) ====================

    private void showSearchSuggestions(String keyword) {
        searchSuggestionPopup.removeAll();

        if (keyword.length() < 1) {
            searchSuggestionPopup.setVisible(false);
            return;
        }

        // Use the new getSuggestions with limit for fast autocomplete
        List<Contact> results = contactService.getSuggestions(keyword, 7);
        int count = Math.min(results.size(), 7);

        if (count == 0) {
            // Show "No suggestions" hint
            JMenuItem noResult = new JMenuItem("No matches found");
            noResult.setFont(new Font("Segoe UI", Font.ITALIC, 11));
            noResult.setEnabled(false);
            searchSuggestionPopup.add(noResult);
            searchSuggestionPopup.show(txtSearch, 0, txtSearch.getHeight());
            txtSearch.requestFocusInWindow();
            return;
        }

        for (int i = 0; i < count; i++) {
            Contact c = results.get(i);
            // Build rich suggestion text with highlighted match
            String suggestionText = buildSuggestionHTML(c, keyword);
            JMenuItem item = new JMenuItem("<html>" + suggestionText + "</html>");
            item.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            item.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            final String name = c.getName();
            item.addActionListener(ev -> {
                txtSearch.setText(name);
                currentSearchKeyword = name;
                performSearch();
                searchSuggestionPopup.setVisible(false);
            });
            searchSuggestionPopup.add(item);
        }

        // Footer with result count
        searchSuggestionPopup.addSeparator();
        JMenuItem footer = new JMenuItem(count + " suggestion" + (count > 1 ? "s" : ""));
        footer.setFont(new Font("Segoe UI", Font.ITALIC, 10));
        footer.setEnabled(false);
        searchSuggestionPopup.add(footer);

        searchSuggestionPopup.show(txtSearch, 0, txtSearch.getHeight());
        txtSearch.requestFocusInWindow();
    }

    /**
     * Build an HTML-formatted suggestion string.
     */
    private String buildSuggestionHTML(Contact c, String keyword) {
        String name = escapeHtml(c.getName());
        String number = escapeHtml(c.getNumber());
        String category = c.getCategory() != null ? escapeHtml(c.getCategory()) : "";

        return "<b>" + name + "</b>"
             + " &nbsp;<font color='gray'>\u2022 " + number + "</font>"
             + " &nbsp;<font color='#888' size='2'>[" + category + "]</font>";
    }

    /**
     * Highlight matching text with colored background using HTML.
     */
    private String highlightMatchingText(String text, String keyword) {
        if (keyword == null || keyword.isEmpty() || text == null || text.isEmpty()) {
            return text;
        }
        try {
            // Case-insensitive match
            Pattern pattern = Pattern.compile("(" + Pattern.quote(keyword) + ")", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(text);
            // Get accent color for the highlight
            Color accent = UITheme.getAccentColor();
            String hexColor = String.format("#%02x%02x%02x", accent.getRed(), accent.getGreen(), accent.getBlue());
            return matcher.replaceAll("<span style='background-color:" + hexColor + ";color:white;font-weight:bold'>$1</span>");
        } catch (Exception e) {
            return text;
        }
    }

    /**
     * Escape HTML special characters.
     */
    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    // ==================== COLUMN VISIBILITY ====================

    private void toggleColumn(String columnName, boolean visible) {
        columnVisibility.put(columnName, visible);
        updateColumnVisibility();
    }

    private void updateColumnVisibility() {
        // Remove all columns from the view
        TableColumnModel cm = contactTable.getColumnModel();
        while (cm.getColumnCount() > 0) {
            cm.removeColumn(cm.getColumn(0));
        }
        // Re-add only visible columns in original order
        for (String name : COLUMN_NAMES) {
            if (columnVisibility.getOrDefault(name, true)) {
                cm.addColumn(savedColumns.get(name));
            }
        }
    }

    // ==================== LOGOUT ====================

    private void doLogout() {
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to logout?",
            "Logout",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        dispose(); // Close the main window

        // Re-launch login dialog on the EDT
        SwingUtilities.invokeLater(() -> {
            UITheme.applyLightTheme();
            LoginDialog login = new LoginDialog(null);
            login.setVisible(true);
            model.User user = login.getLoggedInUser();
            if (user != null) {
                SwingUtilities.invokeLater(() -> {
                    ContactUI ui = new ContactUI(user);
                    ui.setVisible(true);
                });
            } else {
                System.exit(0);
            }
        });
    }

    // ==================== THEME & ACCENT ====================

    private void toggleTheme() {
        UITheme.toggleTheme();

        // Re-apply theme to core components
        UITheme.applyThemeToFrame(this);
        UITheme.applyThemeToTable(contactTable);

        // Update all panel backgrounds
        updatePanelThemes();

        // Update new dashboard panels
        statisticsPanel.applyTheme();
        previewPanel.applyTheme();
        updateEmptyStateTheme();

        // Refresh entire component tree
        SwingUtilities.updateComponentTreeUI(this);

        String theme = UITheme.isDarkMode() ? "Dark" : "Light";
        Toast.show(this, "\ud83c\udfa8 Switched to " + theme + " mode");
    }

    private void updateAccentColor(Color color) {
        UITheme.setAccentColor(color);

        // Refresh accent-dependent components
        previewPanel.repaint();
        statisticsPanel.repaint();

        // Update table selection color with accent tint
        contactTable.setSelectionBackground(new Color(
            color.getRed(), color.getGreen(), color.getBlue(), 80));
        contactTable.repaint();

        Toast.show(this, "\ud83c\udfa8 Accent color updated!");
    }

    /**
     * Update all panel themes (search panel, table panel, etc.)
     */
    private void updatePanelThemes() {
        Color panelBg = UITheme.isDarkMode() ? new Color(50, 50, 50) : new Color(245, 245, 245);
        Color textColor = UITheme.isDarkMode() ? Color.WHITE : Color.BLACK;
        Color borderColor = UITheme.isDarkMode() ? new Color(80, 80, 80) : new Color(200, 200, 200);

        // Update search panel
        searchPanel.setBackground(panelBg);
        searchPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, borderColor),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));

        for (Component comp : searchPanel.getComponents()) {
            if (comp instanceof JLabel) comp.setForeground(textColor);
            if (comp instanceof JCheckBox) {
                comp.setForeground(textColor);
            }
        }

        // Update include-deleted checkbox theme
        if (chkIncludeDeleted != null) {
            chkIncludeDeleted.setForeground(textColor);
        }
        // Update result count label color
        if (lblResultCount != null) {
            lblResultCount.setForeground(UITheme.getAccentColor());
        }

        // Update table container panels
        for (Component comp : getContentPane().getComponents()) {
            if (comp instanceof JPanel) {
                JPanel p = (JPanel) comp;
                p.setBackground(panelBg);
            }
            if (comp instanceof JSplitPane) {
                JSplitPane sp = (JSplitPane) comp;
                Component left = sp.getLeftComponent();
                if (left instanceof JPanel) {
                    JPanel tableOuter = (JPanel) left;
                    tableOuter.setBackground(panelBg);
                    for (Component child : tableOuter.getComponents()) {
                        if (child instanceof JPanel) {
                            JPanel cardP = (JPanel) child;
                            cardP.setBackground(panelBg);
                            for (Component inner : cardP.getComponents()) {
                                if (inner instanceof JScrollPane) {
                                    JScrollPane scp = (JScrollPane) inner;
                                    scp.setBorder(BorderFactory.createLineBorder(borderColor));
                                    scp.getViewport().setBackground(UITheme.isDarkMode() ? new Color(43, 43, 43) : Color.WHITE);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Update empty state panel theme.
     */
    private void updateEmptyStateTheme() {
        Color bg = UITheme.isDarkMode() ? new Color(50, 50, 50) : new Color(245, 245, 245);
        Color textColor = UITheme.isDarkMode() ? Color.WHITE : Color.BLACK;

        emptyStatePanel.setBackground(bg);
        for (Component c : emptyStatePanel.getComponents()) {
            if (c instanceof JPanel) {
                for (Component child : ((JPanel) c).getComponents()) {
                    if (child instanceof JLabel) {
                        JLabel lbl = (JLabel) child;
                        // Only update non-subtitle labels
                        if (lbl.getFont().getSize() > 14) {
                            lbl.setForeground(textColor);
                        }
                    }
                }
            }
        }
        tableCardPanel.setBackground(bg);
    }

    // ==================== KEYBOARD SHORTCUTS ====================

    private void setupKeyboardShortcuts() {
        // Ctrl+N - New Contact
        getRootPane().registerKeyboardAction(
            e -> openAddDialog(),
            KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK),
            JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        // F5 - Refresh
        getRootPane().registerKeyboardAction(
            e -> {
                loadAllContacts();
                refreshStatistics();
                updateStatusBar();
            },
            KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        // Delete key - Delete contact
        getRootPane().registerKeyboardAction(
            e -> {
                if (selectedContactId != -1 && btnDelete.isEnabled()) {
                    deleteContact();
                }
            },
            KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        // Ctrl+F - Focus search field
        getRootPane().registerKeyboardAction(
            e -> {
                txtSearch.requestFocusInWindow();
                txtSearch.selectAll();
            },
            KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_DOWN_MASK),
            JComponent.WHEN_IN_FOCUSED_WINDOW
        );
    }

    // ==================== DIALOG OPERATIONS ====================

    private void openAddDialog() {
        ContactFormDialog dialog = new ContactFormDialog(this, contactService);
        dialog.setVisible(true);

        if (dialog.isSaveSuccessful()) {
            loadAllContacts();
            refreshStatistics();
            updateStatusBar();
            updateEmptyState();

            // Highlight the newest contact (highest ID)
            if (tableModel.getRowCount() > 0) {
                int maxId = -1;
                int maxModelRow = -1;
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    int id = (int) tableModel.getValueAt(i, 0);
                    if (id > maxId) {
                        maxId = id;
                        maxModelRow = i;
                    }
                }
                if (maxModelRow >= 0) {
                    highlightRow(maxModelRow);
                }
            }
        }
    }

    private void openEditDialog() {
        if (selectedContactId == -1) {
            DialogHelper.showWarning(this, "Please select a contact to edit!",
                "No Selection");
            return;
        }

        Contact contact = contactService.getById(selectedContactId);
        if (contact == null) {
            DialogHelper.showError(this, "Contact not found!",
                "Error");
            return;
        }

        ContactFormDialog dialog = new ContactFormDialog(this, contactService, contact);
        dialog.setVisible(true);

        if (dialog.isSaveSuccessful()) {
            int editedId = selectedContactId;
            loadAllContacts();
            refreshStatistics();
            updateStatusBar();

            // Highlight the edited row
            int modelRow = findModelRowById(editedId);
            if (modelRow >= 0) {
                highlightRow(modelRow);
                Contact updated = contactService.getById(editedId);
                if (updated != null) previewPanel.showContact(updated);
            }
        }
    }

    private void openRecycleBin() {
        RecycleBinDialog dialog = new RecycleBinDialog(this, contactService);
        dialog.setVisible(true);

        // Refresh after recycle bin operations
        loadAllContacts();
        refreshStatistics();
        updateStatusBar();
        updateEmptyState();
    }

    private void openImportDialog() {
        ImportCSVDialog dialog = new ImportCSVDialog(this, contactService);
        dialog.setVisible(true);

        if (dialog.isImportSuccessful()) {
            loadAllContacts();
            refreshStatistics();
            updateStatusBar();
            updateEmptyState();
        }
    }

    private void openBatchOperationsDialog() {
        int[] selectedRows = contactTable.getSelectedRows();
        if (selectedRows.length == 0) {
            JOptionPane.showMessageDialog(this,
                "Please select one or more contacts for batch operations.",
                "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Collect selected contact IDs
        java.util.List<Integer> selectedIds = new java.util.ArrayList<>();
        for (int viewRow : selectedRows) {
            int modelRow = contactTable.convertRowIndexToModel(viewRow);
            int id = (int) tableModel.getValueAt(modelRow, 0);
            selectedIds.add(id);
        }

        BatchOperationsDialog dialog = new BatchOperationsDialog(this, contactService, selectedIds);
        dialog.setVisible(true);

        if (dialog.isOperationPerformed()) {
            loadAllContacts();
            refreshStatistics();
            updateStatusBar();
            updateEmptyState();
        }
    }

    // ==================== CRUD OPERATIONS ====================

    private void deleteContact() {
        // Check if multiple contacts are selected
        int[] selectedRows = contactTable.getSelectedRows();
        
        if (selectedRows.length == 0) {
            DialogHelper.showWarning(this, "Please select contact(s) to delete!",
                "No Selection");
            return;
        }
        
        // Batch delete if multiple selected
        if (selectedRows.length > 1) {
            boolean confirmed = DialogHelper.showConfirm(this,
                String.format("Delete %d selected contacts?\n(You can restore them from Recycle Bin)",
                    selectedRows.length),
                "Confirm Batch Delete");

            if (confirmed) {
                try {
                    java.util.List<Integer> ids = new java.util.ArrayList<>();
                    for (int viewRow : selectedRows) {
                        int modelRow = contactTable.convertRowIndexToModel(viewRow);
                        int id = (int) tableModel.getValueAt(modelRow, 0);
                        ids.add(id);
                    }
                    
                    contactService.batchDelete(ids);
                    Toast.show(this, String.format("\u2714 %d contacts moved to Recycle Bin!", ids.size()));
                    selectedContactId = -1;
                    previewPanel.clearPreview();
                    loadAllContacts();
                    refreshStatistics();
                    updateStatusBar();
                    updateEmptyState();
                } catch (Exception e) {
                    DialogHelper.showError(this, "Error during batch delete: " + e.getMessage(),
                        "Error");
                }
            }
            return;
        }
        
        // Single delete
        if (selectedContactId == -1) {
            DialogHelper.showWarning(this, "Please select a contact to delete!",
                "No Selection");
            return;
        }

        Contact contact = contactService.getById(selectedContactId);
        if (contact == null) {
            DialogHelper.showError(this, "Contact not found!",
                "Error");
            return;
        }

        boolean confirmed = DialogHelper.showConfirm(this,
            "Delete contact: " + contact.getName() + "?\n(You can restore it from Recycle Bin)",
            "Confirm Delete");

        if (confirmed) {
            try {
                contactService.softDeleteById(selectedContactId);
                Toast.show(this, "\u2714 Contact moved to Recycle Bin!");
                selectedContactId = -1;
                previewPanel.clearPreview();
                loadAllContacts();
                refreshStatistics();
                updateStatusBar();
                updateEmptyState();
            } catch (Exception e) {
                DialogHelper.showError(this, "Error deleting contact: " + e.getMessage(),
                    "Error");
            }
        }
    }

    private void exportToCSV() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Contacts to CSV");
        fileChooser.setSelectedFile(new java.io.File("contacts.csv"));

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            String filePath = fileChooser.getSelectedFile().getAbsolutePath();
            try {
                contactService.exportToCSV(filePath);
                Toast.show(this, "\u2714 Contacts exported successfully!");
            } catch (Exception e) {
                DialogHelper.showError(this, "Error exporting: " + e.getMessage(),
                    "Error");
            }
        }
    }

    // ==================== DISPLAY OPERATIONS ====================

    private void loadAllContacts() {
        currentSearchKeyword = "";
        tableModel.setRowCount(0);
        List<Contact> contacts = contactService.getAllContacts();

        for (Contact c : contacts) {
            Object[] row = {
                c.getId(),
                c.getName(),
                c.getNumber(),
                c.getEmail() != null ? c.getEmail() : "",
                c.getCategory(),
                c.getCreatedAt() != null ? c.getCreatedAt().toString() : ""
            };
            tableModel.addRow(row);
        }

        updateResultCount(contacts.size());
        updateStatusBar();
        updateEmptyState();
    }

    /**
     * Unified search method — called by debounce timer, category change, and include-deleted toggle.
     * Uses smart ranking for relevance-ordered results.
     */
    private void performSearch() {
        String keyword = txtSearch.getText().trim();
        String category = (String) cmbFilterCategory.getSelectedItem();
        boolean includeDeleted = chkIncludeDeleted.isSelected();

        tableModel.setRowCount(0);

        List<Contact> contacts;
        if (keyword.isEmpty() && "All".equals(category) && !includeDeleted) {
            contacts = contactService.getAllContacts();
        } else {
            // Use smart (ranked) search
            contacts = contactService.smartSearch(keyword, category, includeDeleted);
        }

        for (Contact c : contacts) {
            Object[] row = {
                c.getId(),
                c.getName(),
                c.getNumber(),
                c.getEmail() != null ? c.getEmail() : "",
                c.getCategory(),
                c.getCreatedAt() != null ? c.getCreatedAt().toString() : ""
            };
            tableModel.addRow(row);
        }

        updateResultCount(contacts.size());
        updateStatusBar();
        updateEmptyState();
        // Force repaint to update highlight rendering
        contactTable.repaint();
    }

    private void searchContacts(String keyword) {
        currentSearchKeyword = keyword;
        performSearch();
    }

    private void filterByCategory() {
        performSearch();
    }

    /**
     * Update the result count label.
     */
    private void updateResultCount(int count) {
        if (lblResultCount != null) {
            String keyword = txtSearch.getText().trim();
            if (!keyword.isEmpty() || !"All".equals(cmbFilterCategory.getSelectedItem()) 
                    || chkIncludeDeleted.isSelected()) {
                lblResultCount.setText(count + " result" + (count != 1 ? "s" : "") + " found");
                lblResultCount.setForeground(UITheme.getAccentColor());
            } else {
                lblResultCount.setText("");
            }
        }
    }

    // ==================== NEW FEATURE METHODS ====================

    /**
     * Refresh statistics panel counts from database.
     */
    public void refreshStatistics() {
        if (statisticsPanel != null) {
            statisticsPanel.refreshStatistics();
        }
    }

    /**
     * Switch between table view and empty state based on row count.
     */
    private void updateEmptyState() {
        if (tableCardLayout != null && tableCardPanel != null) {
            if (tableModel.getRowCount() == 0) {
                tableCardLayout.show(tableCardPanel, "EMPTY");
            } else {
                tableCardLayout.show(tableCardPanel, "TABLE");
            }
        }
    }

    /**
     * Briefly highlight a row with accent color after add/edit/restore.
     */
    private void highlightRow(int modelRow) {
        try {
            int viewRow = contactTable.convertRowIndexToView(modelRow);
            if (viewRow < 0) return;

            contactTable.setRowSelectionInterval(viewRow, viewRow);
            contactTable.scrollRectToVisible(contactTable.getCellRect(viewRow, 0, true));

            Color accent = UITheme.getAccentColor();
            highlightColor = new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 100);
            highlightedViewRow = viewRow;
            contactTable.repaint();

            // Clear highlight after 1.5 seconds
            Timer timer = new Timer(1500, e -> {
                highlightedViewRow = -1;
                highlightColor = null;
                contactTable.repaint();
            });
            timer.setRepeats(false);
            timer.start();
        } catch (Exception e) {
            // Silent fail - don't break main flow
        }
    }

    /**
     * Find a model row index by contact ID.
     */
    private int findModelRowById(int id) {
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if ((int) tableModel.getValueAt(i, 0) == id) return i;
        }
        return -1;
    }

    private void updateStatusBar() {
        int totalActive = contactService.countContacts();
        int totalDeleted = contactService.getDeletedContacts().size();
        int displayed = tableModel.getRowCount();

        lblStatus.setText(String.format("\ud83d\udcca Total Active: %d | \ud83d\uddd1\ufe0f Deleted: %d | \ud83d\udccb Displayed: %d",
            totalActive, totalDeleted, displayed));
    }

    public void display() {
        setVisible(true);
    }
}
