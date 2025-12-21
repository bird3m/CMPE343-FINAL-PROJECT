public class Product {
    private int id;
    private String name;
    private String type; // vegetable or fruit 
    private double price;
    private double stock; // stock_kg
    private double threshold; // threshold_kg
    private byte[] image; // image_blob

    public Product(int id, String name, String type, double price, double stock, double threshold, byte[] image) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.price = price;
        this.stock = stock;
        this.threshold = threshold;
        this.image = image;
    }

    // Getters and Setters
    public int getId() { return id; }
    public String getName() { return name; }
    public String getType() { return type; }
    public double getPrice() { return price; }
    public double getStock() { return stock; }
    public void setStock(double stock) { this.stock = stock; } //necessary for updating the stock
    public double getThreshold() { return threshold; }
    public byte[] getImage() { return image; }
}