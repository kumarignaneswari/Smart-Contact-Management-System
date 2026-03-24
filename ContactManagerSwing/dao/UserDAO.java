package dao;

import model.User;

/**
 * User DAO Interface
 *
 * Defines database operations for the User entity.
 * Handles registration, login verification, and user lookup.
 */
public interface UserDAO {

    // Register a new user; returns the generated user ID
    int registerUser(User user);

    // Verify login credentials; returns the matching User or null
    User loginUser(String username, String password);

    // Check if a username is already taken
    boolean usernameExists(String username);

    // Find user by ID
    User getUserById(int id);

    // Find user by username
    User getUserByUsername(String username);
}
