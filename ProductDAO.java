import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {

    // Tüm ürünleri getir (Müşteri ekranı için)
    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        // is_active=1 olanları çekiyoruz (Silinmiş ürünleri göstermemek için)
        String sql = "SELECT * FROM productinfo WHERE is_active = 1 ORDER BY name ASC"; // [cite: 26] isme göre sıralı

        try (Connection conn = DatabaseAdapter.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                products.add(new Product(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("type"),
                    rs.getDouble("price"),
                    rs.getDouble("stock_kg"),
                    rs.getDouble("threshold_kg"),
                    rs.getBytes("image_blob") // BLOB verisini byte array olarak al
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }

    // Ürün stoğunu güncelle (Sipariş sonrası)
    public boolean updateStock(int productId, double newStock) {
        String sql = "UPDATE productinfo SET stock_kg = ? WHERE id = ?";
        try (Connection conn = DatabaseAdapter.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setDouble(1, newStock);
            pstmt.setInt(2, productId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 1. Yeni Ürün Ekleme (Owner için)
    public boolean addProduct(Product product) {
        String sql = "INSERT INTO productinfo (name, type, price, stock_kg, threshold_kg, image_blob, is_active) VALUES (?, ?, ?, ?, ?, ?, 1)";
        try (Connection conn = DatabaseAdapter.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, product.getName());
            pstmt.setString(2, product.getType());
            pstmt.setDouble(3, product.getPrice());
            pstmt.setDouble(4, product.getStock());
            pstmt.setDouble(5, product.getThreshold());
            pstmt.setBytes(6, product.getImage()); // Resim ekleme
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    //Update product
    public boolean updateProduct(Product product) {
        String sql = "UPDATE productinfo SET name=?, type=?, price=?, stock_kg=?, threshold_kg=?, image_blob=? WHERE id=?";
        try (Connection conn = DatabaseAdapter.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, product.getName());
            pstmt.setString(2, product.getType());
            pstmt.setDouble(3, product.getPrice());
            pstmt.setDouble(4, product.getStock());
            pstmt.setDouble(5, product.getThreshold());
            pstmt.setBytes(6, product.getImage());
            pstmt.setInt(7, product.getId());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    //Delete product
    public boolean deleteProduct(int productId) {
        // Gerçekten silmiyoruz, is_active=0 yapıyoruz ki geçmiş siparişler bozulmasın.
        String sql = "UPDATE productinfo SET is_active = 0 WHERE id = ?";
        try (Connection conn = DatabaseAdapter.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, productId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}