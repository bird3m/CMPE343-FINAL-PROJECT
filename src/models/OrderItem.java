package models;

public class OrderItem {
    // Owner tablosu için gerekenler
    private int orderId;
    private String customerName;
    private String orderDate;
    private String status;
    private double total; // Owner tablosundaki toplam tutar

    // Sepet için gerekenler
    private int productId;
    private String productName;
    private double quantity;
    private double pricePerUnit;

    // --- Constructor 1: OWNER EKRANI İÇİN ---
    public OrderItem(int id, String name, String date, double total, String status) {
        this.orderId = id;
        this.customerName = name;
        this.orderDate = date;
        this.total = total;
        this.status = status;
    }

    // --- Constructor 2: SEPET İÇİN ---
    public OrderItem(int productId, String productName, double quantity, double pricePerUnit) {
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.pricePerUnit = pricePerUnit;
    }

    // Getterlar (Hepsi Lazım)
    public int getOrderId() { return orderId; }
    public String getCustomerName() { return customerName; }
    public String getOrderDate() { return orderDate; }
    public double getTotal() { return total; } // Owner tablosu bunu çağırır
    public String getStatus() { return status; }
    
    public int getProductId() { return productId; }
    public String getProductName() { return productName; }
    public double getQuantity() { return quantity; }
    public double getPricePerUnit() { return pricePerUnit; }
    
    // Sepet için miktar artırma
    public void setQuantity(double q) { this.quantity = q; }
    
    // Ürün bazlı toplam tutar (Sepet için)
    public double getTotalPrice() { return quantity * pricePerUnit; }
}