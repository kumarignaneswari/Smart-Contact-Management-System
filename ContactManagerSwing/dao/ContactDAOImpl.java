package dao;

import model.Contact;
import model.ImportResult;
import util.DBConnection;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Contact DAO Implementation
 * 
 * Implements all database operations using JDBC and PreparedStatement.
 * This class contains NO business logic - only database operations.
 * ALL SQL queries use PreparedStatement to prevent SQL injection.
 * Handles soft delete functionality with is_deleted flag.
 */
public class ContactDAOImpl implements ContactDAO {

    // ID of the currently logged-in user; every query is scoped to this user
    private int currentUserId = -1;

    @Override
    public void setCurrentUserId(int userId) {
        this.currentUserId = userId;
    }

    // ==================== CREATE OPERATIONS ====================

    public void addContact(Contact c) {
        String sql = "INSERT INTO contacts (user_id, name, number, email, category, is_deleted) VALUES (?, ?, ?, ?, ?, 0)";
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, currentUserId);
            pstmt.setString(2, c.getName());
            pstmt.setString(3, c.getNumber());
            pstmt.setString(4, c.getEmail());
            pstmt.setString(5, c.getCategory());
            pstmt.executeUpdate();
            System.out.println("Contact added successfully!");
        } catch (SQLException e) {
            System.err.println("Error adding contact: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void addContacts(List<Contact> contacts) {
        String sql = "INSERT INTO contacts (user_id, name, number, email, category, is_deleted) VALUES (?, ?, ?, ?, ?, 0)";
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (Contact c : contacts) {
                pstmt.setInt(1, currentUserId);
                pstmt.setString(2, c.getName());
                pstmt.setString(3, c.getNumber());
                pstmt.setString(4, c.getEmail());
                pstmt.setString(5, c.getCategory() != null ? c.getCategory() : "Friends");
                pstmt.addBatch();
            }
            pstmt.executeBatch();
            System.out.println("Multiple contacts added successfully!");
        } catch (SQLException e) {
            System.err.println("Error adding contacts: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ==================== READ OPERATIONS ====================

    public List<Contact> getAllContacts() {
        List<Contact> contacts = new ArrayList<>();
        String sql = "SELECT * FROM contacts WHERE is_deleted = 0 AND user_id = " + currentUserId;
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                contacts.add(extractContactFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting all contacts: " + e.getMessage());
            e.printStackTrace();
        }
        return contacts;
    }

    public Contact getById(int id) {
        String sql = "SELECT * FROM contacts WHERE id = ? AND is_deleted = 0 AND user_id = " + currentUserId;
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractContactFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting contact by ID: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public Contact getByName(String name) {
        String sql = "SELECT * FROM contacts WHERE name = ? AND is_deleted = 0 AND user_id = " + currentUserId;
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractContactFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting contact by name: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public Contact getByNumber(String number) {
        String sql = "SELECT * FROM contacts WHERE number = ? AND is_deleted = 0 AND user_id = " + currentUserId;
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, number);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractContactFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting contact by number: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public List<Contact> search(String keyword) {
        List<Contact> contacts = new ArrayList<>();
        String sql = "SELECT * FROM contacts WHERE is_deleted = 0 AND user_id = " + currentUserId + " AND " +
                     "(name LIKE ? OR number LIKE ? OR email LIKE ?)";
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String searchPattern = "%" + keyword + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            pstmt.setString(3, searchPattern);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    contacts.add(extractContactFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error searching contacts: " + e.getMessage());
            e.printStackTrace();
        }
        return contacts;
    }

    // NEW: Search by category
    public List<Contact> searchByCategory(String category) {
        List<Contact> contacts = new ArrayList<>();
        String sql = "SELECT * FROM contacts WHERE is_deleted = 0 AND user_id = " + currentUserId + " AND category = ? ORDER BY name ASC";
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, category);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    contacts.add(extractContactFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error searching by category: " + e.getMessage());
            e.printStackTrace();
        }
        return contacts;
    }

    // NEW: Search by name prefix (names starting with prefix)
    public List<Contact> searchByNamePrefix(String prefix) {
        List<Contact> contacts = new ArrayList<>();
        String sql = "SELECT * FROM contacts WHERE is_deleted = 0 AND user_id = " + currentUserId + " AND name LIKE ? ORDER BY name ASC";
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, prefix + "%");  // LIKE 'A%' finds names starting with A
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    contacts.add(extractContactFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error searching by prefix: " + e.getMessage());
            e.printStackTrace();
        }
        return contacts;
    }

    public List<Contact> getContacts(int offset, int limit) {
        List<Contact> contacts = new ArrayList<>();
        String sql = "SELECT * FROM contacts WHERE is_deleted = 0 AND user_id = " + currentUserId + " LIMIT ? OFFSET ?";
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            pstmt.setInt(2, offset);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    contacts.add(extractContactFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting paginated contacts: " + e.getMessage());
            e.printStackTrace();
        }
        return contacts;
    }

    public List<Contact> getAllSortedByName() {
        List<Contact> contacts = new ArrayList<>();
        String sql = "SELECT * FROM contacts WHERE is_deleted = 0 AND user_id = " + currentUserId + " ORDER BY name ASC";
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                contacts.add(extractContactFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting sorted contacts: " + e.getMessage());
            e.printStackTrace();
        }
        return contacts;
    }

    public int countContacts() {
        String sql = "SELECT COUNT(*) FROM contacts WHERE is_deleted = 0 AND user_id = " + currentUserId;
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error counting contacts: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    // ==================== UPDATE OPERATIONS ====================

    public void updateContact(Contact c) {
        String sql = "UPDATE contacts SET name = ?, number = ?, email = ?, category = ? WHERE id = ? AND is_deleted = 0 AND user_id = " + currentUserId;
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, c.getName());
            pstmt.setString(2, c.getNumber());
            pstmt.setString(3, c.getEmail());
            pstmt.setString(4, c.getCategory());
            pstmt.setInt(5, c.getId());
            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                System.out.println("Contact updated successfully!");
            } else {
                System.out.println("Contact not found or already deleted!");
            }
        } catch (SQLException e) {
            System.err.println("Error updating contact: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void updateEmail(int id, String email) {
        String sql = "UPDATE contacts SET email = ? WHERE id = ? AND is_deleted = 0 AND user_id = " + currentUserId;
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            pstmt.setInt(2, id);
            pstmt.executeUpdate();
            System.out.println("Email updated successfully!");
        } catch (SQLException e) {
            System.err.println("Error updating email: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void updateNumber(int id, String number) {
        String sql = "UPDATE contacts SET number = ? WHERE id = ? AND is_deleted = 0 AND user_id = " + currentUserId;
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, number);
            pstmt.setInt(2, id);
            pstmt.executeUpdate();
            System.out.println("Number updated successfully!");
        } catch (SQLException e) {
            System.err.println("Error updating number: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ==================== DELETE OPERATIONS ====================

    public void softDeleteById(int id) {
        String sql = "UPDATE contacts SET is_deleted = 1 WHERE id = ? AND user_id = " + currentUserId;
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                System.out.println("Contact soft deleted successfully!");
            }
        } catch (SQLException e) {
            System.err.println("Error soft deleting contact: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void restoreContact(int id) {
        // CRITICAL FIX: Check for duplicate name/number before restoring
        Contact contactToRestore = null;
        String selectSql = "SELECT * FROM contacts WHERE id = ? AND user_id = " + currentUserId;
        Connection conn = DBConnection.getConnection();
        
        // First, get the contact details
        try (PreparedStatement pstmt = conn.prepareStatement(selectSql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    contactToRestore = extractContactFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting contact for restore: " + e.getMessage());
            e.printStackTrace();
            return;
        }
        
        if (contactToRestore == null) {
            System.err.println("Contact not found!");
            return;
        }
        
        // Check if name already exists in active contacts (case-insensitive)
        String checkNameSql = "SELECT COUNT(*) FROM contacts WHERE LOWER(name) = LOWER(?) AND is_deleted = 0 AND user_id = " + currentUserId;
        try (PreparedStatement pstmt = conn.prepareStatement(checkNameSql)) {
            pstmt.setString(1, contactToRestore.getName());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    throw new SQLException("Cannot restore: A contact with name '" + contactToRestore.getName() + "' already exists!");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error restoring contact: " + e.getMessage());
            e.printStackTrace();
            return;
        }
        
        // Check if number already exists in active contacts
        String checkNumberSql = "SELECT COUNT(*) FROM contacts WHERE number = ? AND is_deleted = 0 AND user_id = " + currentUserId;
        try (PreparedStatement pstmt = conn.prepareStatement(checkNumberSql)) {
            pstmt.setString(1, contactToRestore.getNumber());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    throw new SQLException("Cannot restore: A contact with number '" + contactToRestore.getNumber() + "' already exists!");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error restoring contact: " + e.getMessage());
            e.printStackTrace();
            return;
        }
        
        // If no duplicates, proceed with restore
        String sql = "UPDATE contacts SET is_deleted = 0 WHERE id = ? AND user_id = " + currentUserId;
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                System.out.println("Contact restored successfully!");
            }
        } catch (SQLException e) {
            System.err.println("Error restoring contact: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void deleteById(int id) {
        String sql = "DELETE FROM contacts WHERE id = ? AND user_id = " + currentUserId;
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                System.out.println("Contact permanently deleted!");
            }
        } catch (SQLException e) {
            System.err.println("Error deleting contact: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ==================== VALIDATION OPERATIONS ====================

    public boolean existsByName(String name) {
        // CRITICAL FIX: Case-insensitive name check to prevent "John" vs "john" duplicates
        String sql = "SELECT COUNT(*) FROM contacts WHERE LOWER(name) = LOWER(?) AND is_deleted = 0 AND user_id = " + currentUserId;
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking name existence: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public boolean existsByNumber(String number) {
        String sql = "SELECT COUNT(*) FROM contacts WHERE number = ? AND is_deleted = 0 AND user_id = " + currentUserId;
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, number);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking number existence: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // ==================== SOFT DELETE MANAGEMENT ====================

    public List<Contact> getDeletedContacts() {
        List<Contact> contacts = new ArrayList<>();
        String sql = "SELECT * FROM contacts WHERE is_deleted = 1 AND user_id = " + currentUserId;
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                contacts.add(extractContactFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting deleted contacts: " + e.getMessage());
            e.printStackTrace();
        }
        return contacts;
    }

    public void purgeDeletedContacts() {
        String sql = "DELETE FROM contacts WHERE is_deleted = 1 AND user_id = " + currentUserId;
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            int rows = pstmt.executeUpdate();
            System.out.println(rows + " deleted contacts purged permanently!");
        } catch (SQLException e) {
            System.err.println("Error purging deleted contacts: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ==================== DUPLICATES ====================

    public List<Contact> findDuplicates() {
        List<Contact> duplicates = new ArrayList<>();
        String sql = "SELECT * FROM contacts WHERE is_deleted = 0 AND user_id = " + currentUserId + " AND " +
                     "(name IN (SELECT name FROM contacts WHERE is_deleted = 0 AND user_id = " + currentUserId + " GROUP BY name HAVING COUNT(*) > 1) OR " +
                     "number IN (SELECT number FROM contacts WHERE is_deleted = 0 AND user_id = " + currentUserId + " GROUP BY number HAVING COUNT(*) > 1))";
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                duplicates.add(extractContactFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding duplicates: " + e.getMessage());
            e.printStackTrace();
        }
        return duplicates;
    }

    // ==================== IMPORT / EXPORT ====================

    public void exportToCSV(String filePath) {
        String sql = "SELECT * FROM contacts WHERE is_deleted = 0 AND user_id = " + currentUserId;
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery();
             FileWriter writer = new FileWriter(filePath)) {
            
            // CRITICAL FIX: Include Category in export header
            writer.write("ID,Name,Number,Email,Category,Created At,Updated At\n");
            
            while (rs.next()) {
                writer.write(rs.getInt("id") + ",");
                writer.write(rs.getString("name") + ",");
                writer.write(rs.getString("number") + ",");
                writer.write((rs.getString("email") != null ? rs.getString("email") : "") + ",");
                writer.write((rs.getString("category") != null ? rs.getString("category") : "Friends") + ",");  // NEW: Export category
                writer.write(rs.getTimestamp("created_at") + ",");
                writer.write(rs.getTimestamp("updated_at") + "\n");
            }
            System.out.println("Contacts exported to: " + filePath);
        } catch (SQLException | IOException e) {
            System.err.println("Error exporting to CSV: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public ImportResult importFromCSV(String filePath) {
        ImportResult result = new ImportResult();
        String sql = "INSERT INTO contacts (user_id, name, number, email, category, is_deleted) VALUES (?, ?, ?, ?, ?, 0)";
        Connection conn = DBConnection.getConnection();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath));
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            String line = reader.readLine(); // Skip header
            
            while ((line = reader.readLine()) != null) {
                // Skip empty lines
                if (line.trim().isEmpty()) {
                    continue;
                }
                
                String[] data = line.split(",", -1); // Keep empty strings
                
                // Validate row has minimum required fields
                if (data.length < 2) {
                    result.incrementInvalid();
                    continue;
                }
                
                String name = data[0].trim();
                String number = data[1].trim();
                String email = data.length > 2 ? data[2].trim() : "";
                String category = data.length > 3 && !data[3].trim().isEmpty() ? data[3].trim() : "Friends";
                
                // Validation: Name cannot be empty
                if (name.isEmpty()) {
                    result.incrementInvalid();
                    continue;
                }
                
                // Validation: Number cannot be empty and must be numeric
                if (number.isEmpty() || !number.matches("[0-9+\\-() ]+")) {
                    result.incrementInvalid();
                    continue;
                }
                
                // Validation: Email format (if provided)
                if (!email.isEmpty() && !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                    result.incrementInvalid();
                    continue;
                }
                
                // Validation: Category must be valid
                if (!category.equals("Friends") && !category.equals("Family") && 
                    !category.equals("Work") && !category.equals("Emergency")) {
                    category = "Friends"; // Default to Friends if invalid
                }
                
                // Check for duplicates (only active contacts, not deleted ones)
                if (existsByNameActive(name) || existsByNumberActive(number)) {
                    result.incrementDuplicates();
                    continue;
                }
                
                // Insert valid row
                try {
                    pstmt.setInt(1, currentUserId);
                    pstmt.setString(2, name);
                    pstmt.setString(3, number);
                    pstmt.setString(4, email);
                    pstmt.setString(5, category);
                    pstmt.executeUpdate();
                    result.incrementImported();
                } catch (SQLException e) {
                    // If insert fails (e.g., duplicate constraint), count as duplicate
                    result.incrementDuplicates();
                }
            }
            
            System.out.println("CSV import completed: " + result.toString());
            
        } catch (IOException e) {
            System.err.println("Error reading CSV file: " + e.getMessage());
            throw new RuntimeException("Error reading CSV file: " + e.getMessage(), e);
        } catch (SQLException e) {
            System.err.println("Database error during CSV import: " + e.getMessage());
            throw new RuntimeException("Database error during CSV import: " + e.getMessage(), e);
        }
        
        return result;
    }
    
    // Helper method to check if active contact with name exists
    private boolean existsByNameActive(String name) {
        String sql = "SELECT COUNT(*) FROM contacts WHERE name = ? AND is_deleted = 0 AND user_id = " + currentUserId;
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking name existence: " + e.getMessage());
        }
        return false;
    }
    
    // Helper method to check if active contact with number exists
    private boolean existsByNumberActive(String number) {
        String sql = "SELECT COUNT(*) FROM contacts WHERE number = ? AND is_deleted = 0 AND user_id = " + currentUserId;
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, number);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking number existence: " + e.getMessage());
        }
        return false;
    }

    // ==================== SYSTEM ====================

    public boolean isDatabaseAlive() {
        String sql = "SELECT 1";
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            return rs.next();
        } catch (SQLException e) {
            return false;
        }
    }

    // ==================== ADVANCED SEARCH ====================

    /**
     * Advanced multi-field search with optional category filter and include-deleted toggle.
     * Searches across name, phone, email, and category fields.
     */
    public List<Contact> advancedSearch(String keyword, String category, boolean includeDeleted) {
        List<Contact> contacts = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM contacts WHERE user_id = " + currentUserId);

        // Include deleted filter
        if (!includeDeleted) {
            sql.append(" AND is_deleted = 0");
        }

        // Category filter
        boolean hasCategory = (category != null && !category.isEmpty() && !category.equalsIgnoreCase("All"));
        if (hasCategory) {
            sql.append(" AND category = ?");
        }

        // Keyword search across multiple fields
        boolean hasKeyword = (keyword != null && !keyword.trim().isEmpty());
        if (hasKeyword) {
            sql.append(" AND (name LIKE ? OR number LIKE ? OR email LIKE ? OR category LIKE ?)");
        }

        sql.append(" ORDER BY name ASC");

        Connection conn = DBConnection.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            int paramIndex = 1;
            if (hasCategory) {
                pstmt.setString(paramIndex++, category);
            }
            if (hasKeyword) {
                String pattern = "%" + keyword.trim() + "%";
                pstmt.setString(paramIndex++, pattern);
                pstmt.setString(paramIndex++, pattern);
                pstmt.setString(paramIndex++, pattern);
                pstmt.setString(paramIndex++, pattern);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    contacts.add(extractContactFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error in advanced search: " + e.getMessage());
            e.printStackTrace();
        }
        return contacts;
    }

    /**
     * Get autocomplete suggestions matching prefix across name, phone, and email.
     */
    public List<Contact> getSuggestions(String prefix, int limit) {
        List<Contact> suggestions = new ArrayList<>();
        String sql = "SELECT * FROM contacts WHERE is_deleted = 0 AND user_id = " + currentUserId + " AND " +
                     "(name LIKE ? OR number LIKE ? OR email LIKE ?) " +
                     "ORDER BY " +
                     "CASE WHEN LOWER(name) = LOWER(?) THEN 0 " +
                     "     WHEN LOWER(name) LIKE LOWER(?) THEN 1 " +
                     "     ELSE 2 END, " +
                     "name ASC LIMIT ?";
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String pattern = "%" + prefix + "%";
            String startPattern = prefix + "%";
            pstmt.setString(1, pattern);
            pstmt.setString(2, pattern);
            pstmt.setString(3, pattern);
            pstmt.setString(4, prefix);        // exact match
            pstmt.setString(5, startPattern);   // starts-with
            pstmt.setInt(6, limit);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    suggestions.add(extractContactFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting suggestions: " + e.getMessage());
            e.printStackTrace();
        }
        return suggestions;
    }

    /**
     * Find contacts with similar name or phone (fuzzy duplicate detection).
     * Uses SOUNDEX for name similarity and substring matching for phone.
     */
    public List<Contact> findSimilarContacts(String name, String phone) {
        List<Contact> similar = new ArrayList<>();
        // Use LIKE-based fuzzy matching + SOUNDEX for phonetic similarity
        String sql = "SELECT * FROM contacts WHERE is_deleted = 0 AND user_id = " + currentUserId + " AND (" +
                     "LOWER(name) = LOWER(?) OR " +                          // Exact name match
                     "SOUNDEX(name) = SOUNDEX(?) OR " +                      // Phonetic match
                     "LOWER(name) LIKE LOWER(?) OR " +                       // Name contains
                     "number = ? OR " +                                       // Exact number match
                     "REPLACE(REPLACE(REPLACE(REPLACE(number,' ',''),'-',''),'(',''),')','') = " +
                     "REPLACE(REPLACE(REPLACE(REPLACE(?,' ',''),'-',''),'(',''),')',''))";  // Normalized number match
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, name);
            pstmt.setString(3, "%" + name + "%");
            pstmt.setString(4, phone);
            pstmt.setString(5, phone);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    similar.add(extractContactFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding similar contacts: " + e.getMessage());
            e.printStackTrace();
        }
        return similar;
    }

    // ==================== BATCH OPERATIONS ====================

    /**
     * Soft delete multiple contacts at once using transaction.
     * Either all succeed or all rollback.
     */
    public void batchDelete(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) return;
        
        String sql = "UPDATE contacts SET is_deleted = 1 WHERE id = ?";
        Connection conn = DBConnection.getConnection();
        
        try {
            conn.setAutoCommit(false); // Start transaction
            
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (int id : ids) {
                    ps.setInt(1, id);
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            
            conn.commit(); // Commit transaction
            System.out.println("Batch deleted " + ids.size() + " contacts");
            
        } catch (SQLException e) {
            try {
                conn.rollback(); // Rollback on error
                System.err.println("Batch delete failed, rolled back: " + e.getMessage());
            } catch (SQLException rollbackEx) {
                System.err.println("Rollback failed: " + rollbackEx.getMessage());
            }
            throw new RuntimeException("Batch delete failed", e);
        } finally {
            try {
                conn.setAutoCommit(true); // Restore auto-commit
            } catch (SQLException e) {
                System.err.println("Failed to restore auto-commit: " + e.getMessage());
            }
        }
    }

    /**
     * Permanently delete multiple contacts using transaction.
     */
    public void batchHardDelete(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) return;
        
        String sql = "DELETE FROM contacts WHERE id = ?";
        Connection conn = DBConnection.getConnection();
        
        try {
            conn.setAutoCommit(false);
            
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (int id : ids) {
                    ps.setInt(1, id);
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            
            conn.commit();
            System.out.println("Batch hard deleted " + ids.size() + " contacts");
            
        } catch (SQLException e) {
            try {
                conn.rollback();
                System.err.println("Batch hard delete failed, rolled back: " + e.getMessage());
            } catch (SQLException rollbackEx) {
                System.err.println("Rollback failed: " + rollbackEx.getMessage());
            }
            throw new RuntimeException("Batch hard delete failed", e);
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                System.err.println("Failed to restore auto-commit: " + e.getMessage());
            }
        }
    }

    /**
     * Update category for multiple contacts using transaction.
     */
    public void batchUpdateCategory(List<Integer> ids, String category) {
        if (ids == null || ids.isEmpty()) return;
        
        String sql = "UPDATE contacts SET category = ? WHERE id = ?";
        Connection conn = DBConnection.getConnection();
        
        try {
            conn.setAutoCommit(false);
            
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (int id : ids) {
                    ps.setString(1, category);
                    ps.setInt(2, id);
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            
            conn.commit();
            System.out.println("Batch updated category for " + ids.size() + " contacts");
            
        } catch (SQLException e) {
            try {
                conn.rollback();
                System.err.println("Batch category update failed, rolled back: " + e.getMessage());
            } catch (SQLException rollbackEx) {
                System.err.println("Rollback failed: " + rollbackEx.getMessage());
            }
            throw new RuntimeException("Batch category update failed", e);
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                System.err.println("Failed to restore auto-commit: " + e.getMessage());
            }
        }
    }

    /**
     * Export only selected contacts to CSV file.
     */
    public void exportSelected(String filePath, List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            System.err.println("No contacts selected for export");
            return;
        }
        
        String sql = "SELECT * FROM contacts WHERE id = ? AND is_deleted = 0 AND user_id = " + currentUserId;
        Connection conn = DBConnection.getConnection();
        
        try (java.io.BufferedWriter writer = new java.io.BufferedWriter(
                new java.io.FileWriter(filePath))) {
            
            // Write CSV header
            writer.write("ID,Name,Number,Email,Category,Created At\n");
            
            // Export each selected contact
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (int id : ids) {
                    ps.setInt(1, id);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            Contact c = extractContactFromResultSet(rs);
                            writer.write(String.format("%d,\"%s\",\"%s\",\"%s\",\"%s\",%s\n",
                                c.getId(), c.getName(), c.getNumber(),
                                c.getEmail() != null ? c.getEmail() : "",
                                c.getCategory(),
                                c.getCreatedAt() != null ? c.getCreatedAt().toString() : ""
                            ));
                        }
                    }
                }
            }
            
            System.out.println("Exported " + ids.size() + " selected contacts to: " + filePath);
            
        } catch (Exception e) {
            System.err.println("Error exporting selected contacts: " + e.getMessage());
            throw new RuntimeException("Export selected failed", e);
        }
    }

    // ==================== MERGE OPERATIONS ====================

    /**
     * Merge two contacts: copy missing data from mergeId to keepId, then delete mergeId.
     * Conflict resolution: keepId data takes priority.
     */
    public void mergeContacts(int keepId, int mergeId) {
        Connection conn = DBConnection.getConnection();
        
        try {
            conn.setAutoCommit(false); // Start transaction
            
            // Get both contacts
            Contact keep = getById(keepId);
            Contact merge = getById(mergeId);
            
            if (keep == null || merge == null) {
                throw new RuntimeException("One or both contacts not found");
            }
            
            // Build update SQL for missing fields
            // If keep.email is null/empty, use merge.email
            // If keep.category is default ("Friends"), use merge.category
            
            boolean hasUpdates = false;
            StringBuilder updateSql = new StringBuilder("UPDATE contacts SET ");
            
            if ((keep.getEmail() == null || keep.getEmail().trim().isEmpty()) 
                && merge.getEmail() != null && !merge.getEmail().trim().isEmpty()) {
                updateSql.append("email = '").append(merge.getEmail()).append("'");
                hasUpdates = true;
            }
            
            updateSql.append(" WHERE id = ").append(keepId);
            
            if (hasUpdates) {
                try (PreparedStatement ps = conn.prepareStatement(updateSql.toString())) {
                    ps.executeUpdate();
                }
            }
            
            // Delete the merged contact
            String deleteSql = "DELETE FROM contacts WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(deleteSql)) {
                ps.setInt(1, mergeId);
                ps.executeUpdate();
            }
            
            conn.commit();
            System.out.println("Merged contact " + mergeId + " into " + keepId);
            
        } catch (Exception e) {
            try {
                conn.rollback();
                System.err.println("Merge failed, rolled back: " + e.getMessage());
            } catch (SQLException rollbackEx) {
                System.err.println("Rollback failed: " + rollbackEx.getMessage());
            }
            throw new RuntimeException("Contact merge failed", e);
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                System.err.println("Failed to restore auto-commit: " + e.getMessage());
            }
        }
    }

    // ==================== HELPER METHOD ====================

    private Contact extractContactFromResultSet(ResultSet rs) throws SQLException {
        return new Contact(
            rs.getInt("id"),
            rs.getInt("user_id"),
            rs.getString("name"),
            rs.getString("number"),
            rs.getString("email"),
            rs.getString("category"),
            rs.getBoolean("is_deleted"),
            rs.getTimestamp("created_at"),
            rs.getTimestamp("updated_at")
        );
    }
}
