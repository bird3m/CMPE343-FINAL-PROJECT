package interfaces;

import models.Product;

/**
 * Interface for product click events
 */
public interface ProductClickListener {
    void onProductClicked(Product product);
}