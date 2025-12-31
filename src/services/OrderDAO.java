package services;

import models.Order;
import models.OrderItem;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// Make sure PDFInvoiceGenerator is accessible. 
// If it's in the default package, moving it to 'services' or 'utils' package is recommended.
// import utils.PDFInvoiceGenerator; 

public class OrderDAO {

    // 1. CREATE ORDER
    public boolean createOrder(Order order, List<OrderItem> items) {
        // requested_delivery_time is mandatory, defaulting to 2 hours later.
        String insertOrderSQL = "INSERT INTO orderinfo (customer_id, total_cost, status, requested_delivery_time) " + 
                                "VALUES (?, ?, 'CREATED', DATE_ADD(NOW(), INTERVAL 2 HOUR))";
                                
        String insertItemSQL = "INSERT INTO orderiteminfo (order_id, product_id, amount_kg, unit_price, line_total) VALUES (?, ?, ?, ?, ?)";
        String updateStockSQL = "UPDATE productinfo SET stock_kg = stock_kg - ? WHERE id = ?";
        
        // SQL to update the invoice PDF blob
        String updatePdfSQL = "UPDATE orderinfo SET invoice_pdf = ? WHERE id = ?";
        
        Connection conn = null;
        try {
            conn = DatabaseAdapter.getConnection();
            if (conn == null) return false;

            conn.setAutoCommit(false); // Start Transaction

            // --- A) SAVE ORDER (MAIN) ---
            PreparedStatement pstmtOrder = conn.prepareStatement(insertOrderSQL, Statement.RETURN_GENERATED_KEYS);
            pstmtOrder.setInt(1, order.getCustomerId());
            pstmtOrder.setDouble(2, order.getTotalCost()); 
            
            int affectedRows = pstmtOrder.executeUpdate();
            if (affectedRows == 0) throw new SQLException("Creating order failed, no rows affected.");

            // Get Generated ID
            ResultSet rs = pstmtOrder.getGeneratedKeys();
            int orderId = 0;
            if (rs.next()) {
                orderId = rs.getInt(1);
                order.setId(orderId); // Set ID to order object for PDF generation
            } else {
                throw new SQLException("Creating order failed, no ID obtained.");
            }

            // --- B) SAVE ORDER ITEMS ---
            PreparedStatement pstmtItem = conn.prepareStatement(insertItemSQL);
            for (OrderItem item : items) {
                pstmtItem.setInt(1, orderId);
                pstmtItem.setInt(2, item.getProductId());
                pstmtItem.setDouble(3, item.getQuantity());
                pstmtItem.setDouble(4, item.getPricePerUnit());
                
                double lineTotal = item.getQuantity() * item.getPricePerUnit();
                pstmtItem.setDouble(5, lineTotal);
                
                pstmtItem.addBatch(); 
            }
            pstmtItem.executeBatch(); 

            // --- C) UPDATE STOCK ---
            PreparedStatement pstmtStock = conn.prepareStatement(updateStockSQL);
            for (OrderItem item : items) {
                pstmtStock.setDouble(1, item.getQuantity());
                pstmtStock.setInt(2, item.getProductId());
                pstmtStock.addBatch();
            }
            pstmtStock.executeBatch();

            try {
                // Ensure items are linked to the order object for the generator
                // (Assuming Order class has setItems or getItems().addAll logic)
                // order.setItems(items); 
                
                // Generate PDF bytes
                // NOTE: Ensure PDFInvoiceGenerator class is imported or available
                byte[] pdfData = PDFInvoiceGenerator.generateInvoicePDF(order); 
                
                if (pdfData != null) {
                    PreparedStatement pstmtPdf = conn.prepareStatement(updatePdfSQL);
                    pstmtPdf.setBytes(1, pdfData);
                    pstmtPdf.setInt(2, orderId);
                    pstmtPdf.executeUpdate();
                    pstmtPdf.close();
                }
            } catch (Exception e) {
                System.err.println("Warning: Failed to generate/save PDF Invoice: " + e.getMessage());
                // We don't rollback the whole order just because PDF failed, but we log it.
            }

            conn.commit(); // Commit Transaction
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            return false;
        } finally {
            try { if (conn != null) { conn.setAutoCommit(true); conn.close(); } } catch (SQLException e) {}
        }
    }

