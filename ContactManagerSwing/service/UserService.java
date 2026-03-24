package service;

import dao.UserDAO;
import dao.UserDAOImpl;
import model.User;

/**
 * User Service Layer
 *
 * Handles business logic for user registration and login.
 * UI classes must use this service instead of calling UserDAO directly.
 */
public class UserService {

    private final UserDAO userDAO;

    public UserService() {
        this.userDAO = new UserDAOImpl();
    }

    /**
     * Register a new user.
     *
     * @param username Desired username
     * @param password Desired password
     * @param email    User email (optional)
     * @return Generated user ID on success, or -1 on failure
     * @throws RuntimeException if username is taken or inputs are invalid
     */
    public int register(String username, String password, String email) {
        // Validate inputs
        if (username == null || username.trim().isEmpty()) {
            throw new RuntimeException("Username cannot be empty!");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new RuntimeException("Password cannot be empty!");
        }
        if (username.length() > 50) {
            throw new RuntimeException("Username cannot exceed 50 characters!");
        }
        if (password.length() < 4) {
            throw new RuntimeException("Password must be at least 4 characters!");
        }

        // Check for duplicate username
        if (userDAO.usernameExists(username.trim())) {
            throw new RuntimeException("Username '" + username.trim() + "' is already taken!");
        }

        User user = new User(username.trim(), password.trim(),
                             email != null ? email.trim() : "");
        int newId = userDAO.registerUser(user);
        if (newId == -1) {
            throw new RuntimeException("Registration failed! Please try again.");
        }
        return newId;
    }

    /**
     * Authenticate a user.
     *
     * @param username Entered username
     * @param password Entered password
     * @return Authenticated User object
     * @throws RuntimeException if credentials are invalid
     */
    public User login(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            throw new RuntimeException("Username cannot be empty!");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new RuntimeException("Password cannot be empty!");
        }

        User user = userDAO.loginUser(username.trim(), password.trim());
        if (user == null) {
            throw new RuntimeException("Invalid username or password!");
        }
        return user;
    }

    /**
     * Check if a username is already taken (for live validation in registration form).
     */
    public boolean usernameExists(String username) {
        return userDAO.usernameExists(username);
    }

    /**
     * Get a user by their ID.
     */
    public User getUserById(int id) {
        return userDAO.getUserById(id);
    }
}
