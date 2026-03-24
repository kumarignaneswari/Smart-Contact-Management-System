package ui;

import model.Contact;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * QRCodeDialog
 *
 * Generates and displays a QR code for a contact.
 * QR content is a vCard (standard format readable by any phone scanner).
 *
 * Requires: ZXing core JAR in lib/ folder.
 *   Download: https://repo1.maven.org/maven2/com/google/zxing/core/3.5.3/core-3.5.3.jar
 *   Place at:  <workspace>/lib/core-3.5.3.jar
 */
public class QRCodeDialog extends JDialog {

    private static final int QR_SIZE = 300;
    private static final String ZXING_VERSION = "3.5.3";
    private static final String ZXING_JAR_NAME = "core-" + ZXING_VERSION + ".jar";
    private static final String ZXING_URL = "https://repo1.maven.org/maven2/com/google/zxing/core/"
            + ZXING_VERSION + "/" + ZXING_JAR_NAME;

    public QRCodeDialog(Frame parent, Contact contact) {
        super(parent, "QR Code - " + contact.getName(), true);
        buildUI(contact);
        pack();
        setResizable(false);
        setLocationRelativeTo(parent);
    }

    private void buildUI(Contact contact) {
        Color bg = UITheme.getPanelBackground();
        Color fg = UITheme.getForeground();

        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBackground(bg);
        root.setBorder(new EmptyBorder(20, 20, 16, 20));
        setContentPane(root);

        // ── Title ──
        JLabel lblTitle = new JLabel(contact.getName(), SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(fg);
        root.add(lblTitle, BorderLayout.NORTH);

        // ── QR Image ──
        JLabel qrLabel;
        try {
            BufferedImage qrImg = generateQR(buildVCard(contact), QR_SIZE);
            qrLabel = new JLabel(new ImageIcon(qrImg));
        } catch (Exception e) {
            // ZXing unavailable and bootstrap failed.
            qrLabel = new JLabel(
                "<html><center><b>QR generation failed.</b><br><br>"
                + "Could not load ZXing dependency.<br><br>"
                + "Please check internet once and reopen this dialog,<br>"
                + "or place <tt>" + ZXING_JAR_NAME + "</tt> in <tt>lib/</tt>.<br><br>"
                + "Source:<br><tt>repo1.maven.org/.../" + ZXING_JAR_NAME + "</tt></center></html>",
                SwingConstants.CENTER);
            qrLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            qrLabel.setForeground(Color.RED);
            qrLabel.setPreferredSize(new Dimension(QR_SIZE, QR_SIZE));
        }

        JPanel imgPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        imgPanel.setBackground(Color.WHITE); // QR always on white background
        imgPanel.setBorder(BorderFactory.createLineBorder(UITheme.getBorderColor(), 1));
        imgPanel.add(qrLabel);
        root.add(imgPanel, BorderLayout.CENTER);

        // ── Info text ──
        JLabel lblInfo = new JLabel("Scan with any phone camera to get contact details.", SwingConstants.CENTER);
        lblInfo.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblInfo.setForeground(UITheme.isDarkMode() ? new Color(180, 180, 180) : new Color(100, 100, 100));

        // ── Close button ──
        JButton btnClose = new JButton("Close");
        btnClose.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnClose.addActionListener(e -> dispose());

        JPanel southPanel = new JPanel(new BorderLayout(0, 8));
        southPanel.setBackground(bg);
        southPanel.add(lblInfo, BorderLayout.NORTH);
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnRow.setBackground(bg);
        btnRow.add(btnClose);
        southPanel.add(btnRow, BorderLayout.SOUTH);

        root.add(southPanel, BorderLayout.SOUTH);
    }

    // ─────────────────────────────────────────────────
    //  QR generation (ZXing core JAR)
    // ─────────────────────────────────────────────────

    /**
     * Generate a QR code as a BufferedImage.
     * Uses reflection so the code compiles without hard dependency on ZXing.
     */
    private static BufferedImage generateQR(String content, int size) throws Exception {
        try {
            return generateQRWithClassLoader(content, size, Thread.currentThread().getContextClassLoader());
        } catch (ClassNotFoundException missing) {
            ClassLoader zxingLoader = ensureZXingClassLoader();
            return generateQRWithClassLoader(content, size, zxingLoader);
        }
    }

    private static BufferedImage generateQRWithClassLoader(String content, int size, ClassLoader classLoader)
            throws Exception {
        Class<?> writerClass = Class.forName("com.google.zxing.qrcode.QRCodeWriter", true, classLoader);
        Class<?> formatClass = Class.forName("com.google.zxing.BarcodeFormat", true, classLoader);
        Class<?> matrixClass = Class.forName("com.google.zxing.common.BitMatrix", true, classLoader);

        Object writer       = writerClass.getDeclaredConstructor().newInstance();
        Object qrFormat     = formatClass.getField("QR_CODE").get(null);

        // encode(String contents, BarcodeFormat format, int width, int height)
        Object matrix = writerClass.getMethod("encode",
                String.class, formatClass, int.class, int.class)
                .invoke(writer, content, qrFormat, size, size);

        // Convert BitMatrix → BufferedImage manually (no javase jar needed)
        int w = (int) matrixClass.getMethod("getWidth").invoke(matrix);
        int h = (int) matrixClass.getMethod("getHeight").invoke(matrix);

        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                boolean black = (boolean) matrixClass.getMethod("get", int.class, int.class)
                        .invoke(matrix, x, y);
                img.setRGB(x, y, black ? 0x000000 : 0xFFFFFF);
            }
        }
        return img;
    }

    private static ClassLoader ensureZXingClassLoader() throws Exception {
        Path jarPath = resolveZXingJarPath();
        if (!Files.exists(jarPath)) {
            Files.createDirectories(jarPath.getParent());

            HttpClient client = HttpClient.newBuilder().build();
            HttpRequest request = HttpRequest.newBuilder(URI.create(ZXING_URL)).GET().build();
            HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() != 200) {
                throw new IllegalStateException("Unable to download ZXing jar. HTTP " + response.statusCode());
            }
            Files.write(jarPath, response.body());
        }

        URL jarUrl = jarPath.toUri().toURL();
        return new URLClassLoader(new URL[]{jarUrl}, QRCodeDialog.class.getClassLoader());
    }

    private static Path resolveZXingJarPath() {
        String base = System.getProperty("user.dir", ".");
        return Paths.get(base, "lib", ZXING_JAR_NAME);
    }

    // ─────────────────────────────────────────────────
    //  vCard builder  — readable by all phone cameras
    // ─────────────────────────────────────────────────

    private static String buildVCard(Contact c) {
        StringBuilder sb = new StringBuilder();
        sb.append("BEGIN:VCARD\n");
        sb.append("VERSION:3.0\n");
        sb.append("FN:").append(safe(c.getName())).append("\n");
        sb.append("TEL:").append(safe(c.getNumber())).append("\n");
        if (c.getEmail() != null && !c.getEmail().isEmpty()) {
            sb.append("EMAIL:").append(c.getEmail()).append("\n");
        }
        if (c.getCategory() != null) {
            sb.append("CATEGORIES:").append(c.getCategory()).append("\n");
        }
        sb.append("END:VCARD");
        return sb.toString();
    }

    private static String safe(String s) {
        return s == null ? "" : s.trim();
    }
}
