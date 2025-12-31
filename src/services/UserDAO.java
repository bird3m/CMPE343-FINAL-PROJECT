package services;

import models.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) for User operations.
 * Handles database interactions related to the 'userinfo' table.
 * Used primarily by the Owner role to manage carriers and by the system to retrieve user details.
 *
 * @author Group04
 * @version 2.0
 */
public class UserDAO {

    /**
     * Retrieves a list of users based on their specific role.
     * Useful for filtering users, e.g., listing all carriers for the Owner dashboard.
     *
     * @param role The role to filter by (e.g., "carrier", "customer").
     * @return A List of User objects matching the role.
     */
    public List<User> getUsersByRole(String role) {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM userinfo WHERE role = ?";
        
        try (Connection conn = DatabaseAdapter.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, role);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    users.add(mapRowToUser(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving users by role: " + e.getMessage());
            e.printStackTrace();
        }
        return users;
    }
    
    /**
     * Convenience method to retrieve all users with the 'carrier' role.
     * Used in the Owner interface to display the list of employees.
     *
     * @return A List of User objects representing all carriers.
     */
    public List<User> getAllCarriers() {
        return getUsersByRole("carrier");
    }

    /**
     * Retrieves a specific user by their unique ID.
     * Can be used to find the owner of a specific order.
     *
     * @param id The unique ID of the user.
     * @return The User object if found, null otherwise.
     */
    public User getUserById(int id) {
        String sql = "SELECT * FROM userinfo WHERE id = ?";
        try (Connection conn = DatabaseAdapter.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToUser(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving user by ID: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Deletes a user from the system based on their ID.
     * This operation is typically performed by the Owner (e.g., firing a carrier).
     *
     * @param userId The ID of the user to be deleted.
     * @return true if the deletion was successful, false otherwise.
     */
    public boolean deleteUser(int userId) {
        String sql = "DELETE FROM userinfo WHERE id = ?";
        try (Connection conn = DatabaseAdapter.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            int affected = pstmt.executeUpdate();
            return affected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error deleting user: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Helper method to map a single row from the ResultSet to a User object.
     * Ensures mapping consistency between database columns and the Java model.
     * <p>
     * Note: Maps 'password_hash' column to the User's password field.
     * </p>
     *
     * @param rs The ResultSet positioned at the current row.
     * @return A populated User object.
     * @throws SQLException If a database access error occurs.
     */
    private User mapRowToUser(ResultSet rs) throws SQLException {
        // IMPORTANT: 'password_hash' is the column name in the database.
        // We map it to the 'password' field in the User model.
        return new User(
            rs.getInt("id"),
            rs.getString("username"),
            rs.getString("password_hash"), // Correct column name mapping
            rs.getString("role"),
            rs.getString("address"),
            rs.getString("phone")
        );
    }
    
    /**
 * Check if username already exists in database
 * 
 * @param username Username to check
 * @return true if exists, false otherwise
 */
public boolean usernameExists(String username) {
    String sql = "SELECT COUNT(*) FROM userinfo WHERE username = ?";
    
    try (Connection conn = DatabaseAdapter.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        
        pstmt.setString(1, username);
        ResultSet rs = pstmt.executeQuery();
        
        if (rs.next()) {
            return rs.getInt(1) > 0;
        }
    } catch (SQLException e) {
        System.err.println("Error checking username: " + e.getMessage());
        e.printStackTrace();
    }
    return false;
}

/**
 * Create new user in database
 * 
 * @param user User object to create
 * @return true if successful, false otherwise
 */
public boolean createUser(User user) {
    String sql = "INSERT INTO userinfo (username, password_hash, role, address, phone, full_name) " +
                 "VALUES (?, ?, ?, ?, ?, ?)";
    
    try (Connection conn = DatabaseAdapter.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        
        pstmt.setString(1, user.getUsername());
        pstmt.setString(2, user.getPassword()); // Already hashed
        pstmt.setString(3, user.getRole());
        pstmt.setString(4, user.getAddress());
        pstmt.setString(5, user.getPhone());
        pstmt.setString(6, user.getFullName());
        
        return pstmt.executeUpdate() > 0;
        
    } catch (SQLException e) {
        System.err.println("Error creating user: " + e.getMessage());
        e.printStackTrace();
        return false;
    }
}

/**
 * Update existing user information
 * 
 * @param user User object with updated data
 * @return true if successful, false otherwise
 */
public boolean updateUser(User user) {
    String sql = "UPDATE userinfo SET address = ?, phone = ?, full_name = ? WHERE id = ?";
    
    try (Connection conn = DatabaseAdapter.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        
        pstmt.setString(1, user.getAddress());
        pstmt.setString(2, user.getPhone());
        pstmt.setString(3, user.getFullName());
        pstmt.setInt(4, user.getId());
        
        return pstmt.executeUpdate() > 0;
        
    } catch (SQLException e) {
        System.err.println("Error updating user: " + e.getMessage());
        e.printStackTrace();
        return false;
    }
}


    
}