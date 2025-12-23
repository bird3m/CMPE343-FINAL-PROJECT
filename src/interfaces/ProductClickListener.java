package interfaces;

import models.Product;

/**
 * Product Click Listener Interface
 * Used for handling product card clicks
 * 
 * @author Group04
 */
public interface ProductClickListener {
    
    /**
     * Called when a product is clicked
     * @param product The clicked product
     */
    void onProductClicked(Product product);
    
    /**
     * Called when add to cart is requested for a product
     * @param product The product to add
     * @param quantity The quantity in kg
     */
    void onAddToCart(Product product, double quantity);
}