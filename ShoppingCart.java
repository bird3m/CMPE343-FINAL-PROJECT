import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

/**
 * Shopping Cart class for GreenGrocer Application.
 * Handles cart operations: add, remove, update, merge items.
 * Calculates total with VAT and applies discounts.
 * 
 * @author GroupXX
 * @version 1.0
 */
public class ShoppingCart {
    
    // Cart items: Product ID -> CartItem
    private HashMap<Integer, CartItem> items;
    
    // Customer who owns this cart
    private int customerId;
    
    // VAT rate (18% in Turkey)
    private static final double VAT_RATE = 0.18;
    
    // Minimum cart value to complete purchase
    private static final double MINIMUM_CART_VALUE = 50.0;
    
    // Applied discount coupon
    private String appliedCoupon;
    private double couponDiscount; // percentage
    
    // Loyalty discount
    private double loyaltyDiscount; // percentage
    
    // Validation helper
    private InputValidation validator;

    /**
     * Constructor for ShoppingCart.
     * 
     * @param customerId The customer ID who owns this cart
     */
    public ShoppingCart(int customerId) {
        this.customerId = customerId;
        this.items = new HashMap<>();
        this.appliedCoupon = null;
        this.couponDiscount = 0.0;
        this.loyaltyDiscount = 0.0;
        this.validator = new InputValidation();
    }

    /**
     * Adds item to cart or updates quantity if already exists (MERGE logic).
     * 
     * @param product The product to add
     * @param amount The amount in kilograms
     * @return true if added successfully, false otherwise
     */
    public boolean addItem(Product product, double amount) {
        try {
            // Validate amount
            if (!validator.validateProductAmount(amount)) {
                return false;
            }
            
            // Check stock availability
            if (!validator.validateStockAvailability(amount, product.getStock())) {
                return false;
            }
            
            int productId = product.getId();
            
            // MERGE LOGIC: If product already in cart, add to existing amount
            if (items.containsKey(productId)) {
                CartItem existingItem = items.get(productId);
                double newAmount = existingItem.getAmount() + amount;
                
                // Check if merged amount exceeds stock
                if (!validator.validateStockAvailability(newAmount, product.getStock())) {
                    System.err.println("Cannot add! Total amount would exceed available stock.");
                    return false;
                }
                
                existingItem.setAmount(newAmount);
                System.out.println("✓ Merged: " + product.getName() + " - Total: " + newAmount + " kg");
            } else {
                // Add new item
                CartItem newItem = new CartItem(product, amount);
                items.put(productId, newItem);
                System.out.println("✓ Added: " + product.getName() + " - " + amount + " kg");
            }
            
            return true;
            
        } catch (Exception e) {
            System.err.println("Error adding item to cart: " + e.getMessage());
            return false;
        }
    }

    /**
     * Removes item from cart completely.
     * 
     * @param productId The product ID to remove
     * @return true if removed successfully, false otherwise
     */
    public boolean removeItem(int productId) {
        if (items.containsKey(productId)) {
            CartItem removed = items.remove(productId);
            System.out.println("✓ Removed: " + removed.getProduct().getName());
            return true;
        } else {
            System.err.println("Product not found in cart!");
            return false;
        }
    }

    /**
     * Updates the amount of an existing item in cart.
     * 
     * @param productId The product ID
     * @param newAmount The new amount in kilograms
     * @return true if updated successfully, false otherwise
     */
    public boolean updateItemAmount(int productId, double newAmount) {
        if (!items.containsKey(productId)) {
            System.err.println("Product not found in cart!");
            return false;
        }
        
        if (!validator.validateProductAmount(newAmount)) {
            return false;
        }
        
        CartItem item = items.get(productId);
        Product product = item.getProduct();
        
        // Check stock availability for new amount
        if (!validator.validateStockAvailability(newAmount, product.getStock())) {
            return false;
        }
        
        item.setAmount(newAmount);
        System.out.println("✓ Updated: " + product.getName() + " - New amount: " + newAmount + " kg");
        return true;
    }

    /**
     * Clears all items from cart.
     */
    public void clearCart() {
        items.clear();
        appliedCoupon = null;
        couponDiscount = 0.0;
        System.out.println("✓ Cart cleared");
    }

    /**
     * Calculates subtotal (without VAT and discounts).
     * 
     * @return The subtotal amount
     */
    public double calculateSubtotal() {
        double subtotal = 0.0;
        for (CartItem item : items.values()) {
            subtotal += item.getTotalPrice();
        }
        return subtotal;
    }

    /**
     * Calculates VAT amount.
     * 
     * @return The VAT amount
     */
    public double calculateVAT() {
        return calculateSubtotal() * VAT_RATE;
    }

    /**
     * Calculates total discount amount (coupon + loyalty).
     * 
     * @return The total discount amount
     */
    public double calculateTotalDiscount() {
        double subtotal = calculateSubtotal();
        double totalDiscountPercentage = couponDiscount + loyaltyDiscount;
        
        // Cap discount at 50%
        if (totalDiscountPercentage > 50) {
            totalDiscountPercentage = 50;
        }
        
        return subtotal * (totalDiscountPercentage / 100.0);
    }

    /**
     * Calculates final total (subtotal + VAT - discounts).
     * 
     * @return The final total amount
     */
    public double calculateTotal() {
        double subtotal = calculateSubtotal();
        double vat = calculateVAT();
        double discount = calculateTotalDiscount();
        
        return subtotal + vat - discount;
    }

