package service;

import dao.ContactDAO;
import dao.ContactDAOImpl;
import model.Contact;
import model.ImportResult;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Contact Service Layer
 * 
 * This is the ONLY class that Swing UI should call.
 * Acts as a bridge between UI and DAO layers.
 * Can contain business logic and validation.
 * UI must NEVER call DAO directly - always use Service.
 */
public class ContactService {
    
    private ContactDAO contactDAO;
    private int currentUserId = -1;

    // Constructor initializes DAO implementation
    public ContactService() {
        this.contactDAO = new ContactDAOImpl();
    }

    /**
     * Set the currently logged-in user.
     * Must be called once after login before any contact operations.
     */
    public void setCurrentUserId(int userId) {
        this.currentUserId = userId;
        contactDAO.setCurrentUserId(userId);
    }

    /** Returns the currently logged-in user ID. */
    public int getCurrentUserId() {
        return currentUserId;
    }

    // ==================== CREATE OPERATIONS ====================

    public void addContact(Contact c) {
        contactDAO.addContact(c);
    }

    public void addContacts(List<Contact> contacts) {
        contactDAO.addContacts(contacts);
    }

    // ==================== READ OPERATIONS ====================

    public List<Contact> getAllContacts() {
        return contactDAO.getAllContacts();
    }

    public Contact getById(int id) {
        return contactDAO.getById(id);
    }

    public Contact getByName(String name) {
        return contactDAO.getByName(name);
    }

    public Contact getByNumber(String number) {
        return contactDAO.getByNumber(number);
    }

    public List<Contact> search(String keyword) {
        return contactDAO.search(keyword);
    }

    // NEW: Search by category
    public List<Contact> searchByCategory(String category) {
        return contactDAO.searchByCategory(category);
    }

    // NEW: Search by name prefix (names starting with...)
    public List<Contact> searchByNamePrefix(String prefix) {
        return contactDAO.searchByNamePrefix(prefix);
    }

    public List<Contact> getContacts(int offset, int limit) {
        return contactDAO.getContacts(offset, limit);
    }

    public List<Contact> getAllSortedByName() {
        return contactDAO.getAllSortedByName();
    }

    public int countContacts() {
        return contactDAO.countContacts();
    }

    // ==================== UPDATE OPERATIONS ====================

    public void updateContact(Contact c) {
        // CRITICAL FIX: Check if updated name/number conflicts with OTHER contacts
        // Get the existing contact to compare
        Contact existing = contactDAO.getById(c.getId());
        if (existing == null) {
            throw new RuntimeException("Contact not found!");
        }
        
        // If name changed, check for duplicate (excluding self)
        if (!existing.getName().equalsIgnoreCase(c.getName())) {
            Contact duplicate = contactDAO.getByName(c.getName());
            if (duplicate != null && duplicate.getId() != c.getId()) {
                throw new RuntimeException("A contact with name '" + c.getName() + "' already exists!");
            }
        }
        
        // If number changed, check for duplicate (excluding self)
        if (!existing.getNumber().equals(c.getNumber())) {
            Contact duplicate = contactDAO.getByNumber(c.getNumber());
            if (duplicate != null && duplicate.getId() != c.getId()) {
                throw new RuntimeException("A contact with number '" + c.getNumber() + "' already exists!");
            }
        }
        
        contactDAO.updateContact(c);
    }

    public void updateEmail(int id, String email) {
        contactDAO.updateEmail(id, email);
    }

    public void updateNumber(int id, String number) {
        contactDAO.updateNumber(id, number);
    }

    // ==================== DELETE OPERATIONS ====================

    public void softDeleteById(int id) {
        contactDAO.softDeleteById(id);
    }

    public void restoreContact(int id) {
        contactDAO.restoreContact(id);
    }

    public void deleteById(int id) {
        contactDAO.deleteById(id);
    }

    // ==================== VALIDATION OPERATIONS ====================

    public boolean existsByName(String name) {
        return contactDAO.existsByName(name);
    }

    public boolean existsByNumber(String number) {
        return contactDAO.existsByNumber(number);
    }

    // ==================== SOFT DELETE MANAGEMENT ====================

    public List<Contact> getDeletedContacts() {
        return contactDAO.getDeletedContacts();
    }

    public void purgeDeletedContacts() {
        contactDAO.purgeDeletedContacts();
    }

    // ==================== DUPLICATES ====================

    public List<Contact> findDuplicates() {
        return contactDAO.findDuplicates();
    }

    // ==================== IMPORT / EXPORT ====================

    public void exportToCSV(String filePath) {
        contactDAO.exportToCSV(filePath);
    }

    public ImportResult importFromCSV(String filePath) {
        return contactDAO.importFromCSV(filePath);
    }

    // ==================== SYSTEM ====================

    public boolean isDatabaseAlive() {
        return contactDAO.isDatabaseAlive();
    }

    // ==================== BUSINESS LOGIC / VALIDATION ====================

