package services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import models.User;

public class UserDAO {

    public User login(String username, String password) {
        // 1. Şifreyi Hashle (Static metoddan çağırıyoruz)
        String hashedPassword = AuthenticationService.hashPassword(password);

        System.out.println("================ DEBUG ================");
        System.out.println("Aranan Kullanıcı: " + username);
        System.out.println("Aranan Hash: " + hashedPassword);
        
        // SQL Sorgusu: Hem kullanıcı adı hem şifre hash'i tutmalı
        String sql = "SELECT * FROM userinfo WHERE username = ? AND password_hash = ?";
        
        try (Connection conn = DatabaseAdapter.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             
            pstmt.setString(1, username);
            pstmt.setString(2, hashedPassword);
            
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                System.out.println("✅ Veritabanında EŞLEŞME BULUNDU! Nesne oluşturuluyor...");
                
                // --- İŞTE EKSİK OLAN KISIM BURASI OLABİLİR ---
                // Veritabanındaki sütunları tek tek çekip User nesnesine koyuyoruz.
                int id = rs.getInt("id");
                String dbUser = rs.getString("username");
                String role = rs.getString("role");
                String fullName = rs.getString("full_name"); // Sütun adı 'full_name' mi kontrol et
                String address = rs.getString("address");
                String phone = rs.getString("phone");

                // User nesnesini oluştur (Constructor sırası User.java ile aynı olmalı!)
                User user = new User(id, dbUser, role, fullName, address, phone);
                
                System.out.println("📦 User nesresi paketlendi ve gönderiliyor: " + role);
                return user; // <--- KİLİT NOKTA: BURADA user DÖNMELİ!
                
            } else {
                System.out.println("❌ Eşleşme YOK. Kullanıcı adı veya şifre yanlış.");
                return null;
            }

        } catch (Exception e) {
            System.err.println("💥 Veritabanı Hatası: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}