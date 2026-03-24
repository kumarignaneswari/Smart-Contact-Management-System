package model;

import java.sql.Timestamp;

/**
 * Contact Model Class
 * 
 * Represents a single contact entity with all database fields.
 * Used across DAO, Service, and UI layers.
 * This is a simple POJO (Plain Old Java Object) with getters and setters.
 */
public class Contact {
    private int id;
    private int userId;   // NEW: owner user's ID
    private String name;
    private String number;
    private String email;
    private String category;  // NEW: Friends, Family, Work, Emergency
    private boolean isDeleted;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // Default constructor
    public Contact() {
    }

    // Constructor without ID (for creating new contacts)
    public Contact(String name, String number, String email) {
        this.name = name;
        this.number = number;
        this.email = email;
        this.category = "Friends";  // Default category
        this.isDeleted = false;
    }

    // Constructor with userId and basic fields (for creating new contacts)
    public Contact(int userId, String name, String number, String email) {
        this.userId = userId;
        this.name = name;
        this.number = number;
        this.email = email;
        this.category = "Friends";
        this.isDeleted = false;
    }
    
    // Constructor with category (for creating new contacts with category)
    public Contact(String name, String number, String email, String category) {
        this.name = name;
        this.number = number;
        this.email = email;
        this.category = category;
        this.isDeleted = false;
    }

    // Constructor with userId + category (for creating new contacts)
    public Contact(int userId, String name, String number, String email, String category) {
        this.userId = userId;
        this.name = name;
        this.number = number;
        this.email = email;
        this.category = category;
        this.isDeleted = false;
    }

    // Constructor with all fields (for retrieving from database)
    public Contact(int id, int userId, String name, String number, String email, String category,
                   boolean isDeleted, Timestamp createdAt, Timestamp updatedAt) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.number = number;
        this.email = email;
        this.category = category;
        this.isDeleted = isDeleted;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Legacy constructor without userId (kept for backward compatibility)
    public Contact(int id, String name, String number, String email, String category,
                   boolean isDeleted, Timestamp createdAt, Timestamp updatedAt) {
        this.id = id;
        this.name = name;
        this.number = number;
        this.email = email;
        this.category = category;
        this.isDeleted = isDeleted;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String toString() {
        return "Contact{" +
                "id=" + id +
                ", userId=" + userId +
                ", name='" + name + '\'' +
                ", number='" + number + '\'' +
                ", email='" + email + '\'' +
                ", category='" + category + '\'' +
                ", isDeleted=" + isDeleted +
                '}';
    }
}
