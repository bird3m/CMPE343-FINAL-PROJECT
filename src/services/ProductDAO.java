package services;

import models.Product;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * Data Access Object for Products.
 * FIXED: Constructor arguments aligned with Product.java Model.
 * * @author Group04
 */
public class ProductDAO {

    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        // Fetch only active products
        String sql = "SELECT * FROM productinfo WHERE is_active = 1 ORDER BY name ASC";

        try (Connection conn = DatabaseAdapter.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                String typeStr = rs.getString("type");
                
                products.add(new Product(
                    rs.getInt("id"),
                    rs.getString("name"),
                    typeStr,                    
                    rs.getDouble("price"),     
                    rs.getDouble("stock_kg"),
                    rs.getDouble("threshold_kg"),
                    rs.getBytes("image")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }

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

    // ==========================================
    // OWNER METHODS (ADD / UPDATE / DELETE)
    // ==========================================

    public boolean addProduct(Product product, File imageFile) {
        String sql = "INSERT INTO productinfo (name, type, price, stock_kg, threshold_kg, image, is_active) VALUES (?, ?, ?, ?, ?, ?, 1)";
        
        try (Connection conn = DatabaseAdapter.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, product.getName());
            pstmt.setString(2, product.getType()); 
            pstmt.setDouble(3, product.getPrice());
            pstmt.setDouble(4, product.getStock());
            pstmt.setDouble(5, product.getThreshold());
            
            if (imageFile != null && imageFile.exists()) {
                FileInputStream fis = new FileInputStream(imageFile);
                pstmt.setBinaryStream(6, fis, (int) imageFile.length());
            } else {
                pstmt.setNull(6, java.sql.Types.BLOB);
            }
            
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException | FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateProduct(Product product, File imageFile) {
        String sql;
        if (imageFile != null) {
            sql = "UPDATE productinfo SET name=?, type=?, price=?, stock_kg=?, threshold_kg=?, image=? WHERE id=?";
        } else {
            sql = "UPDATE productinfo SET name=?, type=?, price=?, stock_kg=?, threshold_kg=? WHERE id=?";
        }

        try (Connection conn = DatabaseAdapter.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, product.getName());
            pstmt.setString(2, product.getType());
            pstmt.setDouble(3, product.getPrice());
            pstmt.setDouble(4, product.getStock());
            pstmt.setDouble(5, product.getThreshold());
            
            if (imageFile != null && imageFile.exists()) {
                FileInputStream fis = new FileInputStream(imageFile);
                pstmt.setBinaryStream(6, fis, (int) imageFile.length());
                pstmt.setInt(7, product.getId());
            } else {
                pstmt.setInt(6, product.getId());
            }

            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException | FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteProduct(int productId) {
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