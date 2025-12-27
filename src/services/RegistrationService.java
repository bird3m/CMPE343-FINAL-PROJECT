package services;

import models.User;

/**
 * Registration Service - CUSTOMER REGISTRATION ONLY
 * 
 * Handles new customer registration with validation.
 * Owner and Carrier accounts are created by system admin only.
 * 
 * @author Group04
 * @version 1.0
 */
public class RegistrationService {
    
    private UserDAO userDAO;
    private AuthenticationService authService;
    
    /**
     * Constructor
     */
    public RegistrationService() {
        this.userDAO = new UserDAO();
        this.authService = new AuthenticationService();
    }
    
    /**
     * Register new customer
     * 
     * @param username Unique username (3-20 chars)
     * @param password Password (min 4 chars)
     * @param address Delivery address
     * @param phone Phone number (optional)
     * @return true if registration successful, false otherwise
     */
    public boolean registerCustomer(String username, String password, 
                                    String address, String phone) {
        try {
            // Check if username already exists
            if (userDAO.usernameExists(username)) {
                return false;
            }
            
            // Hash password for security
            String hashedPassword = authService.hashPassword(password);
            
            // Create new customer user
            User newCustomer = new User();
            newCustomer.setUsername(username);
            newCustomer.setPassword(hashedPassword);
            newCustomer.setRole("customer");
            newCustomer.setAddress(address);
            newCustomer.setPhone(phone);
            newCustomer.setFullName(username); // Default to username
            
            // Save to database
            return userDAO.createUser(newCustomer);
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Validate username format
     * 
     * Rules:
     * - 3 to 20 characters
     * - Letters, numbers, underscore only
     * - Must start with letter
     * 
     * @param username Username to validate
     * @return Error message or null if valid
     */
    public String validateUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return "Username cannot be empty";
        }
        
        username = username.trim();
        
        if (username.length() < 3) {
            return "Username must be at least 3 characters";
        }
        
        if (username.length() > 20) {
            return "Username must be at most 20 characters";
        }
        
        if (!username.matches("^[a-zA-Z][a-zA-Z0-9_]*$")) {
            return "Username must start with letter and contain only letters, numbers, and underscore";
        }
        
        return null; // Valid
    }
    
    /**
     * Validate password strength
     * 
     * Rules:
     * - At least 4 characters
     * - (Additional rules can be added)
     * 
     * @param password Password to validate
     * @return Error message or null if valid
     */
    public String validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            return "Password cannot be empty";
        }
        
        if (password.length() < 4) {
            return "Password must be at least 4 characters";
        }
        
        // Optional: Add more strict rules
        // if (!password.matches(".*[A-Z].*")) {
        //     return "Password must contain at least one uppercase letter";
        // }
        
        return null; // Valid
    }
    
    /**
     * Validate phone number format (optional field)
     * 
     * @param phone Phone number
     * @return Error message or null if valid
     */
    public String validatePhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return null; // Phone is optional
        }
        
        phone = phone.trim();
        
        // Remove common separators for validation
        String cleaned = phone.replaceAll("[\\s\\-\\(\\)]", "");
        
        // Check if it's all digits (with optional + at start)
        if (!cleaned.matches("^\\+?[0-9]{10,15}$")) {
            return "Phone number must be 10-15 digits (e.g., +90 532 123 4567)";
        }
        
        return null; // Valid
    }
}