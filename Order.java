
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import ShoppingCart.CartItem;

/**
 * Order class for GreenGrocer Application.
 * Handles order creation, validation, cancellation, and status management.
 * 
 * @author Group04
 * @version 1.0
 */
public class Order {
    
    // Order details
    private int orderId;
    private int customerId;
    private Integer carrierId; // null if not assigned
    private List<OrderItem> items;
    
    // Pricing
    private double subtotal;
    private double vatAmount;
    private double discountAmount;
    private double totalCost;
    
    // Dates
    private LocalDateTime orderTime;
    private LocalDateTime requestedDeliveryTime;
    private LocalDateTime actualDeliveryTime;
    
    // Status
    private OrderStatus status;
    
    // Applied discounts
    private String couponCode;
    private double couponDiscount;
    private double loyaltyDiscount;
    
    // Customer info
    private String customerName;
    private String customerAddress;
    private String customerPhone;
    
    // Invoice
    private byte[] invoicePDF; // Stored as BLOB
    
    // Validation helper
    private InputValidation validator;
    
    // Order cancellation time limit (in hours)
    private static final int CANCELLATION_TIME_LIMIT = 2;

    /**
     * Order status enumeration.
     */
    public enum OrderStatus {
        PENDING,        // Order created, waiting for carrier
        ASSIGNED,       // Carrier assigned
        IN_DELIVERY,    // Carrier picked up
        DELIVERED,      // Successfully delivered
        CANCELLED       // Cancelled by customer
    }

    /**
     * Constructor for creating a new order from shopping cart.
     * 
     * @param cart The shopping cart
     * @param requestedDeliveryTime The requested delivery date/time
     * @param customerName Customer's name
     * @param customerAddress Customer's address
     * @param customerPhone Customer's phone
     */
    public Order(ShoppingCart cart, LocalDateTime requestedDeliveryTime, 
                 String customerName, String customerAddress, String customerPhone) {
        this.validator = new InputValidation();
        
        // Validate delivery time
        if (!validator.validateDeliveryDate(requestedDeliveryTime)) {
            throw new IllegalArgumentException("Invalid delivery date!");
        }
        
        // Validate customer info
        if (!validator.validateAddress(customerAddress)) {
            throw new IllegalArgumentException("Invalid address!");
        }
        
        if (!validator.validatePhoneNumber(customerPhone)) {
            throw new IllegalArgumentException("Invalid phone number!");
        }
        
        // Check minimum cart value
        if (!cart.meetsMinimumValue()) {
            throw new IllegalArgumentException("Cart does not meet minimum value requirement!");
        }
        
        // Initialize order
        this.customerId = cart.getCustomerId();
        this.orderTime = LocalDateTime.now();
        this.requestedDeliveryTime = requestedDeliveryTime;
        this.status = OrderStatus.PENDING;
        
        // Copy customer info
        this.customerName = customerName;
        this.customerAddress = customerAddress;
        this.customerPhone = customerPhone;
        
        // Copy cart items
        this.items = new ArrayList<>();
        for (ShoppingCart.CartItem cartItem : cart.getItems()) {
            OrderItem orderItem = new OrderItem(
                cartItem.getProduct().getId(),
                cartItem.getProduct().getName(),
                cartItem.getProduct().getType(),
                cartItem.getAmount(),
                cartItem.getProduct().getCurrentPrice(),
                cartItem.getTotalPrice()
            );
            this.items.add(orderItem);
        }
        
        // Copy pricing
        this.subtotal = cart.calculateSubtotal();
        this.vatAmount = cart.calculateVAT();
        this.discountAmount = cart.calculateTotalDiscount();
        this.totalCost = cart.calculateTotal();
        
        // Copy discount info
        this.couponCode = cart.getAppliedCoupon();
        this.couponDiscount = cart.getCouponDiscount();
        this.loyaltyDiscount = cart.getLoyaltyDiscount();
        
        System.out.println("✓ Order created successfully!");
    }

