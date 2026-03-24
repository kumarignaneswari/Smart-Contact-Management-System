package util;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;

/**
 * DatabaseInitializer
 *
 * Creates all required tables in java_project if they do not already exist.
 * Called once at application startup (before the login dialog).
 * This way the app works out-of-the-box without needing to run database.sql manually.
 */
public class DatabaseInitializer {

    public static void initialize() {
        Connection conn = DBConnection.getConnection();
        if (conn == null) {
            System.err.println("DatabaseInitializer: no connection – skipping table creation.");
            return;
        }

        try (Statement stmt = conn.createStatement()) {

            // ── users ──────────────────────────────────────────────
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS users (" +
                "  id         INT AUTO_INCREMENT PRIMARY KEY," +
                "  username   VARCHAR(50)  NOT NULL UNIQUE," +
                "  password   VARCHAR(255) NOT NULL," +
                "  email      VARCHAR(100)," +
                "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")"
            );

            // ── contacts ───────────────────────────────────────────
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS contacts (" +
                "  id         INT AUTO_INCREMENT PRIMARY KEY," +
                "  user_id    INT          NOT NULL," +
                "  name       VARCHAR(50)  NOT NULL," +
                "  number     VARCHAR(15)  NOT NULL," +
                "  email      VARCHAR(50)," +
                "  category   VARCHAR(30)  DEFAULT 'Friends'," +
                "  is_deleted TINYINT(1)   DEFAULT 0," +
                "  created_at TIMESTAMP    DEFAULT CURRENT_TIMESTAMP," +
                "  updated_at TIMESTAMP    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                "  CONSTRAINT fk_contacts_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE" +
                ")"
            );

            // Unique constraints per-user (ignore if already present)
            try {
                stmt.executeUpdate(
                    "ALTER TABLE contacts " +
                    "ADD CONSTRAINT unique_name_user_active UNIQUE (user_id, name, is_deleted)"
                );
            } catch (SQLException ignored) { /* constraint already exists */ }

            try {
                stmt.executeUpdate(
                    "ALTER TABLE contacts " +
                    "ADD CONSTRAINT unique_number_user_active UNIQUE (user_id, number, is_deleted)"
                );
            } catch (SQLException ignored) { /* constraint already exists */ }

            // Index for faster per-user lookups
            try {
                stmt.executeUpdate(
                    "CREATE INDEX idx_contacts_user_id ON contacts(user_id)"
                );
            } catch (SQLException ignored) { /* index already exists */ }

            System.out.println("DatabaseInitializer: tables ready.");

        } catch (SQLException e) {
            System.err.println("DatabaseInitializer error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
