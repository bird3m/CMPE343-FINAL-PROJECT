package services;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    //LOGIN
    public User login(String username, String passwordHash) {
        String sql = "SELECT * FROM userinfo WHERE username = ? AND password_hash = ?";
        
        try (Connection conn = DatabaseAdapter.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            pstmt.setString(2, passwordHash); 
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 2. KULLANICI ADI KONTROLÜ (Register öncesi şart!)
    public boolean isUsernameTaken(String username) {
        String sql = "SELECT count(*) FROM userinfo WHERE username = ?";
        try (Connection conn = DatabaseAdapter.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // Eğer sayi > 0 ise kullanıcı adı alınmış demektir
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false; 
    }

    // 3. YENİ KULLANICI EKLE (Register / Hire Carrier)
    public boolean addUser(User user) {
        String sql = "INSERT INTO userinfo (username, password_hash, role, full_name, address, phone) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseAdapter.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword()); // Hashlenmiş gelmeli
            pstmt.setString(3, user.getRole());
            pstmt.setString(4, user.getFullName());
            pstmt.setString(5, user.getAddress());
            pstmt.setString(6, user.getPhone());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Kullanıcı ekleme hatası: " + e.getMessage());
            return false;
        }
    }

    // 4. SADECE PROFİL BİLGİLERİNİ GÜNCELLE (Şifre Hariç)
    // "Edit Profile" ekranında şifre değişmeyecekse bunu kullanın
    public boolean updateProfile(User user) {
        String sql = "UPDATE userinfo SET full_name = ?, address = ?, phone = ? WHERE id = ?";
        
        try (Connection conn = DatabaseAdapter.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, user.getFullName());
            pstmt.setString(2, user.getAddress());
            pstmt.setString(3, user.getPhone());
            pstmt.setInt(4, user.getId());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 5. SADECE ŞİFRE GÜNCELLE (Change Password)
    // Kullanıcı profilinde "Şifremi Değiştir" derse bunu kullanın
    public boolean updatePassword(int userId, String newPasswordHash) {
        String sql = "UPDATE userinfo SET password_hash = ? WHERE id = ?";
        
        try (Connection conn = DatabaseAdapter.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, newPasswordHash);
            pstmt.setInt(2, userId);
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 6. KULLANICI SİL (Fire Carrier)
    public boolean deleteUser(int userId) {
        String sql = "DELETE FROM userinfo WHERE id = ?";
        try (Connection conn = DatabaseAdapter.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Silme hatası: " + e.getMessage());
            return false;
        }
    }

    // 7. ROLE GÖRE LİSTELE (Owner -> Carrier Listesi)
    public List<User> getUsersByRole(String role) {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM userinfo WHERE role = ?";
        
        try (Connection conn = DatabaseAdapter.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, role);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToUser(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Yardımcı: ResultSet -> User
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        return new User(
            rs.getInt("id"),
            rs.getString("username"),
            rs.getString("password_hash"),
            rs.getString("role"),
            rs.getString("full_name"),
            rs.getString("address"),
            rs.getString("phone")
        );
    }
}
