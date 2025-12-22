package services;

import models.Product;
import java.util.ArrayList;
import java.util.List;

/**
 * Product Service - Provides test data
 * TODO: Replace with database access (DAO) later
 */
public class ProductService {
    
    /**
     * Get all vegetables (12 items minimum)
     */
    public static List<Product> getVegetables() {
        List<Product> vegetables = new ArrayList<>();
        
        vegetables.add(new Product(1, "Tomato", "vegetable", 20.50, 50, 5, "/images/vegetables/tomato.jpg"));
        vegetables.add(new Product(2, "Potato", "vegetable", 15.80, 100, 10, "/images/vegetables/potato.jpg"));
        vegetables.add(new Product(3, "Onion", "vegetable", 14.50, 80, 8, "/images/vegetables/onion.jpg"));
        vegetables.add(new Product(4, "Carrot", "vegetable", 20.00, 60, 6, "/images/vegetables/carrot.jpg"));
        vegetables.add(new Product(5, "Cucumber", "vegetable", 25.20, 40, 5, "/images/vegetables/cucumber.jpg"));
        vegetables.add(new Product(6, "Pepper", "vegetable", 30.00, 35, 5, "/images/vegetables/pepper.jpg"));
        vegetables.add(new Product(7, "Eggplant", "vegetable", 29.80, 45, 5, "/images/vegetables/eggplant.jpg"));
        vegetables.add(new Product(8, "Garlic", "vegetable", 15.00, 25, 3, "/images/vegetables/garlic.jpg"));
        vegetables.add(new Product(9, "Lettuce", "vegetable", 10.90, 55, 6, "/images/vegetables/lettuce.jpg"));
        vegetables.add(new Product(10, "Broccoli", "vegetable", 30.20, 30, 4, "/images/vegetables/broccoli.jpg"));
        vegetables.add(new Product(11, "Corn", "vegetable", 20.30, 65, 7, "/images/vegetables/corn.jpg"));
        vegetables.add(new Product(12, "Zucchini", "vegetable", 20.40, 38, 5, "/images/vegetables/zucchini.jpg"));
        
        return vegetables;
    }
    
    /**
     * Get all fruits (12 items minimum)
     */
    public static List<Product> getFruits() {
        List<Product> fruits = new ArrayList<>();
        
        fruits.add(new Product(13, "Apple", "fruit", 24.50, 70, 8, "/images/fruits/apple.jpg"));
        fruits.add(new Product(14, "Banana", "fruit", 80.80, 90, 10, "/images/fruits/banana.jpg"));
        fruits.add(new Product(15, "Orange", "fruit", 30.00, 65, 7, "/images/fruits/orange.jpg"));
        fruits.add(new Product(16, "Kiwi", "fruit", 40.20, 32, 4, "/images/fruits/kiwi.jpg"));
        fruits.add(new Product(17, "Coconut", "fruit", 50.00, 20, 3, "/images/fruits/coconut.jpg"));
        fruits.add(new Product(18, "Lemon", "fruit", 70.50, 55, 6, "/images/fruits/lemon.jpg"));
        fruits.add(new Product(19, "Pineapple", "fruit", 80.60, 28, 4, "/images/fruits/pineapple.jpg"));
        fruits.add(new Product(20, "Pomegranate", "fruit", 40.50, 35, 5, "/images/fruits/pomegranate.jpg"));
        fruits.add(new Product(21, "Tangerine", "fruit", 29.90, 48, 6, "/images/fruits/tangerine.jpg"));
        fruits.add(new Product(22, "Avocado", "fruit", 50.50, 25, 3, "/images/fruits/avocado.jpg"));
        fruits.add(new Product(23, "Chestnut", "fruit", 90.00, 18, 2, "/images/fruits/chesnut.jpg"));
        fruits.add(new Product(24, "Ginger", "fruit", 30.80, 40, 5, "/images/fruits/ginger.jpg"));
        
        return fruits;
    }
    
    /**
     * Get all products
     */
    public static List<Product> getAllProducts() {
        List<Product> allProducts = new ArrayList<>();
        allProducts.addAll(getVegetables());
        allProducts.addAll(getFruits());
        return allProducts;
    }
    
    /**
     * Search products by keyword
     */
    public static List<Product> searchProducts(String keyword, List<Product> products) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return products;
        }
        
        List<Product> results = new ArrayList<>();
        String lowerKeyword = keyword.toLowerCase();
        
        for (Product product : products) {
            if (product.getName().toLowerCase().contains(lowerKeyword)) {
                results.add(product);
            }
        }
        
        return results;
    }
}