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
        // Normalize name: lowercase, remove diacritics and common separators
        String lower = name.toLowerCase();
        lower = java.text.Normalizer.normalize(lower, java.text.Normalizer.Form.NFD)
            .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        String compact = lower.replaceAll("[^a-z0-9]", "");
        String spaced = lower.replaceAll("[^a-z0-9 ]", "").replaceAll("\\s+", " ");

        // Accept both singular and plural type values from DB ("fruit" / "fruits", "vegetable" / "vegetables")
        String t = type == null ? "" : type.trim().toLowerCase();
        String folder;
        if (t.startsWith("fruit")) {
            folder = "fruits";
        } else if (t.startsWith("veget")) {
            folder = "vegetables";
        } else {
            // default fallback
            folder = "vegetables";
        }
        String[] exts = {".jpg", ".jpeg", ".png"};

        // Generate candidate base names to try (original, compact, spaced, plural/singular variants)
        List<String> candidates = new ArrayList<>();
        candidates.add(lower);
        if (!compact.equals(lower)) candidates.add(compact);
        if (!spaced.equals(lower)) candidates.add(spaced.replace(" ", "_"));

        // Try adding or removing trailing 's' to handle plural/singular mismatches
        for (String base : new ArrayList<>(candidates)) {
            if (!base.endsWith("s")) candidates.add(base + "s");
            if (base.endsWith("s") && base.length() > 1) candidates.add(base.substring(0, base.length() - 1));
        }

        // Try each candidate with known extensions
        // resource name candidates computed
        for (String cand : candidates) {
            for (String ext : exts) {
                String path = "/images/" + folder + "/" + cand + ext;
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
        Connection conn = null;
        try {
            conn = DatabaseAdapter.getConnection();
            if (conn == null) return false;

            // Try hard delete first
            String deleteSql = "DELETE FROM productinfo WHERE id = ?";
            try (PreparedStatement del = conn.prepareStatement(deleteSql)) {
                del.setInt(1, productId);
                int affected = del.executeUpdate();
                if (affected > 0) return true;
            } catch (SQLException ignore) {
                // If hard delete fails (FK constraints, permissions), we'll try soft-delete below
            }

            // Fallback: soft-delete by marking inactive
            String softSql = "UPDATE productinfo SET is_active = 0 WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(softSql)) {
                pstmt.setInt(1, productId);
                return pstmt.executeUpdate() > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}