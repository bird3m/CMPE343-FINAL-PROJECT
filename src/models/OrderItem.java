package models;

/**
 * OrderItem Model
 * 
 * Used for TWO purposes:
 * 1. Shopping Cart - Individual product items in cart
 * 2. Owner Dashboard - Order summary display
 * 
 * @author Group04
 * @version 2.0
 */
public class OrderItem {
    
    // --- DATABASE FIELDS (for order items table) ---
    private int id;              // orderitem_id (primary key)
    private int orderId;         // Foreign key to orderinfo
    private int productId;       // Foreign key to productinfo
    private String productName;  // Product name (for display)
    private double quantity;     // Amount in kg
    private double pricePerUnit; // Price per kg at time of order
    
    // --- OWNER DASHBOARD FIELDS (for display only) ---
    private String customerName; // Customer who placed order
    private String orderDate;    // When order was placed
    private String status;       // Order status
    private double total;        // Total order amount
    
    
    // ========================================
    // CONSTRUCTORS
    // ========================================
    
    /**
     * Constructor 1: SHOPPING CART
     * Used when adding items to cart
     * 
     * @param productId Product ID
     * @param productName Product name
     * @param quantity Amount in kg
     * @param pricePerUnit Price per kg
     */
    public OrderItem(int productId, String productName, double quantity, double pricePerUnit) {
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.pricePerUnit = pricePerUnit;
    }
    
    /**
     * Constructor 2: DATABASE RETRIEVAL
     * Used when loading order items from database
     * 
     * @param id OrderItem ID
     * @param orderId Order ID
     * @param productId Product ID
     * @param productName Product name
     * @param quantity Amount in kg
     * @param pricePerUnit Price per kg
     */
    public OrderItem(int id, int orderId, int productId, String productName, 
                     double quantity, double pricePerUnit) {
        this.id = id;
        this.orderId = orderId;
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.pricePerUnit = pricePerUnit;
    }
    
    /**
     * Constructor 3: OWNER DASHBOARD DISPLAY
     * Used for showing order summaries in Owner table
     * 
     * @param orderId Order ID
     * @param customerName Customer name
     * @param orderDate Order date
     * @param total Total amount
     * @param status Order status
     */
    public OrderItem(int orderId, String customerName, String orderDate, 
                     double total, String status) {
        this.orderId = orderId;
        this.customerName = customerName;
        this.orderDate = orderDate;
        this.total = total;
        this.status = status;
    }
    
    
    // ========================================
    // GETTERS & SETTERS
    // ========================================
    
    // --- Database Fields ---
    public int getId() { 
        return id; 
    }
    
    public void setId(int id) { 
        this.id = id; 
    }
    
    public int getOrderId() { 
        return orderId; 
    }
    
    public void setOrderId(int orderId) { 
        this.orderId = orderId; 
    }
    
    public int getProductId() { 
        return productId; 
    }
    
    public void setProductId(int productId) { 
        this.productId = productId; 
    }
    
    public String getProductName() { 
        return productName; 
    }
    
    public void setProductName(String productName) { 
        this.productName = productName; 
    }
    
    public double getQuantity() { 
        return quantity; 
    }
    
    public void setQuantity(double quantity) { 
        this.quantity = quantity; 
    }
    
    public double getPricePerUnit() { 
        return pricePerUnit; 
    }
    
    public void setPricePerUnit(double pricePerUnit) { 
        this.pricePerUnit = pricePerUnit; 
    }
    
    // --- Owner Dashboard Fields ---
    public String getCustomerName() { 
        return customerName; 
    }
    
    public void setCustomerName(String customerName) { 
        this.customerName = customerName; 
    }
    
    public String getOrderDate() { 
        return orderDate; 
    }
    
    public void setOrderDate(String orderDate) { 
        this.orderDate = orderDate; 
    }
    
    public String getStatus() { 
        return status; 
    }
    
    public void setStatus(String status) { 
        this.status = status; 
    }
    
    public double getTotal() { 
        return total; 
    }
    
    public void setTotal(double total) { 
        this.total = total; 
    }
    
    
    // ========================================
    // CALCULATED METHODS
    // ========================================
    
    /**
     * Calculate total price for this item
     * Used in shopping cart
     * 
     * @return quantity * pricePerUnit
     */
    public double getTotalPrice() { 
        return quantity * pricePerUnit; 
    }
    
    
    // ========================================
    // DISPLAY METHODS
    // ========================================
    
    /**
     * String representation for debugging
     */
    @Override
    public String toString() {
        if (productName != null) {
            // Cart item format
            return String.format("%s - %.2f kg @ %.2f₺/kg = %.2f₺", 
                productName, quantity, pricePerUnit, getTotalPrice());
        } else if (customerName != null) {
            // Owner dashboard format
            return String.format("Order #%d - %s - %.2f₺ - %s", 
                orderId, customerName, total, status);
        } else {
            return "Empty OrderItem";
        }
    }
}