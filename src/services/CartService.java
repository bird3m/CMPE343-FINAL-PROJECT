package services;

import models.OrderItem;
import models.Product;
import java.util.ArrayList;
import java.util.List;

public class CartService {
    
    // Sepetimiz (Static olduğu için uygulama boyunca tek bir sepet olur)
    private static List<OrderItem> cartItems = new ArrayList<>();

    // Sepete Ekle
    // Returns the total price added for the given quantity
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

        // Eğer ürün zaten sepette varsa miktarını artır ve fiyat ortalamasını yeniden hesapla
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

        // Yoksa yeni ekle (fiyat per unit olarak ortalama fiyatı sakla)
        cartItems.add(new OrderItem(product.getId(), product.getName(), quantity, addedAvgPrice));
        return addedTotal;
    }

    // Sepeti Getir
    public static List<OrderItem> getCartItems() {
        return cartItems;
    }

    // Sepeti Temizle (Siparişten sonra)
    public static void clearCart() {
        cartItems.clear();
    }
    
    // Toplam Tutar
    public static double getTotal() {
        double total = 0;
        for (OrderItem item : cartItems) {
            total += item.getTotalPrice();
        }
        return total;
    }
}