    /**
     * Constructor for loading existing order from database.
     */
    public Order(int orderId, int customerId, LocalDateTime orderTime, 
                 LocalDateTime requestedDeliveryTime, OrderStatus status) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.orderTime = orderTime;
        this.requestedDeliveryTime = requestedDeliveryTime;
        this.status = status;
        this.items = new ArrayList<>();
        this.validator = new InputValidation();
    }

    /**
     * Assigns a carrier to this order.
     * 
     * @param carrierId The carrier ID
     * @return true if assigned successfully, false otherwise
     */
    public boolean assignCarrier(int carrierId) {
        if (status != OrderStatus.PENDING) {
            System.err.println("Cannot assign carrier! Order status: " + status);
            return false;
        }
        
        this.carrierId = carrierId;
        this.status = OrderStatus.ASSIGNED;
        System.out.println("✓ Carrier assigned to order #" + orderId);
        return true;
    }

    /**
     * Marks order as picked up by carrier.
     * 
     * @return true if successful, false otherwise
     */
    public boolean startDelivery() {
        if (status != OrderStatus.ASSIGNED) {
            System.err.println("Cannot start delivery! Order status: " + status);
            return false;
        }
        
        this.status = OrderStatus.IN_DELIVERY;
        System.out.println("✓ Order #" + orderId + " is now in delivery");
        return true;
    }

    /**
     * Completes the order (carrier delivered).
     * 
     * @param actualDeliveryTime The actual delivery time
     * @return true if completed successfully, false otherwise
     */
    public boolean completeDelivery(LocalDateTime actualDeliveryTime) {
        if (status != OrderStatus.IN_DELIVERY && status != OrderStatus.ASSIGNED) {
            System.err.println("Cannot complete delivery! Order status: " + status);
            return false;
        }
        
        if (actualDeliveryTime.isBefore(orderTime)) {
            System.err.println("Delivery time cannot be before order time!");
            return false;
        }
        
        this.actualDeliveryTime = actualDeliveryTime;
        this.status = OrderStatus.DELIVERED;
        System.out.println("✓ Order #" + orderId + " delivered successfully!");
        return true;
    }

    /**
     * Cancels the order (only within time limit).
     * 
     * @return true if cancelled successfully, false otherwise
     */
    public boolean cancelOrder() {
        if (status == OrderStatus.DELIVERED) {
            System.err.println("Cannot cancel! Order already delivered.");
            return false;
        }
        
        if (status == OrderStatus.CANCELLED) {
            System.err.println("Order already cancelled!");
            return false;
        }
        
        // Check if within cancellation time limit
        LocalDateTime now = LocalDateTime.now();
        long hoursSinceOrder = java.time.Duration.between(orderTime, now).toHours();
        
        if (hoursSinceOrder > CANCELLATION_TIME_LIMIT) {
            System.err.println("Cannot cancel! Cancellation period expired (limit: " 
                + CANCELLATION_TIME_LIMIT + " hours)");
            return false;
        }
        
        this.status = OrderStatus.CANCELLED;
        System.out.println("✓ Order #" + orderId + " cancelled");
        return true;
    }

    /**
     * Checks if order can be cancelled.
     * 
     * @return true if can be cancelled, false otherwise
     */
    public boolean canBeCancelled() {
        if (status == OrderStatus.DELIVERED || status == OrderStatus.CANCELLED) {
            return false;
        }
        
        LocalDateTime now = LocalDateTime.now();
        long hoursSinceOrder = java.time.Duration.between(orderTime, now).toHours();
        
        return hoursSinceOrder <= CANCELLATION_TIME_LIMIT;
    }

    /**
     * Gets order summary for display.
     * 
     * @return Order summary as string
     */
    public String getOrderSummary() {
        StringBuilder summary = new StringBuilder();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        
        summary.append("=== ORDER #").append(orderId).append(" ===\n\n");
        summary.append("Status: ").append(status).append("\n");
        summary.append("Order Time: ").append(orderTime.format(formatter)).append("\n");
        summary.append("Requested Delivery: ").append(requestedDeliveryTime.format(formatter)).append("\n");
        
        if (actualDeliveryTime != null) {
            summary.append("Actual Delivery: ").append(actualDeliveryTime.format(formatter)).append("\n");
        }
        
        summary.append("\nCustomer: ").append(customerName).append("\n");
        summary.append("Address: ").append(customerAddress).append("\n");
        summary.append("Phone: ").append(customerPhone).append("\n");
        
        if (carrierId != null) {
            summary.append("Carrier ID: ").append(carrierId).append("\n");
        }
        
        summary.append("\n--- ITEMS ---\n");
        for (OrderItem item : items) {
            summary.append(String.format("%s - %.2f kg × %.2f TL = %.2f TL\n",
                item.productName, item.amount, item.pricePerKg, item.totalPrice));
        }
        
        summary.append("\n-------------------\n");
        summary.append(String.format("Subtotal: %.2f TL\n", subtotal));
        summary.append(String.format("VAT (18%%): %.2f TL\n", vatAmount));
        
        if (discountAmount > 0) {
            summary.append(String.format("Discount: -%.2f TL\n", discountAmount));
            if (couponDiscount > 0) {
                summary.append(String.format("  - Coupon (%s): %.0f%%\n", couponCode, couponDiscount));
            }
            if (loyaltyDiscount > 0) {
                summary.append(String.format("  - Loyalty: %.0f%%\n", loyaltyDiscount));
            }
        }
        
        summary.append("-------------------\n");
        summary.append(String.format("TOTAL: %.2f TL\n", totalCost));
        
        if (canBeCancelled()) {
            summary.append("\n⚠ This order can still be cancelled\n");
        }
        
        return summary.toString();
    }

    /**
     * Generates invoice data (to be stored as PDF/CLOB in database).
     * 
     * @return Invoice content as string
     */
    public String generateInvoiceContent() {
        StringBuilder invoice = new StringBuilder();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        invoice.append("========================================\n");
        invoice.append("        GREENGROCER INVOICE\n");
        invoice.append("========================================\n\n");
        
        invoice.append("Invoice Date: ").append(LocalDateTime.now().format(formatter)).append("\n");
        invoice.append("Order ID: ").append(orderId).append("\n");
        invoice.append("Order Date: ").append(orderTime.format(formatter)).append("\n\n");
        
        invoice.append("CUSTOMER INFORMATION:\n");
        invoice.append("Name: ").append(customerName).append("\n");
        invoice.append("Address: ").append(customerAddress).append("\n");
        invoice.append("Phone: ").append(customerPhone).append("\n\n");
        
        invoice.append("========================================\n");
        invoice.append("ITEMS:\n");
        invoice.append("----------------------------------------\n");
        
        for (OrderItem item : items) {
            invoice.append(String.format("%-20s %6.2f kg × %8.2f TL = %10.2f TL\n",
                item.productName, item.amount, item.pricePerKg, item.totalPrice));
        }
        
        invoice.append("----------------------------------------\n\n");
        
        invoice.append(String.format("Subtotal:           %10.2f TL\n", subtotal));
        invoice.append(String.format("VAT (18%%):          %10.2f TL\n", vatAmount));
        
        if (discountAmount > 0) {
            invoice.append(String.format("Discount:          -%10.2f TL\n", discountAmount));
        }
        
        invoice.append("========================================\n");
        invoice.append(String.format("TOTAL:              %10.2f TL\n", totalCost));
        invoice.append("========================================\n\n");
        
        invoice.append("Delivery Address: ").append(customerAddress).append("\n");
        invoice.append("Requested Delivery: ").append(requestedDeliveryTime.format(formatter)).append("\n\n");
        
        invoice.append("Thank you for shopping with GreenGrocer!\n");
        
        return invoice.toString();
    }

    // ==================== GETTERS & SETTERS ====================
    
    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getCustomerId() {
        return customerId;
    }

    public Integer getCarrierId() {
        return carrierId;
    }

    public List<OrderItem> getItems() {
        return new ArrayList<>(items);
    }

    public void addItem(OrderItem item) {
        this.items.add(item);
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    public double getVatAmount() {
        return vatAmount;
    }

    public void setVatAmount(double vatAmount) {
        this.vatAmount = vatAmount;
    }

    public double getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(double discountAmount) {
        this.discountAmount = discountAmount;
    }

    public double getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(double totalCost) {
        this.totalCost = totalCost;
    }

    public LocalDateTime getOrderTime() {
        return orderTime;
    }

    public LocalDateTime getRequestedDeliveryTime() {
        return requestedDeliveryTime;
    }

    public LocalDateTime getActualDeliveryTime() {
        return actualDeliveryTime;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerAddress() {
        return customerAddress;
    }

    public void setCustomerAddress(String customerAddress) {
        this.customerAddress = customerAddress;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public byte[] getInvoicePDF() {
        return invoicePDF;
    }

    public void setInvoicePDF(byte[] invoicePDF) {
        this.invoicePDF = invoicePDF;
    }

    public String getCouponCode() {
        return couponCode;
    }

    public double getCouponDiscount() {
        return couponDiscount;
    }

    public double getLoyaltyDiscount() {
        return loyaltyDiscount;
    }

    /**
     * Inner class representing a single item in an order.
     */
    public static class OrderItem {
        private int productId;
        private String productName;
        private String productType; // "vegetable" or "fruit"
        private double amount; // in kilograms
        private double pricePerKg;
        private double totalPrice;

        public OrderItem(int productId, String productName, String productType,
                        double amount, double pricePerKg, double totalPrice) {
            this.productId = productId;
            this.productName = productName;
            this.productType = productType;
            this.amount = amount;
            this.pricePerKg = pricePerKg;
            this.totalPrice = totalPrice;
        }

        // Getters
        public int getProductId() { return productId; }
        public String getProductName() { return productName; }
        public String getProductType() { return productType; }
        public double getAmount() { return amount; }
        public double getPricePerKg() { return pricePerKg; }
        public double getTotalPrice() { return totalPrice; }

        @Override
        public String toString() {
            return String.format("%s (%.2f kg) - %.2f TL", 
                productName, amount, totalPrice);
        }
    }
}
