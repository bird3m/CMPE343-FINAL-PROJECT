package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import models.Product;
import models.User;
import services.ProductService;

import java.util.List;

/**
 * Customer Main Controller
 * 
 * Features:
 * - Browse products (vegetables and fruits)
 * - Search products
 * - Add items to cart
 * - View cart, orders, profile
 * - Stock checking with threshold
 * 
 * @author Group04
 * @version 1.0
 */
public class CustomerMainController {
    
    @FXML private BorderPane rootPane;
    @FXML private Label usernameLabel;
    @FXML private TextField searchField;
    @FXML private TitledPane vegetablePane;
    @FXML private TitledPane fruitPane;
    @FXML private ListView<Product> vegetableList;
    @FXML private ListView<Product> fruitList;
    @FXML private TextField quantityField;
    @FXML private Button addToCartButton;
    @FXML private Button searchButton;
    @FXML private Button viewCartButton;
    @FXML private Button viewOrdersButton;
    @FXML private Button profileButton;
    @FXML private Button messagesButton;
    @FXML private Button logoutButton;
    
    private User currentUser;
    private ObservableList<Product> vegetables;
    private ObservableList<Product> fruits;
    
    /**
     * Initialize - Called automatically after FXML is loaded
     */
    @FXML
    private void initialize() {
        // Load products from service
        loadProducts();
        
        // Setup cell factory for custom display
        setupListViews();
    }
    
    /**
     * Set current user (called from Login)
     */
    public void setUser(User user) {
        this.currentUser = user;
        usernameLabel.setText("Welcome, " + user.getUsername());
    }
    
