package models;

import java.time.LocalDateTime;

/**
 * Order Model.
 * Represents a customer order in the system.
 * Corresponds to the 'orderinfo' table in the database.
 *
 * @author Group04
 * @version 2.0
 */
public class Order {
    private int id;
    private int customerId;
    private String customerName; // Fetched via JOIN from userinfo
    private int carrierId;
    private String status;       // CREATED, ASSIGNED, DELIVERED, CANCELLED
    private LocalDateTime deliveryTime; // Renamed from orderTime to match requirements
    private double totalCost;

    /**
     * Constructor.
     *
     * @param id            Order ID
     * @param customerId    ID of the customer who placed the order
     * @param customerName  Display name of the customer (and address)
     * @param carrierId     ID of the carrier (0 if not assigned)
     * @param status        Current status of the order
     * @param deliveryTime  The requested delivery time
     * @param totalCost     Total cost of the order
     */
    public Order(int id, int customerId, String customerName, int carrierId, String status, LocalDateTime deliveryTime, double totalCost) {
        this.id = id;
        this.customerId = customerId;
        this.customerName = customerName;
        this.carrierId = carrierId;
        this.status = status;
        this.deliveryTime = deliveryTime;
        this.totalCost = totalCost;
    }

    // ==================== GETTERS ====================

    public int getId() {
        return id;
    }

    public int getCustomerId() {
        return customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public int getCarrierId() {
        return carrierId;
    }

    public String getStatus() {
        return status;
    }

    /**
     * Gets the requested delivery time.
     * This is the method CarrierMainController was looking for!
     * @return LocalDateTime of delivery.
     */
    public LocalDateTime getDeliveryTime() {
        return deliveryTime;
    }

    public double getTotalCost() {
        return totalCost;
    }
    
    // ==================== SETTERS ====================
    
    public void setCustomerName(String name) {
        this.customerName = name;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    
    public void setCarrierId(int carrierId) {
        this.carrierId = carrierId;
    }
    
    @Override
    public String toString() {
        return "Order #" + id + " - " + status; 
    }
}