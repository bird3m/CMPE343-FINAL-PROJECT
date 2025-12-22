package models;

/**
 * Product Model - Represents a grocery product
 */
public class Product {
    private int id;
    private String name;
    private String type; // "vegetable" or "fruit"
    private double price;
    private double stock; // in kg
    private double threshold;
    private String imagePath;
    
    // Constructors
    public Product() {}
    
    public Product(int id, String name, String type, double price, 
                   double stock, double threshold, String imagePath) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.price = price;
        this.stock = stock;
        this.threshold = threshold;
        this.imagePath = imagePath;
    }
    
    /**
     * Get current price (doubles if stock <= threshold)
     */
    public double getCurrentPrice() {
        if (stock <= threshold) {
            return price * 2;
        }
        return price;
    }
    
    /**
     * Check if product is in stock
     */
    public boolean isInStock() {
        return stock > 0;
    }
    
    /**
     * Format product for display in ListView
     */
    @Override
    public String toString() {
        return String.format("%s - $%.2f/kg - Stock: %.1fkg", 
                           name, getCurrentPrice(), stock);
    }
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    
    public double getStock() { return stock; }
    public void setStock(double stock) { this.stock = stock; }
    
    public double getThreshold() { return threshold; }
    public void setThreshold(double threshold) { this.threshold = threshold; }
    
    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
}