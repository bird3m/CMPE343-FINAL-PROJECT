package services;

import models.Order;
import models.OrderItem;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO {

    // 1. SİPARİŞ OLUŞTURMA (VERİTABANI ŞEMASIYLA %100 UYUMLU)
    public boolean createOrder(Order order, List<OrderItem> items) {
        // 1. ADIM: Sipariş Ana Kaydı (orderinfo)
        // requested_delivery_time zorunlu olduğu için 2 saat sonrasını ekliyoruz.
        String insertOrderSQL = "INSERT INTO orderinfo (customer_id, total_cost, status, requested_delivery_time) " + 
                                "VALUES (?, ?, 'CREATED', DATE_ADD(NOW(), INTERVAL 2 HOUR))";
                                
        // 2. ADIM: Sipariş Detayları (orderiteminfo)
        // DİKKAT: Sütun isimleri veritabanına göre düzeltildi: amount_kg, unit_price, line_total
        String insertItemSQL = "INSERT INTO orderiteminfo (order_id, product_id, amount_kg, unit_price, line_total) VALUES (?, ?, ?, ?, ?)";
        
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
                // Java'daki veriyi DB sütunlarına eşleştiriyoruz:
                pstmtItem.setInt(1, orderId);                       // order_id
                pstmtItem.setInt(2, item.getProductId());           // product_id
                pstmtItem.setDouble(3, item.getQuantity());         // amount_kg
                pstmtItem.setDouble(4, item.getPricePerUnit());     // unit_price
                
                // line_total hesaplama (Miktar * Birim Fiyat)
                double lineTotal = item.getQuantity() * item.getPricePerUnit();
                pstmtItem.setDouble(5, lineTotal);                  // line_total
                
                pstmtItem.addBatch(); 
            }
            pstmtItem.executeBatch(); 

            conn.commit(); // Her şey yolunda, onayla!
            System.out.println("✅ Sipariş ve detayları başarıyla kaydedildi! ID: " + orderId);
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            return false;
        } finally {
            try { if (conn != null) conn.setAutoCommit(true); conn.close(); } catch (SQLException e) {}
        }
    }

    // 2. OWNER İÇİN: TÜM SİPARİŞLERİ ÇEK
    public List<Order> getAllOrders() {
        List<Order> orders = new ArrayList<>();
        // Müşteri adını da alabilmek için userinfo ile birleştiriyoruz
        String sql = "SELECT o.*, u.username, u.address FROM orderinfo o " +
                     "LEFT JOIN userinfo u ON o.customer_id = u.id " +
                     "ORDER BY o.order_time DESC";

        try (Connection conn = DatabaseAdapter.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                orders.add(mapRowToOrder(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return orders;
    }

 
  // 3. CARRIER İÇİN: HAVUZDAKİ SİPARİŞLER
    public List<Order> getPendingOrders() {
        List<Order> orders = new ArrayList<>();
        // DİKKAT: Veritabanına 'CREATED' yazdık, o yüzden burada da 'CREATED' aramalıyız!
        // Eğer burada 'PENDING' yazıyorsa düzelt.
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

    // 4. CARRIER İÇİN: KENDİ SİPARİŞLERİ (ASSIGNED veya DELIVERED)
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

    // 5. CARRIER İÇİN: SİPARİŞİ ÜZERİNE AL (CREATED -> ASSIGNED)
    public boolean assignOrderToCarrier(int orderId, int carrierId) {
        String sql = "UPDATE orderinfo SET carrier_id = ?, status = 'ASSIGNED' WHERE id = ?";
        try (Connection conn = DatabaseAdapter.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             
            pstmt.setInt(1, carrierId);
            pstmt.setInt(2, orderId);
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    // 6. DURUM GÜNCELLEME (ASSIGNED -> DELIVERED)
    public boolean updateOrderStatus(int orderId, String status) {
        String sql = "UPDATE orderinfo SET status = ? WHERE id = ?";
        try (Connection conn = DatabaseAdapter.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             
            pstmt.setString(1, status);
            pstmt.setInt(2, orderId);
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    // --- YARDIMCI METOT (Veritabanı satırını Java Nesnesine Çevirir) ---
    private Order mapRowToOrder(ResultSet rs) throws SQLException {
        // Tarih dönüşümü
        java.sql.Timestamp ts = rs.getTimestamp("order_time");
        LocalDateTime orderTime = (ts != null) ? ts.toLocalDateTime() : null;
        
        // İsim ve Adres (LEFT JOIN sayesinde geliyor)
        String customerName = rs.getString("username");
        String address = rs.getString("address"); // Adres user tablosundan geliyor
        if (address == null) address = "Adres Bulunamadı";

        // Order nesnesi oluşturuyoruz
        // DİKKAT: Order modelinle bu constructor sırasının tuttuğuna emin ol!
        return new Order(
            rs.getInt("id"),
            rs.getInt("customer_id"),
            customerName + " (" + address + ")", // İsmin yanına adresi ekledik ki listede görünsün
            rs.getInt("carrier_id"),
            rs.getString("status"),
            orderTime,
            rs.getDouble("total_cost") // DB'deki isim total_cost
        );
    }
}