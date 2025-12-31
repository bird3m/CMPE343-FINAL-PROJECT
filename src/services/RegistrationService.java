package services;

import java.sql.Connection;
import java.sql.PreparedStatement;

/**
 * Registration Service
 * 
 * Handles new customer registration with validation.
 * 
 * @author Group04
 * @version 1.0
 */
public class RegistrationService {
    
    private UserDAO userDAO;

    public RegistrationService() {
        this.userDAO = new UserDAO();
    }
    
    /**
     * Register new customer
     * 
     * @param username Unique username (3-20 chars)
     * @param password Password (min 4 chars)
     * @param address Delivery address
     * @param phone Phone number (optional)
     * @param fullName Customer's full name
     * @return true if registration successful, false otherwise
     */
    public boolean registerCustomer(String username, String password, 
                                    String address, String phone, String fullName) {
        // Check if username already exists
        if (userDAO.usernameExists(username)) {
            return false;
        }

        // Hash password for security
        String hashedPassword = AuthenticationService.hashPassword(password);
        
        // SQL with full_name parameter
        String sql = "INSERT INTO userinfo (username, password_hash, role, address, phone, full_name) " +
                     "VALUES (?, ?, 'customer', ?, ?, ?)";

        try (Connection conn = DatabaseAdapter.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            pstmt.setString(2, hashedPassword);
            pstmt.setString(3, address);
            pstmt.setString(4, phone);
            pstmt.setString(5, fullName); 
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Registration SQL ERROR: " + e.getMessage());
            return false;
        }
    }
}