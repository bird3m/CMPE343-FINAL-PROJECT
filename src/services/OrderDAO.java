package services;

import models.OrderItem;
import models.Order;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO {

    
    public boolean createOrder(Order order, List<OrderItem> items) {
        String insertOrderSQL = "INSERT INTO orders (customer_id, total_price, status, delivery_address) VALUES (?, ?, 'PENDING', ?)";
        String insertItemSQL = "INSERT INTO order_items (order_id, product_id, quantity, price_at_purchase) VALUES (?, ?, ?, ?)";
        
        Connection conn = null;
        try {
            conn = DatabaseAdapter.getConnection();
            conn.setAutoCommit(false); 

            //save the order
            PreparedStatement pstmtOrder = conn.prepareStatement(insertOrderSQL, Statement.RETURN_GENERATED_KEYS);
            pstmtOrder.setInt(1, order.getCustomerId());
            pstmtOrder.setDouble(2, order.getTotalPrice());
            pstmtOrder.setString(3, order.getDeliveryAddress());
            pstmtOrder.executeUpdate();

            //Take the id of the created order
            ResultSet rs = pstmtOrder.getGeneratedKeys();
            int orderId = 0;
            if (rs.next()) {
                orderId = rs.getInt(1);
            }

           
            PreparedStatement pstmtItem = conn.prepareStatement(insertItemSQL);
            for (OrderItem item : items) {
                pstmtItem.setInt(1, orderId);
                pstmtItem.setInt(2, item.getProductId());
                pstmtItem.setDouble(3, item.getQuantity());
                pstmtItem.setDouble(4, item.getPricePerUnit());
                pstmtItem.addBatch(); 
            }
            pstmtItem.executeBatch(); // Hepsini tek seferde yolla

            conn.commit(); 
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            try {
                if (conn != null) conn.rollback(); // HATA VARSA GERİ AL
            } catch (SQLException ex) { ex.printStackTrace(); }
            return false;
        } finally {
            try { if (conn != null) conn.setAutoCommit(true); conn.close(); } catch (SQLException e) {}
        }
    }

    public List<Order> getOrdersByCustomer(int customerId) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM orders WHERE customer_id = ? ORDER BY order_date DESC";
        
        try (Connection conn = DatabaseAdapter.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, customerId);
            ResultSet rs = pstmt.executeQuery();
            while(rs.next()) {
                orders.add(mapRowToOrder(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return orders;
    }

    //for carrier
    public List<Order> getPendingOrders() {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM orders WHERE status = 'PENDING'";
        
        try (Connection conn = DatabaseAdapter.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while(rs.next()) {
                orders.add(mapRowToOrder(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return orders;
    }

    public boolean updateStatus(int orderId, String newStatus, int carrierId) {
        String sql = "UPDATE orders SET status = ?, carrier_id = ? WHERE id = ?";
        // Eğer carrierId 0 ise (iptal veya boşta), NULL set etmek gerekebilir ama şimdilik int tutuyoruz.
        
        try (Connection conn = DatabaseAdapter.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, newStatus);
            if (carrierId > 0) pstmt.setInt(2, carrierId); else pstmt.setNull(2, Types.INTEGER);
            pstmt.setInt(3, orderId);
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // Helper
    private Order mapRowToOrder(ResultSet rs) throws SQLException {
        return new Order(
            rs.getInt("id"),
            rs.getInt("customer_id"),
            rs.getInt("carrier_id"),
            rs.getDouble("total_price"),
            rs.getString("status"),
            rs.getTimestamp("order_date"),
            rs.getString("delivery_address")
        );
    }
}