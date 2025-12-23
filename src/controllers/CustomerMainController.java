package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.Product;
import models.User;
import services.ProductService;

import java.util.List;

/**
 * Customer Main Controller
 * Uses TitledPane and ListView as required by project
 */
public class CustomerMainController {
    
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
        loadProducts();
    }
    
    /**
     * Set current user (called from Login)
     */
    public void setUser(User user) {
        this.currentUser = user;
        usernameLabel.setText("Welcome, " + user.getUsername());
    }
    
    /**
     * Load products from service (sorted by name)
     */
    private void loadProducts() {
        // Get vegetables
        List<Product> vegList = ProductService.getVegetables();
        vegList.sort((p1, p2) -> p1.getName().compareTo(p2.getName())); // Sort A-Z
        vegetables = FXCollections.observableArrayList(vegList);
        vegetableList.setItems(vegetables);
        
        // Get fruits
        List<Product> fruitList = ProductService.getFruits();
        fruitList.sort((p1, p2) -> p1.getName().compareTo(p2.getName())); // Sort A-Z
        fruits = FXCollections.observableArrayList(fruitList);
        this.fruitList.setItems(fruits);
    }
    
    /**
     * Search button handler
     */
    @FXML
    private void handleSearch(ActionEvent event) {
        String keyword = searchField.getText().trim();
        
        if (keyword.isEmpty()) {
            loadProducts(); // Show all if empty
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
    }
    
    /**
     * Add to cart button handler
     */
    @FXML
    private void handleAddToCart(ActionEvent event) {
        // Get selected product
        Product selectedProduct = getSelectedProduct();
        
        if (selectedProduct == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", 
                     "Please select a product first!");
            return;
        }
        
        // Validate quantity
        String quantityText = quantityField.getText().trim();
        
        if (quantityText.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Empty Quantity", 
                     "Please enter quantity in kg!");
            return;
        }
        
        try {
            double quantity = Double.parseDouble(quantityText);
            
            // Check if positive
            if (quantity <= 0) {
                showAlert(Alert.AlertType.ERROR, "Invalid Quantity", 
                         "Quantity must be positive!");
                return;
            }
            
            // Check stock
            if (quantity > selectedProduct.getStock()) {
                showAlert(Alert.AlertType.WARNING, "Insufficient Stock", 
                         String.format("Only %.1f kg available!", 
                                     selectedProduct.getStock()));
                return;
            }
            
            // Success
            showAlert(Alert.AlertType.INFORMATION, "Added to Cart", 
                     String.format("Added %.2f kg of %s to cart!\nPrice: $%.2f", 
                                 quantity, 
                                 selectedProduct.getName(),
                                 selectedProduct.getCurrentPrice() * quantity));
            
            quantityField.clear();
            
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", 
                     "Please enter a valid number!");
        }
    }
    
    /**
     * Get selected product from both ListViews
     */
    private Product getSelectedProduct() {
        Product selected = vegetableList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            selected = fruitList.getSelectionModel().getSelectedItem();
        }
        return selected;
    }
    
    /**
     * View cart handler
     */
    @FXML
    private void handleViewCart(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/ShoppingCart.fxml")
            );
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.setScene(new Scene(root, 960, 540));
            stage.setTitle("Shopping Cart");
            stage.show();
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", 
                     "Could not open shopping cart!");
        }
    }
    
    /**
     * View orders handler
     */
    @FXML
    private void handleViewOrders(ActionEvent event) {
        showAlert(Alert.AlertType.INFORMATION, "Orders", 
                 "Order history feature coming soon!");
    }
    
    /**
     * Profile handler
     */
    @FXML
    private void handleProfile(ActionEvent event) {
        showAlert(Alert.AlertType.INFORMATION, "Profile", 
                 "Profile feature coming soon!");
    }
    
    /**
     * Messages handler
     */
    @FXML
    private void handleMessages(ActionEvent event) {
        showAlert(Alert.AlertType.INFORMATION, "Messages", 
                 "Messages feature coming soon!");
    }
    
    /**
     * Logout handler
     */
    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            // Load login screen
            Parent root = FXMLLoader.load(
                getClass().getResource("/fxml/Login.fxml")
            );
            
            Stage currentStage = (Stage) logoutButton.getScene().getWindow();
            currentStage.setScene(new Scene(root, 960, 540));
            currentStage.setTitle("GreenGrocer - Login");
            
        } catch (Exception e) {
            e.printStackTrace();
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