package services;

import models.Order;
import models.OrderItem;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import services.PDFInvoiceGenerator;
/**
 * Data Access Object (DAO) for Order operations.
 * NEW: Handles order creation, retrieval, assignment, and status updates.
 * @author Group04
 * @version 1.0
 */

public class OrderDAO {

    // 1. CREATE ORDER
    public boolean createOrder(Order order, List<OrderItem> items) {
        // requested_delivery_time is mandatory, defaulting to 2 hours later.
        String insertOrderSQL = "INSERT INTO orderinfo (customer_id, total_cost, status, requested_delivery_time) " + 
                    "VALUES (?, ?, 'CREATED', ?)";
                                
        String insertItemSQL = "INSERT INTO orderiteminfo (order_id, product_id, amount_kg, unit_price, line_total) VALUES (?, ?, ?, ?, ?)";
        String updateStockSQL = "UPDATE productinfo SET stock_kg = stock_kg - ? WHERE id = ?";
        
        // SQL to update the invoice as Base64 CLOB (invoice_log)
        String updateLogSQL = "UPDATE orderinfo SET invoice_log = ? WHERE id = ?";
        
        Connection conn = null;
        try {
            conn = DatabaseAdapter.getConnection();
            if (conn == null) return false;

            conn.setAutoCommit(false); // Start Transaction

            // --- A) SAVE ORDER (MAIN) ---
            PreparedStatement pstmtOrder = conn.prepareStatement(insertOrderSQL, Statement.RETURN_GENERATED_KEYS);
            pstmtOrder.setInt(1, order.getCustomerId());
            pstmtOrder.setDouble(2, order.getTotalCost()); 
            // Use the requested delivery time provided by the application (assume Istanbul zone applied earlier)
            java.time.LocalDateTime reqDt = null;
            try { reqDt = order.getDeliveryTime(); } catch (Exception ex) { reqDt = null; }
            if (reqDt == null) {
                reqDt = java.time.LocalDateTime.now(java.time.ZoneId.of("Europe/Istanbul")).plusHours(2);
            }
            // Use JDBC 4.2 setObject with LocalDateTime to avoid driver timezone conversions for DATETIME
            // Use JDBC 4.2 setObject with LocalDateTime to store DATETIME without timezone conversion
            pstmtOrder.setObject(3, reqDt);
            
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
                    // Save Base64-encoded PDF into invoice_log (CLOB) only (requirement)
                    try (PreparedStatement pstmtLog = conn.prepareStatement(updateLogSQL)) {
                        String base64 = java.util.Base64.getEncoder().encodeToString(pdfData);
                        pstmtLog.setString(1, base64);
                        pstmtLog.setInt(2, orderId);
                        pstmtLog.executeUpdate();
                    } catch (Exception e) {
                        System.err.println("Warning: Failed to save invoice_log (CLOB): " + e.getMessage());
                    }
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

    /**
     * Cancel an order and restore product stock.
     * Only orders with status 'CREATED' can be cancelled.
     * 
     * @param orderId The ID of the order to cancel
     * @return true if cancellation successful, false otherwise
     */
    public boolean cancelOrder(int orderId) {
        String updateOrder = "UPDATE orderinfo SET status = 'CANCELLED' WHERE id = ? AND status = 'CREATED'";
        String getItems = "SELECT product_id, amount_kg FROM orderiteminfo WHERE order_id = ?";
        String updateStock = "UPDATE productinfo SET stock_kg = stock_kg + ? WHERE id = ?";
        
        Connection conn = null;
        try {
            conn = DatabaseAdapter.getConnection();
            if (conn == null) return false;
            
            conn.setAutoCommit(false); // Start transaction
            
            // 1. Check if order exists and can be cancelled
            PreparedStatement pstmtCheck = conn.prepareStatement(
                "SELECT COUNT(*) FROM orderinfo WHERE id = ? AND status = 'CREATED'"
            );
            pstmtCheck.setInt(1, orderId);
            ResultSet rsCheck = pstmtCheck.executeQuery();
            
            if (!rsCheck.next() || rsCheck.getInt(1) == 0) {
                conn.rollback();
                System.err.println("Cannot cancel Order #" + orderId + " - Not in CREATED status or doesn't exist");
                return false;
            }
            
            // 2. Get order items to restore stock
            PreparedStatement pstmtItems = conn.prepareStatement(getItems);
            pstmtItems.setInt(1, orderId);
            ResultSet rsItems = pstmtItems.executeQuery();
            
            // 3. Restore stock for each product
            PreparedStatement pstmtStock = conn.prepareStatement(updateStock);
            while (rsItems.next()) {
                double amountKg = rsItems.getDouble("amount_kg");
                int productId = rsItems.getInt("product_id");
                
                pstmtStock.setDouble(1, amountKg);
                pstmtStock.setInt(2, productId);
                pstmtStock.addBatch();
            }
            pstmtStock.executeBatch();
            
            // 4. Update order status to CANCELLED
            PreparedStatement pstmtOrder = conn.prepareStatement(updateOrder);
            pstmtOrder.setInt(1, orderId);
            int affected = pstmtOrder.executeUpdate();
            
            conn.commit(); // Commit transaction
            
            // Order cancelled and stock restored
            return affected > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            return false;
        } finally {
            try { if (conn != null) { conn.setAutoCommit(true); conn.close(); } } catch (SQLException e) {}
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
        // Only assign if order is still unassigned and in CREATED status to avoid race conditions
        String sql = "UPDATE orderinfo SET carrier_id = ?, status = 'ASSIGNED' WHERE id = ? AND (carrier_id IS NULL OR carrier_id = 0) AND status = 'CREATED'";
        try (Connection conn = DatabaseAdapter.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, carrierId);
            pstmt.setInt(2, orderId);
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    // 7. UPDATE ORDER STATUS (DELIVERY)
    public boolean updateOrderStatus(int orderId, String status) {
        return updateOrderStatus(orderId, status, null);
    }

    /**
     * Update order status and optionally set the delivered_time column when marking DELIVERED.
     * @param orderId order id
     * @param status new status
     * @param deliveredTime delivered LocalDateTime (applied when status == "DELIVERED")
     * @return true if update succeeded
     */
    public boolean updateOrderStatus(int orderId, String status, java.time.LocalDateTime deliveredTime) {
        String sqlWithDelivered = "UPDATE orderinfo SET status = ?, delivered_time = ? WHERE id = ?";
        String sqlSimple = "UPDATE orderinfo SET status = ? WHERE id = ?";

        try (Connection conn = DatabaseAdapter.getConnection()) {
            if (conn == null) return false;

            if ("DELIVERED".equalsIgnoreCase(status) && deliveredTime != null) {
                try (PreparedStatement pstmt = conn.prepareStatement(sqlWithDelivered)) {
                    pstmt.setString(1, status);
                    pstmt.setObject(2, deliveredTime);
                    pstmt.setInt(3, orderId);
                    return pstmt.executeUpdate() > 0;
                }
            } else {
                try (PreparedStatement pstmt = conn.prepareStatement(sqlSimple)) {
                    pstmt.setString(1, status);
                    pstmt.setInt(2, orderId);
                    return pstmt.executeUpdate() > 0;
                }
            }
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
        // Read DATETIME as LocalDateTime to avoid timezone conversions by the driver
        LocalDateTime requested = null;
        LocalDateTime delivered = null;
        try {
            requested = rs.getObject("requested_delivery_time", LocalDateTime.class);
        } catch (Exception ex) {
            java.sql.Timestamp ts = rs.getTimestamp("requested_delivery_time");
            requested = (ts != null) ? ts.toLocalDateTime() : null;
        }
        try {
            delivered = rs.getObject("delivered_time", LocalDateTime.class);
        } catch (Exception ex) {
            java.sql.Timestamp ts2 = rs.getTimestamp("delivered_time");
            delivered = (ts2 != null) ? ts2.toLocalDateTime() : null;
        }

        // Prefer actual delivered time for display when order is DELIVERED
        LocalDateTime deliveryTime = delivered;
        String status = rs.getString("status");
        if (!"DELIVERED".equalsIgnoreCase(status) || deliveryTime == null) {
            deliveryTime = requested;
        }
        
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

    /**
     * Returns revenue aggregated by product name (top N products by revenue).
     * Key: product name, Value: total revenue (sum of line_total)
     */
    public java.util.LinkedHashMap<String, Double> getRevenueByProductTopN(int limit) {
        java.util.LinkedHashMap<String, Double> map = new java.util.LinkedHashMap<>();
        String sql = "SELECT p.name AS product_name, SUM(oi.line_total) AS revenue " +
                     "FROM orderiteminfo oi " +
                     "JOIN productinfo p ON oi.product_id = p.id " +
                     "GROUP BY p.id, p.name " +
                     "ORDER BY revenue DESC " +
                     "LIMIT ?";
        try (java.sql.Connection conn = DatabaseAdapter.getConnection();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            try (java.sql.ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getString("product_name"), rs.getDouble("revenue"));
                }
            }
        } catch (java.sql.SQLException e) { e.printStackTrace(); }
        return map;
    }

    /**
     * Returns daily revenue for the last `days` days (including today).
     * Key: date string (yyyy-MM-dd), Value: total revenue for that day.
     */
    public java.util.LinkedHashMap<String, Double> getDailyRevenueLastNDays(int days) {
        java.util.LinkedHashMap<String, Double> map = new java.util.LinkedHashMap<>();
        String sql = "SELECT DATE(o.requested_delivery_time) AS d, SUM(o.total_cost) AS revenue " +
                     "FROM orderinfo o " +
                     "WHERE o.requested_delivery_time >= DATE_SUB(CURDATE(), INTERVAL ? DAY) " +
                     "GROUP BY d ORDER BY d";
        try (java.sql.Connection conn = DatabaseAdapter.getConnection();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, days);
            try (java.sql.ResultSet rs = pstmt.executeQuery()) {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
                while (rs.next()) {
                    java.sql.Date d = rs.getDate("d");
                    String dateStr = (d != null) ? sdf.format(d) : "Unknown";
                    map.put(dateStr, rs.getDouble("revenue"));
                }
            }
        } catch (java.sql.SQLException e) { e.printStackTrace(); }
        return map;
    }

    /**
     * Returns top N products by total quantity sold (amount_kg) as LinkedHashMap(productName -> totalKg)
     */
    public java.util.LinkedHashMap<String, Double> getTopProductsByQuantityTopN(int limit) {
        java.util.LinkedHashMap<String, Double> map = new java.util.LinkedHashMap<>();
        String sql = "SELECT p.name AS product_name, SUM(oi.amount_kg) AS qty " +
                     "FROM orderiteminfo oi " +
                     "JOIN productinfo p ON oi.product_id = p.id " +
                     "GROUP BY p.id, p.name " +
                     "ORDER BY qty DESC LIMIT ?";
        try (java.sql.Connection conn = DatabaseAdapter.getConnection();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            try (java.sql.ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getString("product_name"), rs.getDouble("qty"));
                }
            }
        } catch (java.sql.SQLException e) { e.printStackTrace(); }
        return map;
    }

}