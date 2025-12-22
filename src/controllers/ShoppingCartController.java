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
 * Displays cart items and handles checkout
 * 
 * @author Group04
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
    private static final double VAT_RATE = 0.18; // 18% KDV
    
    /**
     * Initialize - Called automatically
     */
    @FXML
    private void initialize() {
        // Setup table columns
        productColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        totalColumn.setCellValueFactory(new PropertyValueFactory<>("itemTotal"));
        
        // Sample data (normally comes from a service)
        cartItems = FXCollections.observableArrayList();
        cartTable.setItems(cartItems);
        
        // Load sample items for demo
        loadSampleItems();
        
        // Calculate totals
        updateTotals();
    }
    
    /**
     * Load sample cart items (for demo purposes)
     */
    private void loadSampleItems() {
        cartItems.add(new CartItem("Tomato", 2.0, 20.50));
        cartItems.add(new CartItem("Apple", 1.5, 24.50));
        cartItems.add(new CartItem("Potato", 3.0, 15.80));
    }
    
    /**
     * Calculate and update totals
     */
    private void updateTotals() {
        double subtotal = 0.0;
        
        for (CartItem item : cartItems) {
            subtotal += item.getItemTotal();
        }
        
        double vat = subtotal * VAT_RATE;
        double discount = 0.0; // TODO: Apply real discounts
        double total = subtotal + vat - discount;
        
        subtotalLabel.setText(String.format("%.2f ₺", subtotal));
        vatLabel.setText(String.format("%.2f ₺", vat));
        discountLabel.setText(String.format("%.2f ₺", discount));
        totalLabel.setText(String.format("%.2f ₺", total));
    }
    
    /**
     * Remove selected item from cart
     */
    @FXML
    private void handleRemove(ActionEvent event) {
        CartItem selected = cartTable.getSelectionModel().getSelectedItem();
        
        if (selected != null) {
            cartItems.remove(selected);
            updateTotals();
            showAlert(Alert.AlertType.INFORMATION, "Removed", 
                     "Item removed from cart!");
        } else {
            showAlert(Alert.AlertType.WARNING, "No Selection", 
                     "Please select an item to remove!");
        }
    }
    
    /**
     * Checkout handler
     */
    @FXML
    private void handleCheckout(ActionEvent event) {
        if (cartItems.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Empty Cart", 
                     "Your cart is empty!");
            return;
        }
        
        // TODO: Open delivery date selection dialog
        // TODO: Process payment
        // TODO: Generate invoice
        
        showAlert(Alert.AlertType.INFORMATION, "Success", 
                 "Order placed successfully!\n(Feature in development)");
    }
    
    /**
     * Continue shopping - close cart window
     */
    @FXML
    private void handleContinueShopping(ActionEvent event) {
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
    
    /**
     * Inner class for cart items
     */
    public static class CartItem {
        private String productName;
        private double quantity;
        private double unitPrice;
        
        public CartItem(String productName, double quantity, double unitPrice) {
            this.productName = productName;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
        }
        
        public String getProductName() { return productName; }
        public double getQuantity() { return quantity; }
        public double getUnitPrice() { return unitPrice; }
        public double getItemTotal() { return quantity * unitPrice; }
    }
}