    /**
     * Applies a discount coupon to the cart.
     * 
     * @param couponCode The coupon code
     * @param discountPercentage The discount percentage (0-100)
     * @return true if applied successfully, false otherwise
     */
    public boolean applyCoupon(String couponCode, double discountPercentage) {
        if (!validator.validateCouponCode(couponCode)) {
            return false;
        }
        
        if (!validator.validateDiscountPercentage(discountPercentage)) {
            return false;
        }
        
        if (appliedCoupon != null) {
            System.err.println("A coupon is already applied! Remove it first.");
            return false;
        }
        
        this.appliedCoupon = couponCode;
        this.couponDiscount = discountPercentage;
        System.out.println("✓ Coupon applied: " + couponCode + " (" + discountPercentage + "% off)");
        return true;
    }

    /**
     * Removes the applied coupon.
     */
    public void removeCoupon() {
        if (appliedCoupon != null) {
            System.out.println("✓ Coupon removed: " + appliedCoupon);
            appliedCoupon = null;
            couponDiscount = 0.0;
        }
    }

    /**
     * Sets loyalty discount based on customer's past purchases.
     * 
     * @param discountPercentage The loyalty discount percentage
     * @return true if set successfully, false otherwise
     */
    public boolean setLoyaltyDiscount(double discountPercentage) {
        if (!validator.validateDiscountPercentage(discountPercentage)) {
            return false;
        }
        
        this.loyaltyDiscount = discountPercentage;
        System.out.println("✓ Loyalty discount applied: " + discountPercentage + "%");
        return true;
    }

    /**
     * Checks if cart meets minimum value requirement.
     * 
     * @return true if minimum met, false otherwise
     */
    public boolean meetsMinimumValue() {
        return validator.validateMinimumCartValue(calculateTotal(), MINIMUM_CART_VALUE);
    }

    /**
     * Gets all items in cart.
     * 
     * @return List of CartItems
     */
    public List<CartItem> getItems() {
        return new ArrayList<>(items.values());
    }

    /**
     * Gets number of different products in cart.
     * 
     * @return Number of different products
     */
    public int getItemCount() {
        return items.size();
    }

    /**
     * Checks if cart is empty.
     * 
     * @return true if empty, false otherwise
     */
    public boolean isEmpty() {
        return items.isEmpty();
    }

    /**
     * Gets a summary of the cart for display.
     * 
     * @return Cart summary as string
     */
    public String getCartSummary() {
        if (isEmpty()) {
            return "Cart is empty";
        }
        
        StringBuilder summary = new StringBuilder();
        summary.append("=== CART SUMMARY ===\n\n");
        
        for (CartItem item : items.values()) {
            summary.append(String.format("%s - %.2f kg × %.2f TL = %.2f TL\n",
                item.getProduct().getName(),
                item.getAmount(),
                item.getProduct().getPrice(),
                item.getTotalPrice()));
        }
        
        summary.append("\n-------------------\n");
        summary.append(String.format("Subtotal: %.2f TL\n", calculateSubtotal()));
        summary.append(String.format("VAT (18%%): %.2f TL\n", calculateVAT()));
        
        if (calculateTotalDiscount() > 0) {
            summary.append(String.format("Discount: -%.2f TL\n", calculateTotalDiscount()));
            if (couponDiscount > 0) {
                summary.append(String.format("  - Coupon (%s): %.0f%%\n", appliedCoupon, couponDiscount));
            }
            if (loyaltyDiscount > 0) {
                summary.append(String.format("  - Loyalty: %.0f%%\n", loyaltyDiscount));
            }
        }
        
        summary.append("-------------------\n");
        summary.append(String.format("TOTAL: %.2f TL\n", calculateTotal()));
        
        if (!meetsMinimumValue()) {
            summary.append(String.format("\n⚠ Minimum cart value: %.2f TL\n", MINIMUM_CART_VALUE));
        }
        
        return summary.toString();
    }

    // ==================== GETTERS ====================
    
    public int getCustomerId() {
        return customerId;
    }

    public String getAppliedCoupon() {
        return appliedCoupon;
    }

    public double getCouponDiscount() {
        return couponDiscount;
    }

    public double getLoyaltyDiscount() {
        return loyaltyDiscount;
    }

    public static double getMinimumCartValue() {
        return MINIMUM_CART_VALUE;
    }

    public static double getVatRate() {
        return VAT_RATE;
    }

    /**
     * Inner class representing a single item in the cart.
     */
    public static class CartItem {
        private Product product;
        private double amount; // in kilograms

        public CartItem(Product product, double amount) {
            this.product = product;
            this.amount = amount;
        }

        public Product getProduct() {
            return product;
        }

        public double getAmount() {
            return amount;
        }

        public void setAmount(double amount) {
            this.amount = amount;
        }

        /**
         * Calculates total price for this item (considering threshold pricing).
         * 
         * @return Total price for this item
         */
        public double getTotalPrice() {
            double pricePerKg = product.getPrice();
            
            // Check if threshold pricing applies
            if (product.getStock() <= product.getThreshold()) {
                pricePerKg *= 2; // Double the price!
            }
            
            return amount * pricePerKg;
        }

        @Override
        public String toString() {
            return String.format("%s - %.2f kg @ %.2f TL/kg = %.2f TL",
                product.getName(), amount, product.getPrice(), getTotalPrice());
        }
    }
}
