package services;

import models.Product;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Product Service - Database Wrapper.
 * 
 * All products are fetched from database with images stored as BLOBs.
 * 
 * @author Group04
 * @version 1.0
 */
public class ProductService {
    
    private static ProductDAO productDAO = new ProductDAO();
    
    /**
     * Get all vegetables from database.
     * Filters products with type="vegetable" and sorts by name.
     * 
     * @return List of vegetable products
     */
    public static List<Product> getVegetables() {
        return productDAO.getAllProducts().stream()
            .filter(p -> "vegetable".equalsIgnoreCase(p.getType()))
            .sorted((p1, p2) -> p1.getName().compareToIgnoreCase(p2.getName()))
            .collect(Collectors.toList());
    }
    
    /**
     * Get all fruits from database.
     * Filters products with type="fruit" and sorts by name.
     * 
     * @return List of fruit products
     */
    public static List<Product> getFruits() {
        return productDAO.getAllProducts().stream()
            .filter(p -> "fruit".equalsIgnoreCase(p.getType()))
            .sorted((p1, p2) -> p1.getName().compareToIgnoreCase(p2.getName()))
            .collect(Collectors.toList());
    }
    
    /**
     * Get all products from database.
     * Returns all active products sorted by name.
     * 
     * @return List of all products
     */
    public static List<Product> getAllProducts() {
        return productDAO.getAllProducts();
    }
    
    /**
     * Search products by keyword.
     * Performs case-insensitive search in product names.
     * 
     * @param keyword Search term (can be partial name)
     * @param products List of products to search in
     * @return Filtered list matching the keyword
     */
    public static List<Product> searchProducts(String keyword, List<Product> products) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return products;
        }
        
        String lowerKeyword = keyword.toLowerCase().trim();
        
        return products.stream()
            .filter(p -> p.getName().toLowerCase().contains(lowerKeyword))
            .collect(Collectors.toList());
    }
}