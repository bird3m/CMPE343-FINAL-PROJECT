package services;

import models.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) for User operations.
 * FIXED: mapRowToUser now correctly assigns fullName.
 *
 * @author Group04
 * @version 2.1
 */
public class UserDAO {

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
            e.printStackTrace();
        }
        return users;
    }
    
    public List<User> getAllCarriers() {
        return getUsersByRole("carrier");
    }

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
            e.printStackTrace();
        }
        return null;
    }

    public boolean deleteUser(int userId) {
        String sql = "DELETE FROM userinfo WHERE id = ?";
        try (Connection conn = DatabaseAdapter.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- USER MAPPING (HATA BURADAYDI, DÜZELDİ) ---
    private User mapRowToUser(ResultSet rs) throws SQLException {
        // Önce nesneyi oluşturup bir değişkene atıyoruz
        User user = new User(
            rs.getInt("id"),
            rs.getString("username"),
            rs.getString("password_hash"),
            rs.getString("role"),
            rs.getString("address"),
            rs.getString("phone")
        );
        
        // Sonra eksik bilgiyi ekliyoruz
        user.setFullName(rs.getString("full_name")); 
        
        return user;
    }
    
    /**
 * Checks if a username is already taken by ANY user in the system.
 * This prevents duplicate usernames regardless of role.
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
            System.err.println("Error checking global username: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public boolean createUser(User user) {
        String sql = "INSERT INTO userinfo (username, password_hash, role, address, phone, full_name) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseAdapter.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword()); 
            pstmt.setString(3, user.getRole());
            pstmt.setString(4, user.getAddress());
            pstmt.setString(5, user.getPhone());
            pstmt.setString(6, user.getFullName());
            
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

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
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Update user's full name by username (helper for sample data fixes)
     */
    public boolean updateFullNameByUsername(String username, String fullName) {
        String sql = "UPDATE userinfo SET full_name = ? WHERE username = ?";
        try (Connection conn = DatabaseAdapter.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, fullName);
            pstmt.setString(2, username);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}