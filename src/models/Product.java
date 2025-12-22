package models;

/**
 * Product Model - Represents a grocery product
 * @author Group04
 */
public class Product {
    private int id;
    private String name;
    private String type; // "vegetable" or "fruit"
    private double price;
    private double stock; // in kg
    private double threshold;
    private String imagePath;
    private byte[] image; // BLOB için byte array
    
    // Constructor 1 - imagePath ile (Test için)
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
    
    // Constructor 2 - byte[] image ile (Database için)
    public Product(int id, String name, String type, double price, 
                   double stock, double threshold, byte[] image) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.price = price;
        this.stock = stock;
        this.threshold = threshold;
        this.image = image;
    }
    
    /**
     * Get current price (doubles if stock <= threshold)
     * PROJECT REQUIREMENT: Threshold feature
     */
    public double getCurrentPrice() {
        if (stock <= threshold) {
            return price * 2; // GREEDY OWNER!
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
        String priceIndicator = (stock <= threshold) ? " (2x) " : " ";
        return String.format("%s%s- %.2f₺/kg - Stock: %.1fkg", 
                           name, priceIndicator, getCurrentPrice(), stock);
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
    
    public byte[] getImage() { return image; }
    public void setImage(byte[] image) { this.image = image; }
}