package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Database Connection Utility
 * 
 * Provides database connection management using singleton pattern.
 * Contains MySQL connection configuration.
 * Used by DAO layer to interact with database.
 */
public class DBConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/java_project";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "Kappala@2007";
    
    private static Connection connection = null;

    // Private constructor to prevent instantiation
    private DBConnection() {
    }

    /**
     * Get database connection (singleton)
     * Creates new connection if not exists or if connection is closed
     */
    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                // Load MySQL JDBC Driver
                Class.forName("com.mysql.cj.jdbc.Driver");
                // Create connection
                connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
                System.out.println("Database connected successfully!");
            }
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found!");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Database connection failed!");
            e.printStackTrace();
        }
        return connection;
    }

    /**
     * Close database connection
     */
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("Error closing connection!");
            e.printStackTrace();
        }
    }

    /**
     * Test if database connection is alive
     */
    public static boolean testConnection() {
        try {
            Connection conn = getConnection();
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}
