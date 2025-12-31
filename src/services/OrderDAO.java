package services;

import models.Order;
import models.OrderItem;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO {

    // 1. SİPARİŞ OLUŞTURMA
    public boolean createOrder(Order order, List<OrderItem> items) {
        // requested_delivery_time zorunlu, şimdilik 2 saat sonrasını veriyoruz.
        String insertOrderSQL = "INSERT INTO orderinfo (customer_id, total_cost, status, requested_delivery_time) " + 
                                "VALUES (?, ?, 'CREATED', DATE_ADD(NOW(), INTERVAL 2 HOUR))";
                                
        String insertItemSQL = "INSERT INTO orderiteminfo (order_id, product_id, amount_kg, unit_price, line_total) VALUES (?, ?, ?, ?, ?)";
        String updateStockSQL = "UPDATE productinfo SET stock_kg = stock_kg - ? WHERE id = ?";
        
        
        Connection conn = null;
        try {
            conn = DatabaseAdapter.getConnection();
            if (conn == null) return false;

            conn.setAutoCommit(false); // Transaction Başlat

            // --- A) SİPARİŞİ KAYDET ---
            PreparedStatement pstmtOrder = conn.prepareStatement(insertOrderSQL, Statement.RETURN_GENERATED_KEYS);
            pstmtOrder.setInt(1, order.getCustomerId());
            pstmtOrder.setDouble(2, order.getTotalCost()); 
            
            int affectedRows = pstmtOrder.executeUpdate();
            if (affectedRows == 0) throw new SQLException("Sipariş ana kaydı oluşturulamadı.");

            // ID'yi al
            ResultSet rs = pstmtOrder.getGeneratedKeys();
            int orderId = 0;
            if (rs.next()) {
                orderId = rs.getInt(1);
            } else {
                throw new SQLException("Sipariş ID alınamadı.");
            }

            // --- B) DETAYLARI (ÜRÜNLERİ) KAYDET ---
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

            conn.commit(); // Onayla
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            return false;
        } finally {
            try { if (conn != null) { conn.setAutoCommit(true); conn.close(); } } catch (SQLException e) {}
        }
    }

    // 2. OWNER İÇİN: TÜM SİPARİŞLERİ ÇEK (İSİMLERLE BERABER)
    public List<Order> getAllOrders() {
        List<Order> orders = new ArrayList<>();
        // SİHİRLİ DOKUNUŞ: LEFT JOIN ile kullanıcı adını (u.username) alıyoruz!
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

    // 3. CARRIER İÇİN: BEKLEYEN SİPARİŞLER
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

    // 4. CARRIER İÇİN: KENDİ SİPARİŞLERİ
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

    // 5. SİPARİŞ ATAMA (Carrier Assignment)
    public boolean assignOrderToCarrier(int orderId, int carrierId) {
        String sql = "UPDATE orderinfo SET carrier_id = ?, status = 'ASSIGNED' WHERE id = ?";
        try (Connection conn = DatabaseAdapter.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             
            pstmt.setInt(1, carrierId);
            pstmt.setInt(2, orderId);
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    // 6. DURUM GÜNCELLEME (Teslimat)
    public boolean updateOrderStatus(int orderId, String status) {
        String sql = "UPDATE orderinfo SET status = ? WHERE id = ?";
        try (Connection conn = DatabaseAdapter.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             
            pstmt.setString(1, status);
            pstmt.setInt(2, orderId);
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    // --- YARDIMCI METOT (FIX BURADA) ---
    private Order mapRowToOrder(ResultSet rs) throws SQLException {
        // Tarih kontrolü
        java.sql.Timestamp ts = rs.getTimestamp("requested_delivery_time");
        LocalDateTime deliveryTime = (ts != null) ? ts.toLocalDateTime() : null;
        
        // KULLANICI ADI ÇEKME (İşte burası düzeltiyor!)
        String customerName = rs.getString("username"); // JOIN'den gelen isim
        
        // Eğer kullanıcı silinmişse veya join hatası varsa ID göster
        if (customerName == null) {
            customerName = "Silinmiş Kullanıcı (ID: " + rs.getInt("customer_id") + ")";
        }
        
        // Adres bilgisi (User tablosundan geliyor)
        String address = rs.getString("address");
        if (address == null) address = "Adres Yok";

        // Görünen İsim Formatı: "TestCustomer (Kadıköy/İstanbul)"
        String displayName = customerName + " (" + address + ")";

        return new Order(
            rs.getInt("id"),
            rs.getInt("customer_id"),
            displayName, // <-- BURAYA DİKKAT: Artık isim gidiyor!
            rs.getInt("carrier_id"),
            rs.getString("status"),
            deliveryTime,
            rs.getDouble("total_cost")
        );
    }

    // 7. CUSTOMER İÇİN: KENDİ GEÇMİŞ SİPARİŞLERİ
    public List<Order> getOrdersByCustomerId(int customerId) {
        List<Order> orders = new ArrayList<>();
        // Burada carrier bilgisini de çekelim ki "Kurye: Ahmet" diye görebilsin
        String sql = "SELECT o.*, u.username, u.address FROM orderinfo o " +
                     "LEFT JOIN userinfo u ON o.customer_id = u.id " + // Bu satır customer adını getirir (zaten biliyoruz ama format bozulmasın)
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
}