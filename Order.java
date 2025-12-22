import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Handles order processing, carrier assignments, and status management.
 * Complies with Project 3 requirements for delivery timing and invoicing.
 */
public class Order {
    
    // Status constants for the carrier interface [cite: 44]
    public enum OrderStatus {
        AVAILABLE,    // Ready for a carrier to pick up
        SELECTED,     // Carrier has picked it up [cite: 46]
        DELIVERING,   // In transit
        COMPLETED,    // Money received and delivered [cite: 46]
        CANCELLED     // Cancelled by customer 
    }

    private int orderId;
    private ShoppingCart cart;
    private LocalDateTime orderTime;
    private LocalDateTime requestedDeliveryTime;
    private LocalDateTime actualDeliveryTime;
    private String customerName;
    private String customerAddress;
    private String customerPhone;
    private int carrierId;
    private OrderStatus status;

    public Order(ShoppingCart cart, LocalDateTime requestedDeliveryTime, 
                 String customerName, String customerAddress, String customerPhone) {
        
        // Madde 33: Delivery must be within 48 hours 
        InputValidation validator = new InputValidation();
        if (!validator.validateDeliveryDate(requestedDeliveryTime)) {
            throw new IllegalArgumentException("Invalid delivery date! Must be within 48 hours.");
        }

        this.cart = cart;
        this.orderTime = LocalDateTime.now();
        this.requestedDeliveryTime = requestedDeliveryTime;
        this.customerName = customerName;
        this.customerAddress = customerAddress;
        this.customerPhone = customerPhone;
        this.status = OrderStatus.AVAILABLE; // Initial state [cite: 44]
    }

    // ==================== WORKFLOW METHODS ====================

    /**
     * Madde 45-46: Carrier selects the order [cite: 45, 46]
     */
    public void assignCarrier(int carrierId) {
        if (this.status == OrderStatus.AVAILABLE) {
            this.carrierId = carrierId;
            this.status = OrderStatus.SELECTED;
        }
    }

    public void startDelivery() {
        if (this.status == OrderStatus.SELECTED) {
            this.status = OrderStatus.DELIVERING;
        }
    }

    /**
     * Mad de 46: Carrier completes the order [cite: 46]
     */
    public void completeDelivery(LocalDateTime deliveryTime) {
        this.actualDeliveryTime = deliveryTime;
        this.status = OrderStatus.COMPLETED;
    }

    /**
     * Madde 42: Allows customers to cancel orders within 48h window [cite: 42, 33]
     */
    public boolean cancelOrder() {
        if (status == OrderStatus.AVAILABLE || status == OrderStatus.SELECTED) {
            this.status = OrderStatus.CANCELLED;
            return true;
        }
        return false;
    }

    public boolean canBeCancelled() {
        return status == OrderStatus.AVAILABLE || status == OrderStatus.SELECTED;
    }

    // ==================== SUMMARY & INVOICE [cite: 34, 40] ====================

    /**
     * Madde 40 & 59: Generates content for PDF/CLOB invoice 
     */
    public String generateInvoiceContent() {
        StringBuilder invoice = new StringBuilder();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        invoice.append("***** INVOICE *****\n");
        invoice.append("Order ID: ").append(orderId).append("\n");
        invoice.append("Date: ").append(orderTime.format(dtf)).append("\n");
        invoice.append("Customer: ").append(customerName).append("\n");
        invoice.append("Address: ").append(customerAddress).append("\n");
        invoice.append("-------------------\n");
        
        for (ShoppingCart.CartItem item : cart.getItems()) {
            invoice.append(String.format("%s: %.2f kg x %.2f TL = %.2f TL\n",
                    item.getProduct().getName(), item.getAmount(), 
                    item.getProduct().getEffectivePrice(), item.getTotalPrice()));
        }

        invoice.append("-------------------\n");
        invoice.append(String.format("Subtotal: %.2f TL\n", cart.calculateSubtotal()));
        invoice.append(String.format("VAT (18%%): %.2f TL\n", cart.calculateVAT()));
        invoice.append(String.format("Total Discount: -%.2f TL\n", cart.calculateTotalDiscount()));
        invoice.append(String.format("GRAND TOTAL: %.2f TL\n", cart.calculateTotal()));
        invoice.append("*******************");

        return invoice.toString();
    }

    public String getOrderSummary() {
        return String.format("Order Summary for %s\nStatus: %s\nTotal Items: %d\nFinal Total: %.2f TL",
                customerName, status, cart.getItemCount(), cart.calculateTotal());
    }

    // ==================== GETTERS & SETTERS ====================
    
    public void setOrderId(int orderId) { this.orderId = orderId; }
    public OrderStatus getStatus() { return status; }
}
