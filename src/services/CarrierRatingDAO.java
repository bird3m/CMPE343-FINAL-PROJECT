package services;

import models.CarrierRating;
import java.sql.*;

/**
 * Data Access Object for Carrier Ratings and Statistics.
 * 
 * - Table name corrected to 'carrierrating'
 * - Column name corrected to 'rating' (not 'score')
 * - No more hardcoded IDs - uses CarrierRating model fields
 * 
 * @author Group04
 * @version 1.0
 */
public class CarrierRatingDAO {

    /**
     * Adds a new rating for a carrier.
     * 
     * No more hardcoded values!
     * 
     * @param rating CarrierRating object with all required fields
     * @return true if rating added successfully, false otherwise
     */
    public boolean addRating(CarrierRating rating) {
        String sql = "INSERT INTO carrierrating (carrier_id, customer_id, order_id, rating, comment) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseAdapter.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, rating.getCarrierId());
            pstmt.setInt(2, rating.getCustomerId());  
            pstmt.setInt(3, rating.getOrderId());     
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
     * 
     * @param carrierId The carrier's ID
     * @return Average rating (0.0 to 5.0), or 0.0 if no ratings exist
     */
    public double getAverageRating(int carrierId) {
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
     * Counts total DELIVERED orders for a carrier.
     * Only counts orders with status='DELIVERED'.
     * 
     * @param carrierId The carrier's ID
     * @return Number of completed deliveries
     */
    public int getDeliveryCount(int carrierId) {
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