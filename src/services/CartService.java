package services;

import models.OrderItem;
import models.Product;
import java.util.ArrayList;
import java.util.List;

public class CartService {

    /**
     * In-memory shopping cart used by the application.
     * This is a simple static list representing the current cart contents.
     */
    private static List<OrderItem> cartItems = new ArrayList<>();

    /**
     * Add a product to the cart.
     * Pricing rule: quantity above the threshold is charged at double price.
     * @param product product to add
     * @param quantity quantity to add
     * @return total price added for the given quantity
     */
    public static double addToCart(Product product, double quantity) {
        double stock = product.getStock();
        double threshold = product.getThreshold();
        double basePrice = product.getPrice();

        double normalQty = 0.0;
        if (stock > threshold) {
            normalQty = Math.max(0.0, Math.min(quantity, stock - threshold));
        }
        double doubledQty = quantity - normalQty;

        double addedTotal = normalQty * basePrice + doubledQty * basePrice * 2.0;
        double addedAvgPrice = addedTotal / quantity;

        // If the product is already in the cart, increase quantity and recalculate average unit price
        for (OrderItem item : cartItems) {
            if (item.getProductId() == product.getId()) {
                double existingTotal = item.getPricePerUnit() * item.getQuantity();
                double newTotal = existingTotal + addedTotal;
                double newQty = item.getQuantity() + quantity;
                double newAvg = newTotal / newQty;
                item.setQuantity(newQty);
                item.setPricePerUnit(newAvg);
                return addedTotal;
            }
        }

        // Otherwise add a new order item (store average price per unit)
        cartItems.add(new OrderItem(product.getId(), product.getName(), quantity, addedAvgPrice));
        return addedTotal;
    }

    /**
     * Returns the list of cart items.
     */
    public static List<OrderItem> getCartItems() {
        return cartItems;
    }

    /**
     * Clears the cart contents.
     */
    public static void clearCart() {
        cartItems.clear();
    }

    /**
     * Calculates the total price of items in the cart.
     * @return total amount
     */
    public static double getTotal() {
        double total = 0;
        for (OrderItem item : cartItems) {
            total += item.getTotalPrice();
        }
        return total;
    }
}