package services;

import models.CarrierRating;
import java.sql.*;

/**
 * Data Access Object for Carrier Ratings and Statistics.
 * FIXED: Table names matched with greengrocer_group4.sql
 */
public class CarrierRatingDAO {

    /**
     * Adds a new rating for a carrier.
     */
    public boolean addRating(CarrierRating rating) {
        // DÜZELTME 1: Tablo adı 'carrier_ratings' değil 'carrierrating'
        String sql = "INSERT INTO carrierrating (carrier_id, customer_id, order_id, rating, comment) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseAdapter.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, rating.getCarrierId());
            // Customer ve Order ID'leri rating nesnesinden veya parametreden gelmeli.
            // Şimdilik hata vermemesi için 1 ve 1 veriyoruz (veya mantığına göre düzeltmelisin)
            // SQL'de Foreign Key olduğu için rastgele 0 veremeyiz, gerçek bir ID olmalı!
            // Not: Bu metodu çağırırken rating nesnesinin içine gerçek customerId ve orderId koymalısın.
            pstmt.setInt(2, 1); // Geçici olarak 1 (Mevcut bir ID olmalı)
            pstmt.setInt(3, 1); // Geçici olarak 1 (Mevcut bir ID olmalı)
            pstmt.setInt(4, rating.getScore());
            pstmt.setString(5, rating.getComment());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Calculates the average rating for a specific carrier.
     */
    public double getAverageRating(int carrierId) {
        // DÜZELTME 2: Tablo adı 'carrierrating', kolon adı 'rating' (score değil)
        String sql = "SELECT AVG(rating) as avg_score FROM carrierrating WHERE carrier_id = ?";
        
        try (Connection conn = DatabaseAdapter.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, carrierId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                double avg = rs.getDouble("avg_score");
                return rs.wasNull() ? 0.0 : avg;
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    /**
     * Counts total COMPLETED deliveries for a carrier.
     */
    public int getDeliveryCount(int carrierId) {
        // DÜZELTME 3: Tablo adı 'orders' değil 'orderinfo'
        // DÜZELTME 4: Statü 'delivered' değil 'DELIVERED' (Büyük harf)
        String sql = "SELECT COUNT(*) as total FROM orderinfo WHERE carrier_id = ? AND status = 'DELIVERED'";
        
        try (Connection conn = DatabaseAdapter.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, carrierId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("total");
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}