package models;

/**
 * Carrier Rating Model.
 * Represents a customer's rating for a carrier after delivery.
 * 
 * 
 * @author Group04
 * @version 1.0
 */
public class CarrierRating {
    private int id;
    private int carrierId;
    private int customerId;  
    private int orderId;    
    private int score;       // Rating: 1-5 stars
    private String comment;  // Optional comment
    
    /**
     * Full Constructor (recommended).
     * Use this when creating a new rating with all required fields.
     * 
     * @param carrierId ID of the carrier being rated
     * @param customerId ID of the customer giving the rating
     * @param orderId ID of the delivered order
     * @param score Rating score (1-5)
     * @param comment Optional comment
     */
    public CarrierRating(int carrierId, int customerId, int orderId, int score, String comment) {
        this.carrierId = carrierId;
        this.customerId = customerId;
        this.orderId = orderId;
        this.score = score;
        this.comment = comment;
    }
    
    /**
     * Legacy Constructor (backward compatibility).
     * For existing code that doesn't provide customerId/orderId.
     * 
     * @deprecated Use full constructor instead
     * @param carrierId ID of the carrier
     * @param score Rating score (1-5)
     * @param comment Optional comment
     */
    @Deprecated
    public CarrierRating(int carrierId, int score, String comment) {
        this.carrierId = carrierId;
        this.score = score;
        this.comment = comment;
        // customerId and orderId will need to be set manually
    }
    
    // ==================== GETTERS ====================
    
    public int getId() { 
        return id; 
    }
    
    public int getCarrierId() { 
        return carrierId; 
    }
    
    /**
     * Get the customer ID who gave this rating.
     * @return Customer ID
     */
    public int getCustomerId() { 
        return customerId; 
    }
    
    /**
     * Get the order ID this rating is associated with.
     * @return Order ID
     */
    public int getOrderId() { 
        return orderId; 
    }
    
    public int getScore() { 
        return score; 
    }
    
    public String getComment() { 
        return comment; 
    }
    
    // ==================== SETTERS ====================
    
    public void setId(int id) { 
        this.id = id; 
    }
    
    public void setCarrierId(int carrierId) { 
        this.carrierId = carrierId; 
    }
    
    /**
     * Set the customer ID who gave this rating.
     * @param customerId Customer ID
     */
    public void setCustomerId(int customerId) { 
        this.customerId = customerId; 
    }
    
    /**
     * Set the order ID this rating is associated with.
     * @param orderId Order ID
     */
    public void setOrderId(int orderId) { 
        this.orderId = orderId; 
    }
    
    public void setScore(int score) { 
        this.score = score; 
    }
    
    public void setComment(String comment) { 
        this.comment = comment; 
    }
    
    @Override
    public String toString() {
        return "CarrierRating{" +
                "carrierId=" + carrierId +
                ", customerId=" + customerId +
                ", orderId=" + orderId +
                ", score=" + score +
                ", comment='" + comment + '\'' +
                '}';
    }
}