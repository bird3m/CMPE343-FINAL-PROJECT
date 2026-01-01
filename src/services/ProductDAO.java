package services;

import models.Product;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

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
                String prodName = rs.getString("name");
                
                // Prefer DB blob, but fallback to packaged resource images (resources/images)
                byte[] img = rs.getBytes("image_blob");
                if (img == null || img.length == 0) {
                    img = loadResourceImage(prodName, typeStr);
                }

                products.add(new Product(
                    rs.getInt("id"),
                    prodName,
                    typeStr,                    
                    rs.getDouble("price"),     
                    rs.getDouble("stock_kg"),
                    rs.getDouble("threshold_kg"),
                    img
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }

    // Attempt to load product image from packaged resources (resources/images/{fruits,vegetables})
    private byte[] loadResourceImage(String name, String type) {
        if (name == null || type == null) return null;
        String lower = name.toLowerCase();
        String folder = "fruits".equalsIgnoreCase(type) ? "fruits" : "vegetables";
        String[] exts = {".jpg", ".jpeg", ".png"};

        for (String ext : exts) {
            String path = "/images/" + folder + "/" + lower + ext;
            try (InputStream is = getClass().getResourceAsStream(path)) {
                if (is != null) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    byte[] buffer = new byte[4096];
                    int read;
                    while ((read = is.read(buffer)) != -1) {
                        baos.write(buffer, 0, read);
                    }
                    return baos.toByteArray();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
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
        String sql = "INSERT INTO productinfo (name, type, price, stock_kg, threshold_kg, image_blob, is_active) VALUES (?, ?, ?, ?, ?, ?, 1)";
        
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
            sql = "UPDATE productinfo SET name=?, type=?, price=?, stock_kg=?, threshold_kg=?, image_blob=? WHERE id=?";
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