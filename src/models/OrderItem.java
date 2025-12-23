package models;

public class OrderItem {
    private int id;
    private int orderId;
    private int productId;
    private String productName; // DB'den çekerken kolaylık olsun diye
    private double quantity;    // Kaç kilo?
    private double pricePerUnit; // O anki fiyatı

    public OrderItem(int id, int orderId, int productId, String productName, double quantity, double pricePerUnit) {
        this.id = id;
        this.orderId = orderId;
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.pricePerUnit = pricePerUnit;
    }

    // Yeni sipariş oluştururken
    public OrderItem(int productId, double quantity, double pricePerUnit) {
        this.productId = productId;
        this.quantity = quantity;
        this.pricePerUnit = pricePerUnit;
    }

    public int getId() { return id; }
    public int getOrderId() { return orderId; }
    public int getProductId() { return productId; }
    public String getProductName() { return productName; }
    public double getQuantity() { return quantity; }
    public double getPricePerUnit() { return pricePerUnit; }
}