    /**
     * Validate contact before adding
     * Returns error message if invalid, null if valid
     */
    public String validateContact(Contact c) {
        if (c.getName() == null || c.getName().trim().isEmpty()) {
            return "Name cannot be empty!";
        }
        if (c.getNumber() == null || c.getNumber().trim().isEmpty()) {
            return "Number cannot be empty!";
        }
        if (c.getName().length() > 50) {
            return "Name cannot exceed 50 characters!";
        }
        if (c.getNumber().length() > 15) {
            return "Number cannot exceed 15 characters!";
        }
        // NEW: Validate number format - must contain only digits, spaces, +, -, (, )
        if (!c.getNumber().matches("[0-9+\\-() ]+")) {
            return "Number can only contain digits, spaces, +, -, (, )";
        }
        // NEW: Validate email format if provided
        if (c.getEmail() != null && !c.getEmail().trim().isEmpty()) {
            if (c.getEmail().length() > 50) {
                return "Email cannot exceed 50 characters!";
            }
            if (!c.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                return "Invalid email format!";
            }
        }
        // NEW: Validate category
        if (c.getCategory() == null || c.getCategory().trim().isEmpty()) {
            return "Category must be selected!";
        }
        return null; // Valid
    }

    /**
     * Validate contact for update (includes ID check)
     */
    public String validateContactForUpdate(Contact c) {
        if (c.getId() <= 0) {
            return "Invalid contact ID!";
        }
        return validateContact(c);
    }

    // ==================== ADVANCED SEARCH ====================

    /**
     * Advanced search with multi-field keyword, category filter, and include-deleted toggle.
     */
    public List<Contact> advancedSearch(String keyword, String category, boolean includeDeleted) {
        return contactDAO.advancedSearch(keyword, category, includeDeleted);
    }

    /**
     * Smart ranked search: returns results sorted by relevance.
     * - Exact name match → highest
     * - Name starts with keyword → high
     * - Name contains keyword → medium
     * - Phone/email/category match → lower
     */
    public List<Contact> smartSearch(String keyword, String category, boolean includeDeleted) {
        List<Contact> results = contactDAO.advancedSearch(keyword, category, includeDeleted);
        if (keyword == null || keyword.trim().isEmpty()) return results;

        final String kw = keyword.trim().toLowerCase();

        // Sort by relevance score (lower score = higher rank)
        results.sort(Comparator.comparingInt(c -> computeRelevanceScore(c, kw)));
        return results;
    }

    /**
     * Compute relevance score for a contact (lower = more relevant).
     */
    private int computeRelevanceScore(Contact c, String keyword) {
        String name = c.getName() != null ? c.getName().toLowerCase() : "";
        String number = c.getNumber() != null ? c.getNumber().toLowerCase() : "";
        String email = c.getEmail() != null ? c.getEmail().toLowerCase() : "";

        // Exact name match
        if (name.equals(keyword)) return 0;
        // Name starts with keyword
        if (name.startsWith(keyword)) return 1;
        // Name contains keyword
        if (name.contains(keyword)) return 2;
        // Phone exact match
        if (number.equals(keyword)) return 3;
        // Phone contains
        if (number.contains(keyword)) return 4;
        // Email contains
        if (email.contains(keyword)) return 5;
        // Category or other
        return 6;
    }

    /**
     * Get autocomplete suggestions for the search box.
     */
    public List<Contact> getSuggestions(String prefix, int limit) {
        return contactDAO.getSuggestions(prefix, limit);
    }

    // ==================== SMART DUPLICATE DETECTION ====================

    /**
     * Find contacts that are similar to the given name/phone.
     * Used for intelligent "similar contact found" detection.
     */
    public List<Contact> findSimilarContacts(String name, String phone) {
        return contactDAO.findSimilarContacts(name, phone);
    }

    /**
     * Check for similar contacts excluding a specific ID (for edit mode).
     */
    public List<Contact> findSimilarContactsExcluding(String name, String phone, int excludeId) {
        List<Contact> similar = contactDAO.findSimilarContacts(name, phone);
        List<Contact> filtered = new ArrayList<>();
        for (Contact c : similar) {
            if (c.getId() != excludeId) {
                filtered.add(c);
            }
        }
        return filtered;
    }

    // ==================== BATCH OPERATIONS ====================

    /**
     * Batch soft delete multiple contacts.
     * Uses transaction - all succeed or all rollback.
     */
    public void batchDelete(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new RuntimeException("No contacts selected for batch delete");
        }
        contactDAO.batchDelete(ids);
    }

    /**
     * Batch permanently delete multiple contacts.
     */
    public void batchHardDelete(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new RuntimeException("No contacts selected for batch hard delete");
        }
        contactDAO.batchHardDelete(ids);
    }

    /**
     * Batch update category for multiple contacts.
     */
    public void batchUpdateCategory(List<Integer> ids, String category) {
        if (ids == null || ids.isEmpty()) {
            throw new RuntimeException("No contacts selected for category update");
        }
        if (category == null || category.trim().isEmpty()) {
            throw new RuntimeException("Category cannot be empty");
        }
        contactDAO.batchUpdateCategory(ids, category);
    }

    /**
     * Export only selected contacts to CSV.
     */
    public void exportSelected(String filePath, List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new RuntimeException("No contacts selected for export");
        }
        contactDAO.exportSelected(filePath, ids);
    }

    // ==================== MERGE OPERATIONS ====================

    /**
     * Merge two contacts: keep one, merge data from the other, delete the merged one.
     * Validates that both contacts exist before merging.
     */
    public void mergeContacts(int keepId, int mergeId) {
        Contact keep = contactDAO.getById(keepId);
        Contact merge = contactDAO.getById(mergeId);
        
        if (keep == null) {
            throw new RuntimeException("Contact to keep (ID: " + keepId + ") not found");
        }
        if (merge == null) {
            throw new RuntimeException("Contact to merge (ID: " + mergeId + ") not found");
        }
        if (keepId == mergeId) {
            throw new RuntimeException("Cannot merge a contact with itself");
        }
        
        contactDAO.mergeContacts(keepId, mergeId);
    }
}