    /**
     * Setup ListView cell factories
     */
    private void setupListViews() {
        // Custom cell factory to show product info
        vegetableList.setCellFactory(lv -> new ListCell<Product>() {
            @Override
            protected void updateItem(Product item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.toString());
                }
            }
        });
        
        fruitList.setCellFactory(lv -> new ListCell<Product>() {
            @Override
            protected void updateItem(Product item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.toString());
                }
            }
        });
    }
    
    /**
     * Load products from service and sort by name
     */
    private void loadProducts() {
        // Get vegetables
        List<Product> vegList = ProductService.getVegetables();
        vegList.sort((p1, p2) -> p1.getName().compareTo(p2.getName()));
        vegetables = FXCollections.observableArrayList(vegList);
        vegetableList.setItems(vegetables);
        
        // Get fruits
        List<Product> fruitList = ProductService.getFruits();
        fruitList.sort((p1, p2) -> p1.getName().compareTo(p2.getName()));
        fruits = FXCollections.observableArrayList(fruitList);
        this.fruitList.setItems(fruits);
    }
    
    /**
     * Handle Search Button
     */
    @FXML
    private void handleSearch(ActionEvent event) {
        String keyword = searchField.getText().trim();
        
        if (keyword.isEmpty()) {
            // Show all products if search is empty
            loadProducts();
            return;
        }
        
        // Search in vegetables
        List<Product> vegResults = ProductService.searchProducts(
            keyword, 
            ProductService.getVegetables()
        );
        vegResults.sort((p1, p2) -> p1.getName().compareTo(p2.getName()));
        vegetableList.setItems(FXCollections.observableArrayList(vegResults));
        
        // Search in fruits
        List<Product> fruitResults = ProductService.searchProducts(
            keyword, 
            ProductService.getFruits()
        );
        fruitResults.sort((p1, p2) -> p1.getName().compareTo(p2.getName()));
        fruitList.setItems(FXCollections.observableArrayList(fruitResults));
        
        // Show result count
        int totalResults = vegResults.size() + fruitResults.size();
        if (totalResults == 0) {
            showAlert(Alert.AlertType.INFORMATION, "Search Results", 
                     "No products found for: " + keyword);
        } else {
            showAlert(Alert.AlertType.INFORMATION, "Search Results", 
                     "Found " + totalResults + " products matching: " + keyword);
        }
    }
    
    /**
     * Handle Add to Cart Button
     */
    @FXML
    private void handleAddToCart(ActionEvent event) {
        // Get selected product from either list
        Product selectedProduct = getSelectedProduct();
        
        if (selectedProduct == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", 
                     "Please select a product first!");
            return;
        }
        
        // Validate quantity input
        String quantityText = quantityField.getText().trim();
        
        if (quantityText.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Empty Quantity", 
                     "Please enter quantity in kg!");
            return;
        }
        
        try {
            double quantity = Double.parseDouble(quantityText);
            
            // Check if quantity is positive
            if (quantity <= 0) {
                showAlert(Alert.AlertType.ERROR, "Invalid Quantity", 
                         "Quantity must be greater than zero!");
                return;
            }
            
            // Check stock availability
            if (quantity > selectedProduct.getStock()) {
                showAlert(Alert.AlertType.WARNING, "Insufficient Stock", 
                         String.format(
                             "Only %.2f kg available for %s!\n\n" +
                             "Please reduce quantity.",
                             selectedProduct.getStock(),
                             selectedProduct.getName()
                         ));
                return;
            }
            
            // Calculate price
            double unitPrice = selectedProduct.getCurrentPrice();
            double totalPrice = unitPrice * quantity;
            
            // Check threshold warning
            String thresholdWarning = "";
            if (selectedProduct.getStock() <= selectedProduct.getThreshold()) {
                thresholdWarning = "\n\n⚠️ Low stock! Price is doubled!";
            }
            
            // Success - show confirmation
            showAlert(Alert.AlertType.INFORMATION, "Added to Cart", 
                     String.format(
                         "✅ Successfully added to cart!\n\n" +
                         "Product: %s\n" +
                         "Quantity: %.2f kg\n" +
                         "Unit Price: %.2f₺/kg\n" +
                         "Total: %.2f₺" +
                         thresholdWarning,
                         selectedProduct.getName(),
                         quantity,
                         unitPrice,
                         totalPrice
                     ));
            
            // Clear quantity field
            quantityField.clear();
            
            // Clear selection
            vegetableList.getSelectionModel().clearSelection();
            fruitList.getSelectionModel().clearSelection();
            
            // TODO: Actually add to cart (implement CartService)
            
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", 
                     "Please enter a valid number!\n\n" +
                     "Examples: 0.5, 1, 2.5, 3");
        }
    }
    
    /**
     * Get selected product from both lists
     */
    private Product getSelectedProduct() {
        Product selected = vegetableList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            selected = fruitList.getSelectionModel().getSelectedItem();
        }
        return selected;
    }
    
    /**
     * Handle View Cart Button
     */
    @FXML
    private void handleViewCart(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/ShoppingCart.fxml")
            );
            Parent root = loader.load();
            
            // Create new stage for cart
            Stage cartStage = new Stage();
            Scene scene = new Scene(root, 960, 540);
            scene.getStylesheets().add(
                getClass().getResource("/css/style.css").toExternalForm()
            );
            
            cartStage.setScene(scene);
            cartStage.setTitle("Shopping Cart");
            cartStage.centerOnScreen();
            cartStage.show();
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", 
                     "Could not open shopping cart!\n\n" + e.getMessage());
        }
    }
    
    /**
     * Handle View Orders Button
     */
    @FXML
    private void handleViewOrders(ActionEvent event) {
        showAlert(Alert.AlertType.INFORMATION, "My Orders", 
                 "📋 Order History\n\n" +
                 "This feature will show:\n" +
                 "• Past orders\n" +
                 "• Current orders\n" +
                 "• Delivery status\n" +
                 "• Order tracking\n\n" +
                 "Coming soon!");
    }
    
    /**
     * Handle Profile Button
     */
    @FXML
    private void handleProfile(ActionEvent event) {
        if (currentUser != null) {
            showAlert(Alert.AlertType.INFORMATION, "My Profile", 
                     "👤 Profile Information\n\n" +
                     "Username: " + currentUser.getUsername() + "\n" +
                     "Role: " + currentUser.getRole() + "\n\n" +
                     "Profile editing feature coming soon!");
        } else {
            showAlert(Alert.AlertType.INFORMATION, "My Profile", 
                     "Profile feature coming soon!");
        }
    }
    
    /**
     * Handle Messages Button
     */
    @FXML
    private void handleMessages(ActionEvent event) {
        showAlert(Alert.AlertType.INFORMATION, "Messages", 
                 "💬 Messaging System\n\n" +
                 "This feature will allow:\n" +
                 "• Message owner\n" +
                 "• Ask questions\n" +
                 "• Report issues\n" +
                 "• Get support\n\n" +
                 "Coming soon!");
    }
    
    /**
     * Handle Logout Button
     */
    @FXML
    private void handleLogout(ActionEvent event) {
        // Confirm logout
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Logout");
        confirm.setHeaderText("Are you sure?");
        confirm.setContentText("Do you want to logout?");
        
        if (confirm.showAndWait().get() == ButtonType.OK) {
            try {
                // Load login screen
                Parent root = FXMLLoader.load(
                    getClass().getResource("/fxml/Login.fxml")
                );
                
                Scene scene = new Scene(root, 960, 540);
                scene.getStylesheets().add(
                    getClass().getResource("/css/style.css").toExternalForm()
                );
                
                Stage currentStage = (Stage) logoutButton.getScene().getWindow();
                currentStage.setScene(scene);
                currentStage.setTitle("Group04 GreenGrocer - Login");
                currentStage.centerOnScreen();
                
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", 
                         "Could not logout!\n\n" + e.getMessage());
            }
        }
    }
    
    /**
     * Helper method to show alerts
     */
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}