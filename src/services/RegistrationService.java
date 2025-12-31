package services;

import models.User;
import utils.InputValidation; // <--- YENİ: Validator sınıfımızı çağırdık
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Registration Service.
 * Handles new customer registration with validation using InputValidation utility.
 * * @author Group04
 * @version 2.0 (InputValidation Integrated)
 */
public class RegistrationService {
    
    //use SQL for register
    private UserDAO userDAO; 

    public RegistrationService() {
        this.userDAO = new UserDAO(); //prevent null pointer errors
    }
    
    /**
     * Register new customer.
     * Uses raw SQL to ensure 'full_name' is inserted correctly.
     */
    public boolean registerCustomer(String username, String password, String address, String phone) {
       
        if (userDAO.usernameExists(username)) {
            return false;
        }

        
        String hashedPassword = AuthenticationService.hashPassword(password);
        
        String sql = "INSERT INTO userinfo (username, password_hash, role, address, phone, full_name) VALUES (?, ?, 'customer', ?, ?, ?)";

        try (Connection conn = DatabaseAdapter.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            pstmt.setString(2, hashedPassword);
            pstmt.setString(3, address);
            pstmt.setString(4, phone);
            // KRİTİK NOKTA: full_name boş olamaz, username'i yazıyoruz.
            pstmt.setString(5, username); 
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("🚨 SQL ERROR " + e.getMessage());
            return false;
        }
    }
    

    /**
     * Validate username format using centralized InputValidation.
     */
    public String validateUsername(String username) {
        return InputValidation.validateUsername(username);
    }
    
    /**
     * Validate password strength using centralized InputValidation.
     */
    public String validatePassword(String password) {
        return InputValidation.validatePassword(password);
    }
    
    /**
     * Validate phone number format using centralized InputValidation.
     */
    public String validatePhone(String phone) {
        return InputValidation.validatePhone(phone);
    }
}