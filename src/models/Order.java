package models;

import java.time.LocalDateTime;

public class Order {
    private int id;
    private int customerId;
    private String customerName; // Join ile gelen isim
    private int carrierId;
    private String status;
    private LocalDateTime orderTime;
    private double totalCost; // Arkadaşın bunu totalPrice yapmış olabilir, doğrusu bu.

    public Order(int id, int customerId, String customerName, int carrierId, String status, LocalDateTime orderTime, double totalCost) {
        this.id = id;
        this.customerId = customerId;
        this.customerName = customerName;
        this.carrierId = carrierId;
        this.status = status;
        this.orderTime = orderTime;
        this.totalCost = totalCost;
    }

    // Getterlar (Hata veren yerler buraları arıyor)
    public int getId() { return id; }
    public int getCustomerId() { return customerId; }
    public String getCustomerName() { return customerName; }
    public int getCarrierId() { return carrierId; }
    public String getStatus() { return status; }
    public LocalDateTime getOrderTime() { return orderTime; }
    public double getTotalCost() { return totalCost; }
    
    // Setterlar (Gerekirse)
    public void setCustomerName(String name) { this.customerName = name; }
}