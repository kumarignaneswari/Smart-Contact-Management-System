import model.User;
import ui.ContactUI;
import ui.LoginDialog;
import util.DBConnection;
import util.DatabaseInitializer;

import javax.swing.*;

/**
 * Main Application Launcher
 * 
 * Entry point for the Contact Management System.
 * 1. Checks database connection.
 * 2. Shows Login / Register dialog.
 * 3. Launches ContactUI scoped to the authenticated user.
 */
public class Main {
        
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("Contact Management System - Starting...");
        System.out.println("========================================");
        
        // Test database connection
        System.out.println("Testing database connection...");
        if (!DBConnection.testConnection()) {
            System.err.println("ERROR: Cannot connect to database!");
            System.err.println("Please ensure:");
            System.err.println("1. MySQL server is running");
            System.err.println("2. Database 'java_project' exists");
            System.err.println("3. Username: root, Password: root");
            System.err.println("4. Run the database.sql script first");
            
            JOptionPane.showMessageDialog(null, 
                "Database connection failed!\n" +
                "Please check:\n" +
                "1. MySQL is running\n" +
                "2. Database 'java_project' exists\n" +
                "3. Credentials: root/root\n" +
                "4. Run database.sql first",
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        System.out.println("Database connection successful!");

        // Auto-create tables if they don't exist (first-run setup)
        System.out.println("Initializing database tables...");
        DatabaseInitializer.initialize();
        
        // Set look and feel for better appearance
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Could not set look and feel: " + e.getMessage());
        }

        // Show Login / Register dialog (blocks until user authenticates)
        LoginDialog loginDialog = new LoginDialog(null);
        loginDialog.setVisible(true);

        User loggedInUser = loginDialog.getLoggedInUser();
        if (loggedInUser == null) {
            // User closed the dialog without logging in
            System.out.println("Login cancelled. Exiting.");
            return;
        }

        System.out.println("Logged in as: " + loggedInUser.getUsername());
        System.out.println("Launching UI...");
        
        // Launch Swing UI on Event Dispatch Thread, scoped to the logged-in user
        final User user = loggedInUser;
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                ContactUI contactUI = new ContactUI(user);
                contactUI.display();
                System.out.println("Application started successfully!");
            }
        });
    }
}
