package ui;

import model.User;
import service.UserService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Login / Registration Dialog — Professional redesign
 *
 * Two-column layout:
 *   Left  — branded gradient panel with app logo & description
 *   Right — clean white/dark form card with modern styled inputs
 *
 * On successful login the authenticated User object is stored and retrievable
 * via getLoggedInUser().
 */
public class LoginDialog extends JDialog {

    private final UserService userService;

    // Result
    private User loggedInUser = null;

    // ===== Login tab components =====
    private JTextField txtLoginUser;
    private JPasswordField txtLoginPass;
    private JButton btnLogin;

    // ===== Register tab components =====
    private JTextField txtRegUser;
    private JPasswordField txtRegPass;
    private JTextField txtRegEmail;
    private JButton btnRegister;

    // ===== Shared =====
    private JTabbedPane tabbedPane;
    private JLabel lblMessage;

    // Branding gradient colors
    private static final Color BRAND_TOP    = new Color(25, 55, 110);
    private static final Color BRAND_BOTTOM = new Color(42, 108, 185);
    private static final Color BRAND_ACCENT = new Color(80, 160, 255);

    public LoginDialog(Frame owner) {
        super(owner, "Contact Manager – Login", true);
        this.userService = new UserService();
        buildUI();
        pack();
        setMinimumSize(new Dimension(780, 500));
        setSize(780, 500);
        setLocationRelativeTo(owner);
    }

    // ==================== UI CONSTRUCTION ====================

    private void buildUI() {
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        // Root: side-by-side branding + form
        JPanel root = new JPanel(new BorderLayout());
        root.add(buildBrandPanel(), BorderLayout.WEST);
        root.add(buildFormPanel(), BorderLayout.CENTER);
        setContentPane(root);

        // Default button wiring
        getRootPane().setDefaultButton(btnLogin);
        tabbedPane.addChangeListener(e -> {
            clearMessage();
            if (tabbedPane.getSelectedIndex() == 0) {
                getRootPane().setDefaultButton(btnLogin);
            } else {
                getRootPane().setDefaultButton(btnRegister);
            }
        });
    }

    // ==================== BRAND PANEL (LEFT) ====================

