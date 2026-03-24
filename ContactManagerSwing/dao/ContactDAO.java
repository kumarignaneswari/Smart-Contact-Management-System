package dao;

import model.Contact;
import model.ImportResult;
import java.util.List;

/**
 * Contact DAO Interface
 * 
 * Defines all database operations for Contact entity.
 * This interface ensures proper separation between DAO contract and implementation.
 * All methods use PreparedStatement to prevent SQL injection.
 * All queries are automatically scoped to the currently logged-in user.
 */
public interface ContactDAO {

    // Set the current logged-in user's ID (call once after login)
    void setCurrentUserId(int userId);

    // CREATE operations
    void addContact(Contact c);
    void addContacts(List<Contact> contacts);
    
    // READ operations
    List<Contact> getAllContacts();
    Contact getById(int id);
    Contact getByName(String name);
    Contact getByNumber(String number);
    List<Contact> search(String keyword);
    List<Contact> searchByCategory(String category);  // Filter by category
    List<Contact> searchByNamePrefix(String prefix);  // Search names starting with prefix
    List<Contact> getContacts(int offset, int limit);
    List<Contact> getAllSortedByName();
    int countContacts();
    
    // ADVANCED SEARCH: Multi-field + category filter + include deleted
    List<Contact> advancedSearch(String keyword, String category, boolean includeDeleted);
    
    // AUTOCOMPLETE: Get suggestion contacts matching prefix (limited)
    List<Contact> getSuggestions(String prefix, int limit);
    
    // FUZZY DUPLICATE DETECTION: Find contacts similar to given name/phone
    List<Contact> findSimilarContacts(String name, String phone);
    
    // UPDATE operations
    void updateContact(Contact c);
    void updateEmail(int id, String email);
    void updateNumber(int id, String number);
    
    // DELETE operations
    void softDeleteById(int id);
    void restoreContact(int id);
    void deleteById(int id);
    
    // VALIDATION operations
    boolean existsByName(String name);
    boolean existsByNumber(String number);
    
    // SOFT DELETE MANAGEMENT
    List<Contact> getDeletedContacts();
    void purgeDeletedContacts();
    
    // DUPLICATES
    List<Contact> findDuplicates();
    
    // BATCH OPERATIONS
    void batchDelete(List<Integer> ids);  // Soft delete multiple contacts
    void batchHardDelete(List<Integer> ids);  // Permanently delete multiple contacts
    void batchUpdateCategory(List<Integer> ids, String category);  // Update category for multiple contacts
    void exportSelected(String filePath, List<Integer> ids);  // Export selected contacts to CSV
    
    // MERGE OPERATIONS
    void mergeContacts(int keepId, int mergeId);  // Merge mergeId into keepId, delete mergeId
    
    // IMPORT / EXPORT
    void exportToCSV(String filePath);
    ImportResult importFromCSV(String filePath);
    
    // SYSTEM
    boolean isDatabaseAlive();
}
