package services;

import models.CarrierRating;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CarrierRatingDAO {

    // PUAN VER
    public boolean addRating(CarrierRating rating) {
        String sql = "INSERT INTO carrier_ratings (carrier_id, customer_id, order_id, score, comment) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseAdapter.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, rating.getCarrierId());
            pstmt.setInt(2, 0); // Customer ID elimizde yoksa 0 (veya nesneden alınmalı)
            pstmt.setInt(3, 0); // Order ID
            pstmt.setInt(4, rating.getScore());
            pstmt.setString(5, rating.getComment());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // KURYENİN ORTALAMA PUANINI GETİR
    public double getAverageRating(int carrierId) {
        String sql = "SELECT AVG(score) as avg_score FROM carrier_ratings WHERE carrier_id = ?";
        
        try (Connection conn = DatabaseAdapter.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, carrierId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("avg_score");
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return 0.0;
    }
}