    private JPanel buildBrandPanel() {
        // Load the profile image once; fall back to gradient if missing
        java.awt.image.BufferedImage[] profileImg = {null};
        try {
            String[] paths = {
                "/resources/icons/loginprofile.jpeg",
                "/icons/loginprofile.jpeg",
                "resources/icons/loginprofile.jpeg"
            };
            for (String path : paths) {
                java.net.URL imgUrl = LoginDialog.class.getResource(path);
                if (imgUrl != null) {
                    profileImg[0] = javax.imageio.ImageIO.read(imgUrl);
                    break;
                }
            }
            // Try direct file path if classpath search fails
            if (profileImg[0] == null) {
                java.io.File f = new java.io.File("ContactManagerSwing/resources/icons/loginprofile.jpeg");
                if (!f.exists()) f = new java.io.File("resources/icons/loginprofile.jpeg");
                if (f.exists()) profileImg[0] = javax.imageio.ImageIO.read(f);
            }
        } catch (Exception ignored) {}

        JPanel panel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

                if (profileImg[0] != null) {
                    // Scale image to cover the full panel (cover-fit, centered)
                    int pw = getWidth(), ph = getHeight();
                    int iw = profileImg[0].getWidth(), ih = profileImg[0].getHeight();
                    double scale = Math.max((double) pw / iw, (double) ph / ih);
                    int dw = (int) (iw * scale), dh = (int) (ih * scale);
                    int dx = (pw - dw) / 2,    dy = (ph - dh) / 2;
                    g2.drawImage(profileImg[0], dx, dy, dw, dh, null);

                    // Dark gradient scrim so white text stays readable
                    GradientPaint scrim = new GradientPaint(
                        0, 0, new Color(20, 40, 90, 80),
                        0, ph, new Color(10, 20, 55, 200));
                    g2.setPaint(scrim);
                    g2.fillRect(0, 0, pw, ph);
                } else {
                    // Fallback: original gradient
                    GradientPaint gp = new GradientPaint(0, 0, BRAND_TOP, 0, getHeight(), BRAND_BOTTOM);
                    g2.setPaint(gp);
                    g2.fillRect(0, 0, getWidth(), getHeight());
                }
                g2.dispose();
            }
        };
        panel.setPreferredSize(new Dimension(290, 500));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(40, 30, 40, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(6, 0, 6, 0);

        // App name
        JLabel appName = new JLabel("ContactHub", SwingConstants.CENTER);
        appName.setFont(new Font("Segoe UI", Font.BOLD, 26));
        appName.setForeground(Color.WHITE);
        gbc.gridy = 1;
        panel.add(appName, gbc);

        // Divider line
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(255, 255, 255, 60));
        sep.setPreferredSize(new Dimension(180, 1));
        gbc.gridy = 2;
        gbc.insets = new Insets(4, 0, 12, 0);
        panel.add(sep, gbc);

        // Tagline
        JLabel tagline = new JLabel("<html><center>Manage your contacts<br>smarter & faster</center></html>", SwingConstants.CENTER);
        tagline.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tagline.setForeground(new Color(200, 220, 255));
        gbc.gridy = 3;
        gbc.insets = new Insets(0, 0, 28, 0);
        panel.add(tagline, gbc);

        // Feature bullets
        String[] features = { "✓  Multi-user accounts", "✓  Smart search & filters", "✓  Import / Export CSV", "✓  Dark & Light themes" };
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(3, 10, 3, 10);
        for (int i = 0; i < features.length; i++) {
            JLabel feat = new JLabel(features[i]);
            feat.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            feat.setForeground(new Color(180, 210, 255));
            gbc.gridy = 4 + i;
            panel.add(feat, gbc);
        }

        // Version label at bottom
        JLabel version = new JLabel("v2.0  •  Professional Edition");
        version.setFont(new Font("Segoe UI", Font.ITALIC, 10));
        version.setForeground(new Color(140, 170, 220));
        gbc.gridy = 9;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(30, 0, 0, 0);
        panel.add(version, gbc);

        return panel;
    }

    // ==================== FORM PANEL (RIGHT) ====================

    private JPanel buildFormPanel() {
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(new Color(245, 246, 252));

        JPanel card = new JPanel(new BorderLayout(0, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 0, 0, 18));
                g2.fillRoundRect(4, 6, getWidth() - 5, getHeight() - 5, 18, 18);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 4, getHeight() - 4, 18, 18);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(380, 390));
        card.setBorder(new EmptyBorder(28, 32, 24, 32));

        // Heading
        JLabel heading = new JLabel("Welcome Back", SwingConstants.CENTER);
        heading.setFont(new Font("Segoe UI", Font.BOLD, 20));
        heading.setForeground(new Color(25, 40, 80));
        heading.setBorder(new EmptyBorder(0, 0, 16, 0));
        card.add(heading, BorderLayout.NORTH);

        // Tabs — custom styled
        tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabbedPane.setBackground(Color.WHITE);
        tabbedPane.addTab("  Sign In  ", buildLoginPanel());
        tabbedPane.addTab("  Register  ", buildRegisterPanel());
        tabbedPane.addChangeListener(e -> {
            heading.setText(tabbedPane.getSelectedIndex() == 0 ? "Welcome Back" : "Create Account");
        });
        card.add(tabbedPane, BorderLayout.CENTER);

        // Message label
        lblMessage = new JLabel(" ", SwingConstants.CENTER);
        lblMessage.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblMessage.setForeground(Color.RED);
        lblMessage.setBorder(new EmptyBorder(8, 0, 0, 0));
        card.add(lblMessage, BorderLayout.SOUTH);

        GridBagConstraints gbc = new GridBagConstraints();
        wrapper.add(card, gbc);
        return wrapper;
    }

    private JPanel buildLoginPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(10, 4, 10, 4));
        GridBagConstraints gbc = formGbc();

        // Username
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        p.add(fieldLabel("Username"), gbc);
        gbc.gridy = 1;
        txtLoginUser = new JTextField(18);
        txtLoginUser.setPreferredSize(new Dimension(260, 36));
        UITheme.styleTextField(txtLoginUser);
        p.add(txtLoginUser, gbc);

        // Password
        gbc.gridy = 2;
        p.add(fieldLabel("Password"), gbc);
        gbc.gridy = 3;
        txtLoginPass = new JPasswordField(18);
        txtLoginPass.setPreferredSize(new Dimension(260, 36));
        UITheme.stylePasswordField(txtLoginPass);
        p.add(txtLoginPass, gbc);

        // Login button
        gbc.gridy = 4;
        gbc.insets = new Insets(16, 0, 0, 0);
        btnLogin = UITheme.createAccentButton("Sign In");
        btnLogin.setPreferredSize(new Dimension(260, 40));
        btnLogin.addActionListener(e -> doLogin());
        p.add(btnLogin, gbc);

        return p;
    }

    private JPanel buildRegisterPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(6, 4, 6, 4));
        GridBagConstraints gbc = formGbc();

        // Username
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        p.add(fieldLabel("Username"), gbc);
        gbc.gridy = 1;
        txtRegUser = new JTextField(18);
        txtRegUser.setPreferredSize(new Dimension(260, 36));
        UITheme.styleTextField(txtRegUser);
        p.add(txtRegUser, gbc);

        // Password
        gbc.gridy = 2;
        p.add(fieldLabel("Password"), gbc);
        gbc.gridy = 3;
        txtRegPass = new JPasswordField(18);
        txtRegPass.setPreferredSize(new Dimension(260, 36));
        UITheme.stylePasswordField(txtRegPass);
        p.add(txtRegPass, gbc);

        // Email
        gbc.gridy = 4;
        p.add(fieldLabel("Email (optional)"), gbc);
        gbc.gridy = 5;
        txtRegEmail = new JTextField(18);
        txtRegEmail.setPreferredSize(new Dimension(260, 36));
        UITheme.styleTextField(txtRegEmail);
        p.add(txtRegEmail, gbc);

        // Register button
        gbc.gridy = 6;
        gbc.insets = new Insets(12, 0, 0, 0);
        btnRegister = UITheme.createAccentButton("Create Account");
        btnRegister.setPreferredSize(new Dimension(260, 40));
        btnRegister.addActionListener(e -> doRegister());
        p.add(btnRegister, gbc);

        return p;
    }

    private JLabel fieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(new Color(75, 85, 115));
        lbl.setBorder(new EmptyBorder(6, 0, 2, 0));
        return lbl;
    }

    private GridBagConstraints formGbc() {
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(2, 0, 2, 0);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.WEST;
        return g;
    }

    // ==================== ACTIONS ====================

    private void doLogin() {
        String username = txtLoginUser.getText().trim();
        String password = new String(txtLoginPass.getPassword()).trim();
        try {
            User user = userService.login(username, password);
            loggedInUser = user;
            setVisible(false);
        } catch (RuntimeException ex) {
            showError(ex.getMessage());
        }
    }

    private void doRegister() {
        String username = txtRegUser.getText().trim();
        String password = new String(txtRegPass.getPassword()).trim();
        String email    = txtRegEmail.getText().trim();
        try {
            int newId = userService.register(username, password, email);
            loggedInUser = userService.getUserById(newId);
            showSuccess("Registration successful! Logged in as: " + username);
            Timer t = new Timer(1200, e -> setVisible(false));
            t.setRepeats(false);
            t.start();
        } catch (RuntimeException ex) {
            showError(ex.getMessage());
        }
    }

    private void showError(String msg) {
        lblMessage.setForeground(new Color(200, 40, 40));
        lblMessage.setText(msg);
    }

    private void showSuccess(String msg) {
        lblMessage.setForeground(new Color(0, 140, 70));
        lblMessage.setText(msg);
    }

    private void clearMessage() {
        lblMessage.setText(" ");
    }

    // ==================== RESULT ====================

    /**
     * Returns the authenticated User after a successful login/registration.
     * Returns null if the dialog was cancelled.
     */
    public User getLoggedInUser() {
        return loggedInUser;
    }
}
