/**
 * Product class for GreenGrocer Application.
 * Represents a product (vegetable or fruit) in the system.
 * 
 * @author GroupXX
 * @version 1.0
 */
public class Product {
    
    private int id;
    private String name;
    private String type; // "vegetable" or "fruit"
    private double price; // price per kg
    private double stock; // available stock in kg
    private double threshold; // when to double the price
    private byte[] imageData; // stored as BLOB in database
    private String imageLocation; // path/url for local images

    /**
     * Full constructor for Product.
     * 
     * @param id Product ID
     * @param name Product name
     * @param type Product type ("vegetable" or "fruit")
     * @param price Price per kilogram
     * @param stock Available stock in kilograms
     * @param threshold Threshold for price doubling
     * @param imageLocation Image file path
     */
    public Product(int id, String name, String type, double price, 
                   double stock, double threshold, String imageLocation) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.price = price;
        this.stock = stock;
        this.threshold = threshold;
        this.imageLocation = imageLocation;
    }

    /**
     * Constructor without ID (for new products before DB insertion).
     */
    public Product(String name, String type, double price, 
                   double stock, double threshold, String imageLocation) {
        this.name = name;
        this.type = type;
        this.price = price;
        this.stock = stock;
        this.threshold = threshold;
        this.imageLocation = imageLocation;
    }

    /**
     * Gets the current effective price (considering threshold).
     * If stock <= threshold, price is doubled.
     * 
     * @return The effective price per kg
     */
    public double getEffectivePrice() {
        if (stock <= threshold) {
            return price * 2; // Greedy owner! 😈
        }
        return price;
    }

    /**
     * Checks if product is available in stock.
     * 
     * @return true if stock > 0, false otherwise
     */
    public boolean isAvailable() {
        return stock > 0;
    }

    /**
     * Checks if threshold pricing is active.
     * 
     * @return true if stock <= threshold, false otherwise
     */
    public boolean isThresholdActive() {
        return stock <= threshold;
    }

    /**
     * Reduces stock by given amount.
     * 
     * @param amount Amount to reduce
     * @return true if successful, false if insufficient stock
     */
    public boolean reduceStock(double amount) {
        if (amount > stock) {
            return false;
        }
        stock -= amount;
        return true;
    }

    /**
     * Increases stock by given amount.
     * 
     * @param amount Amount to add
     */
    public void addStock(double amount) {
        stock += amount;
    }

    // ==================== GETTERS & SETTERS ====================
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getStock() {
        return stock;
    }

    public void setStock(double stock) {
        this.stock = stock;
    }

    public double getThreshold() {
        return threshold;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    public byte[] getImageData() {
        return imageData;
    }

    public void setImageData(byte[] imageData) {
        this.imageData = imageData;
    }

    public String getImageLocation() {
        return imageLocation;
    }

    public void setImageLocation(String imageLocation) {
        this.imageLocation = imageLocation;
    }

    @Override
    public String toString() {
        return String.format("%s (%s) - %.2f TL/kg [Stock: %.2f kg]%s",
            name, type, getEffectivePrice(), stock, 
            isThresholdActive() ? " ⚠️ THRESHOLD ACTIVE" : "");
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Product product = (Product) obj;
        return id == product.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}
