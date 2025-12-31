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
 * Handles database operations for product management including images.
 * * @author Group04
 */
public class ProductDAO {

    /**
     * Retrieves all active products from the database.
     * Sorted by name alphabetically.
     * * @return List of Product objects.
     */
    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        // Fetch only active products (is_active = 1) [cite: 26]
        String sql = "SELECT * FROM productinfo WHERE is_active = 1 ORDER BY name ASC";

        try (Connection conn = DatabaseAdapter.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                // Determine product type (enum) safely
                String typeStr = rs.getString("type");
                
                products.add(new Product(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getDouble("price"),
                    rs.getDouble("stock_kg"),
                    typeStr,
                    rs.getDouble("threshold_kg"),
                    rs.getBytes("image_blob") // Get BLOB as byte array
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }

    /**
     * Updates the stock of a specific product.
     * Used after an order is placed.
     * * @param productId ID of the product.
     * @param newStock New stock amount.
     * @return true if successful.
     */
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

    /**
     * Adds a new product to the database using a File object for the image.
     * This method handles the FileInputStream creation for BLOB storage.
     * * @param product The product object with text data.
     * @param imageFile The image file selected from the computer (can be null).
     * @return true if successful.
     */
    public boolean addProduct(Product product, File imageFile) {
        String sql = "INSERT INTO productinfo (name, type, price, stock_kg, threshold_kg, image_blob, is_active) VALUES (?, ?, ?, ?, ?, ?, 1)";
        
        try (Connection conn = DatabaseAdapter.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, product.getName());
            pstmt.setString(2, product.getType()); // "vegetable" or "fruit"
            pstmt.setDouble(3, product.getPrice());
            pstmt.setDouble(4, product.getStock());
            pstmt.setDouble(5, product.getThreshold());
            
            // --- IMAGE HANDLING ---
            if (imageFile != null && imageFile.exists()) {
                FileInputStream fis = new FileInputStream(imageFile);
                // Set binary stream for BLOB column
                pstmt.setBinaryStream(6, fis, (int) imageFile.length());
            } else {
                // If no image provided, set NULL
                pstmt.setNull(6, java.sql.Types.BLOB);
            }
            
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException | FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Updates an existing product using a File object for the image.
     * Logic: If a new image file is provided, update the BLOB. If null, keep the old image.
     * * @param product The product object with updated text data.
     * @param imageFile The new image file (pass null to keep existing image).
     * @return true if successful.
     */
    public boolean updateProduct(Product product, File imageFile) {
        String sql;
        // Dynamic SQL: Update image column only if a new file is provided
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
                // Case 1: Updating Image
                FileInputStream fis = new FileInputStream(imageFile);
                pstmt.setBinaryStream(6, fis, (int) imageFile.length());
                pstmt.setInt(7, product.getId());
            } else {
                // Case 2: Keeping Old Image
                pstmt.setInt(6, product.getId());
            }

            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException | FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Soft deletes a product (sets is_active to 0).
     * Preferable to DELETE to maintain order history integrity.
     * * @param productId ID of the product to remove.
     * @return true if successful.
     */
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