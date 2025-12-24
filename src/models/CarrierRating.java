package models;

public class CarrierRating {
    private int id;
    private int carrierId;
    private int customerId;
    private int orderId;
    private int score; // 1 ile 5 arası
    private String comment;

    public CarrierRating(int carrierId, int customerId, int orderId, int score, String comment) {
        this.carrierId = carrierId;
        this.customerId = customerId;
        this.orderId = orderId;
        this.score = score;
        this.comment = comment;
    }

    // Getters
    public int getCarrierId() { return carrierId; }
    public int getScore() { return score; }
    public String getComment() { return comment; }
}