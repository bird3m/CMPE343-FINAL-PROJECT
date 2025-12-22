package main;

import java.sql.Timestamp;

public class Order {
    private int id;
    private int customerId;
    private int carrierId; // Henüz atanmadıysa 0 veya null olabilir
    private double totalPrice;
    private String status; // PENDING, ON_THE_WAY, DELIVERED, CANCELLED
    private Timestamp orderDate;
    private String deliveryAddress;

    public Order(int id, int customerId, int carrierId, double totalPrice, String status, Timestamp orderDate, String deliveryAddress) {
        this.id = id;
        this.customerId = customerId;
        this.carrierId = carrierId;
        this.totalPrice = totalPrice;
        this.status = status;
        this.orderDate = orderDate;
        this.deliveryAddress = deliveryAddress;
    }

    // Yeni sipariş oluştururken ID ve Date otomatik atanır
    public Order(int customerId, double totalPrice, String deliveryAddress) {
        this.customerId = customerId;
        this.totalPrice = totalPrice;
        this.deliveryAddress = deliveryAddress;
        this.status = "PENDING";
    }

    // Getters
    public int getId() { return id; }
    public int getCustomerId() { return customerId; }
    public int getCarrierId() { return carrierId; }
    public double getTotalPrice() { return totalPrice; }
    public String getStatus() { return status; }
    public Timestamp getOrderDate() { return orderDate; }
    public String getDeliveryAddress() { return deliveryAddress; }
    
    // Setters
    public void setCarrierId(int carrierId) { this.carrierId = carrierId; }
    public void setStatus(String status) { this.status = status; }
}