    // 2. CANCEL ORDER (NEW METHOD)
    public boolean cancelOrder(int orderId) {
        // Only orders with 'CREATED' status can be cancelled.
        String sql = "UPDATE orderinfo SET status = 'CANCELLED' WHERE id = ? AND status = 'CREATED'";
        
        try (Connection conn = DatabaseAdapter.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, orderId);
            int affected = pstmt.executeUpdate();
            // If affected > 0, it means the order existed AND was in CREATED state.
            return affected > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 3. FOR OWNER: GET ALL ORDERS
    public List<Order> getAllOrders() {
        List<Order> orders = new ArrayList<>();
        // LEFT JOIN to get username
        String sql = "SELECT o.*, u.username, u.address FROM orderinfo o " +
                     "LEFT JOIN userinfo u ON o.customer_id = u.id " +
                     "ORDER BY o.requested_delivery_time DESC";

        try (Connection conn = DatabaseAdapter.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                orders.add(mapRowToOrder(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return orders;
    }

    // 4. FOR CARRIER: GET PENDING ORDERS
    public List<Order> getPendingOrders() {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT o.*, u.username, u.address FROM orderinfo o " +
                     "LEFT JOIN userinfo u ON o.customer_id = u.id " +
                     "WHERE o.status = 'CREATED' AND (o.carrier_id IS NULL OR o.carrier_id = 0)";
        
        try (Connection conn = DatabaseAdapter.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                orders.add(mapRowToOrder(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return orders;
    }

    // 5. FOR CARRIER: GET OWN ORDERS BY STATUS
    public List<Order> getOrdersByCarrierAndStatus(int carrierId, String status) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT o.*, u.username, u.address FROM orderinfo o " +
                     "LEFT JOIN userinfo u ON o.customer_id = u.id " +
                     "WHERE o.carrier_id = ? AND o.status = ?";
        
        try (Connection conn = DatabaseAdapter.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             
            pstmt.setInt(1, carrierId);
            pstmt.setString(2, status);
            ResultSet rs = pstmt.executeQuery();
            while(rs.next()) {
                orders.add(mapRowToOrder(rs));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return orders;
    }

    // 6. ASSIGN ORDER TO CARRIER
    public boolean assignOrderToCarrier(int orderId, int carrierId) {
        String sql = "UPDATE orderinfo SET carrier_id = ?, status = 'ASSIGNED' WHERE id = ?";
        try (Connection conn = DatabaseAdapter.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             
            pstmt.setInt(1, carrierId);
            pstmt.setInt(2, orderId);
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    // 7. UPDATE ORDER STATUS (DELIVERY)
    public boolean updateOrderStatus(int orderId, String status) {
        String sql = "UPDATE orderinfo SET status = ? WHERE id = ?";
        try (Connection conn = DatabaseAdapter.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             
            pstmt.setString(1, status);
            pstmt.setInt(2, orderId);
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    // 8. FOR CUSTOMER: GET OWN ORDER HISTORY
    public List<Order> getOrdersByCustomerId(int customerId) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT o.*, u.username, u.address FROM orderinfo o " +
                     "LEFT JOIN userinfo u ON o.customer_id = u.id " +
                     "WHERE o.customer_id = ? " +
                     "ORDER BY o.requested_delivery_time DESC";
        
        try (Connection conn = DatabaseAdapter.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             
            pstmt.setInt(1, customerId);
            ResultSet rs = pstmt.executeQuery();
            while(rs.next()) {
                orders.add(mapRowToOrder(rs));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return orders;
    }

    // --- HELPER METHOD ---
    private Order mapRowToOrder(ResultSet rs) throws SQLException {
        java.sql.Timestamp ts = rs.getTimestamp("requested_delivery_time");
        LocalDateTime deliveryTime = (ts != null) ? ts.toLocalDateTime() : null;
        
        String customerName = rs.getString("username"); 
        if (customerName == null) {
            customerName = "Deleted User (ID: " + rs.getInt("customer_id") + ")";
        }
        
        String address = rs.getString("address");
        if (address == null) address = "No Address";

        String displayName = customerName + " (" + address + ")";

        return new Order(
            rs.getInt("id"),
            rs.getInt("customer_id"),
            displayName, 
            rs.getInt("carrier_id"),
            rs.getString("status"),
            deliveryTime,
            rs.getDouble("total_cost")
        );
    }

}