package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

/**
 * Shopping Cart Controller
 * 
 * Features:
 * - Display cart items in table
 * - Remove items from cart
 * - Calculate subtotal, VAT, discount
 * - Show final total
 * - Proceed to checkout
 * - Continue shopping
 * 
 * @author Group04
 * @version 1.0
 */
public class ShoppingCartController {
    
    @FXML private TableView<CartItem> cartTable;
    @FXML private TableColumn<CartItem, String> productColumn;
    @FXML private TableColumn<CartItem, Double> quantityColumn;
    @FXML private TableColumn<CartItem, Double> priceColumn;
    @FXML private TableColumn<CartItem, Double> totalColumn;
    
    @FXML private Label subtotalLabel;
    @FXML private Label vatLabel;
    @FXML private Label discountLabel;
    @FXML private Label totalLabel;
    
    @FXML private Button removeButton;
    @FXML private Button checkoutButton;
    @FXML private Button continueShoppingButton;
    
    private ObservableList<CartItem> cartItems;
    
    // Constants
    private static final double VAT_RATE = 0.18; // 18% KDV
    private static final double FREE_DELIVERY_THRESHOLD = 200.0;
    
    /**
     * Initialize - Called automatically after FXML is loaded
     */
    @FXML
    private void initialize() {
        // Setup table columns
        productColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        totalColumn.setCellValueFactory(new PropertyValueFactory<>("itemTotal"));
        
        // Format numeric columns
        quantityColumn.setCellFactory(tc -> new TableCell<CartItem, Double>() {
            @Override
            protected void updateItem(Double value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f kg", value));
                }
            }
        });
        
        priceColumn.setCellFactory(tc -> new TableCell<CartItem, Double>() {
            @Override
            protected void updateItem(Double value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f ₺", value));
                }
            }
        });
        
        totalColumn.setCellFactory(tc -> new TableCell<CartItem, Double>() {
            @Override
            protected void updateItem(Double value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f ₺", value));
                }
            }
        });
        
        // Initialize cart items
        cartItems = FXCollections.observableArrayList();
        cartTable.setItems(cartItems);
        
        // Load sample data for demo
        loadSampleCartItems();
        
        // Calculate totals
        updateTotals();
    }
    
    /**
     * Load sample cart items for demonstration
     * TODO: Replace with real cart data from CartService
     */
    private void loadSampleCartItems() {
        cartItems.add(new CartItem("Tomato", 2.0, 20.50));
        cartItems.add(new CartItem("Apple", 1.5, 24.50));
        cartItems.add(new CartItem("Potato", 3.0, 15.80));
        cartItems.add(new CartItem("Banana", 1.0, 80.80));
    }
    
    /**
     * Calculate and update all totals
     */
    private void updateTotals() {
        // Calculate subtotal
        double subtotal = 0.0;
        for (CartItem item : cartItems) {
            subtotal += item.getItemTotal();
        }
        
        // Calculate VAT
        double vat = subtotal * VAT_RATE;
        
        // Calculate discount
        double discount = 0.0;
        // TODO: Apply real discount logic (coupons, loyalty, etc.)
        if (subtotal >= FREE_DELIVERY_THRESHOLD) {
            // Example: 5% discount for orders over 200₺
            discount = subtotal * 0.05;
        }
        
        // Calculate final total
        double total = subtotal + vat - discount;
        
        // Update labels
        subtotalLabel.setText(String.format("%.2f ₺", subtotal));
        vatLabel.setText(String.format("%.2f ₺", vat));
        discountLabel.setText(String.format("%.2f ₺", discount));
        totalLabel.setText(String.format("%.2f ₺", total));
    }
    
    /**
     * Handle Remove Item Button
     */
    @FXML
    private void handleRemove(ActionEvent event) {
        CartItem selected = cartTable.getSelectionModel().getSelectedItem();
        
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", 
                     "Please select an item to remove!");
            return;
        }
        
        // Confirm removal
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Remove Item");
        confirm.setHeaderText("Remove from cart?");
        confirm.setContentText("Remove " + selected.getProductName() + " from cart?");
        
        if (confirm.showAndWait().get() == ButtonType.OK) {
            // Remove item
            cartItems.remove(selected);
            
            // Update totals
            updateTotals();
            
            showAlert(Alert.AlertType.INFORMATION, "Removed", 
                     "Item removed from cart!");
        }
    }
    
    /**
     * Handle Checkout Button
     */
    @FXML
    private void handleCheckout(ActionEvent event) {
        // Check if cart is empty
        if (cartItems.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Empty Cart", 
                     "Your cart is empty!\n\nPlease add items before checkout.");
            return;
        }
        
        // Get total
        double total = Double.parseDouble(
            totalLabel.getText().replace("₺", "").trim()
        );
        
        // Show checkout info
        String deliveryInfo = "";
        if (total >= FREE_DELIVERY_THRESHOLD) {
            deliveryInfo = "\n🎉 Free delivery applied!";
        }
        
        showAlert(Alert.AlertType.INFORMATION, "Checkout", 
                 "📦 Order Summary\n\n" +
                 "Items: " + cartItems.size() + "\n" +
                 "Total: " + totalLabel.getText() + "\n" +
                 deliveryInfo + "\n\n" +
                 "Features coming soon:\n" +
                 "• Delivery date selection\n" +
                 "• Payment processing\n" +
                 "• PDF invoice generation\n" +
                 "• Order confirmation email");
        
        // TODO: Open delivery date selection dialog
        // TODO: Process payment
        // TODO: Generate invoice
        // TODO: Save order to database
        // TODO: Update stock
        // TODO: Close cart window
    }
    
    /**
     * Handle Continue Shopping Button
     */
    @FXML
    private void handleContinueShopping(ActionEvent event) {
        // Close cart window
        Stage stage = (Stage) continueShoppingButton.getScene().getWindow();
        stage.close();
    }
    
    /**
     * Show alert dialog
     */
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    // ==================== INNER CLASS: CART ITEM ====================
    
    /**
     * Cart Item Model
     * Represents a single item in the shopping cart
     */
    public static class CartItem {
        private String productName;
        private double quantity;
        private double unitPrice;
        
        /**
         * Constructor
         */
        public CartItem(String productName, double quantity, double unitPrice) {
            this.productName = productName;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
        }
        
        // Getters
        public String getProductName() { 
            return productName; 
        }
        
        public double getQuantity() { 
            return quantity; 
        }
        
        public double getUnitPrice() { 
            return unitPrice; 
        }
        
        public double getItemTotal() { 
            return quantity * unitPrice; 
        }
        
        // Setters
        public void setProductName(String productName) { 
            this.productName = productName; 
        }
        
        public void setQuantity(double quantity) { 
            this.quantity = quantity; 
        }
        
        public void setUnitPrice(double unitPrice) { 
            this.unitPrice = unitPrice; 
        }
    }
}