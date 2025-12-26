package services;

import models.OrderItem;
import models.Product;
import java.util.ArrayList;
import java.util.List;

public class CartService {
    
    // Sepetimiz (Static olduğu için uygulama boyunca tek bir sepet olur)
    private static List<OrderItem> cartItems = new ArrayList<>();

    // Sepete Ekle
    public static void addToCart(Product product, double quantity) {
        // Eğer ürün zaten sepette varsa miktarını artır
        for (OrderItem item : cartItems) {
            if (item.getProductId() == product.getId()) {
                item.setQuantity(item.getQuantity() + quantity);
                return;
            }
        }
        // Yoksa yeni ekle
        // OrderItem constructor: (int productId, String productName, double quantity, double pricePerUnit)
        // NOT: Senin modeline uygun hale getirdik.
        // OrderItem modelini birazdan kontrol edeceğiz, eğer model farklıysa burayı düzeltiriz.
        cartItems.add(new OrderItem(product.getId(), product.getName(), quantity, product.getCurrentPrice()));
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