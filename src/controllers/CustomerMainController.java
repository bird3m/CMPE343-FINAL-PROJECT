package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import models.Product;
import models.User;
import services.ProductDAO;
import services.CartService;

import java.util.ArrayList;
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
    private ProductDAO productDAO; 
    
    /**
     * Initialize - Called automatically after FXML is loaded
     */
    @FXML
    private void initialize() {
        productDAO = new ProductDAO();
        
        // Load products from database
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
    /**
     * Setup ListView cell factories with images
    */
    private void setupListViews() {
    // Custom cell factory to show product info WITH IMAGE
    vegetableList.setCellFactory(lv -> new ListCell<Product>() {
        private javafx.scene.image.ImageView imageView = new javafx.scene.image.ImageView();
        {
            imageView.setFitHeight(50);
            imageView.setFitWidth(50);
            imageView.setPreserveRatio(true);
        }
        
        @Override
        protected void updateItem(Product item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                setGraphic(null);
            } else {
                setText(item.toString());
                if (item.getImage() != null && item.getImage().length > 0) {
                    imageView.setImage(new javafx.scene.image.Image(
                        new java.io.ByteArrayInputStream(item.getImage())
                    ));
                    setGraphic(imageView);
                } else {
                    setGraphic(null);
                }
            }
        }
    });
    
    fruitList.setCellFactory(lv -> new ListCell<Product>() {
        private javafx.scene.image.ImageView imageView = new javafx.scene.image.ImageView();
        {
            imageView.setFitHeight(50);
            imageView.setFitWidth(50);
            imageView.setPreserveRatio(true);
        }
        
        @Override
        protected void updateItem(Product item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                setGraphic(null);
            } else {
                setText(item.toString());
                if (item.getImage() != null && item.getImage().length > 0) {
                    imageView.setImage(new javafx.scene.image.Image(
                        new java.io.ByteArrayInputStream(item.getImage())
                    ));
                    setGraphic(imageView);
                } else {
                    setGraphic(null);
                }
            }
        }
    });
}
    
    /**
     * Load products from DATABASE and sort by name
     * Only shows products with stock > 0
     */
    private void loadProducts() {
        List<Product> allProducts = productDAO.getAllProducts();
        
        List<Product> vegList = new ArrayList<>();
        List<Product> fruitList = new ArrayList<>();
        
        for (Product p : allProducts) {
            // Only add products with available stock
            if (p.getStock() > 0) {
                if ("vegetable".equals(p.getType())) {
                    vegList.add(p);
                } else if ("fruit".equals(p.getType())) {
                    fruitList.add(p);
                }
            }
        }
        
        vegList.sort((p1, p2) -> p1.getName().compareTo(p2.getName()));
        fruitList.sort((p1, p2) -> p1.getName().compareTo(p2.getName()));
        
        vegetables = FXCollections.observableArrayList(vegList);
        vegetableList.setItems(vegetables);
        
        fruits = FXCollections.observableArrayList(fruitList);
        this.fruitList.setItems(fruits);
    }
    
    /**
     * Handle Search Button
     * Only shows products with stock > 0
     */
    @FXML
    private void handleSearch(ActionEvent event) {
        String keyword = searchField.getText().trim();
        
        if (keyword.isEmpty()) {
            // Show all products if search is empty
            loadProducts();
            return;
        }
        
        List<Product> allProducts = productDAO.getAllProducts();
        
        List<Product> vegResults = new ArrayList<>();
        List<Product> fruitResults = new ArrayList<>();
        
        String lowerKeyword = keyword.toLowerCase();
        
        for (Product p : allProducts) {
            // Filter by name AND stock availability
            if (p.getName().toLowerCase().contains(lowerKeyword) && p.getStock() > 0) {
                if ("vegetable".equals(p.getType())) {
                    vegResults.add(p);
                } else if ("fruit".equals(p.getType())) {
                    fruitResults.add(p);
                }
            }
        }

        vegResults.sort((p1, p2) -> p1.getName().compareTo(p2.getName()));
        fruitResults.sort((p1, p2) -> p1.getName().compareTo(p2.getName()));
        
        vegetableList.setItems(FXCollections.observableArrayList(vegResults));
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
            
            // Calculate split pricing: units above threshold at normal price,
            // units that reduce stock to <= threshold are doubled.
            double stock = selectedProduct.getStock();
            double threshold = selectedProduct.getThreshold();
            double basePrice = selectedProduct.getPrice();

            double normalQty = 0.0;
            if (stock > threshold) {
                normalQty = Math.max(0.0, Math.min(quantity, stock - threshold));
            }
            double doubledQty = quantity - normalQty;

            double normalTotal = normalQty * basePrice;
            double doubledTotal = doubledQty * basePrice * 2.0;
            double totalPrice = normalTotal + doubledTotal;
            double avgUnitPrice = totalPrice / quantity;

            String thresholdWarning = "";
            if (doubledQty > 0) {
                thresholdWarning = String.format("\n\nNote: %.2f kg charged at doubled price.", doubledQty);
            } else if (stock <= threshold) {
                thresholdWarning = "\n\nLow stock! Price is doubled for this product.";
            }

            // Add to cart (CartService now returns added total)
            double addedTotal = CartService.addToCart(selectedProduct, quantity);

            // Success - show confirmation with breakdown
            showAlert(Alert.AlertType.INFORMATION, "Added to Cart",
                     String.format(
                         "Successfully added to cart!\n\n" +
                         "Product: %s\n" +
                         "Quantity: %.2f kg\n" +
                         "Unit Price (avg): %.2f₺/kg\n" +
                         "Total: %.2f₺" +
                         thresholdWarning,
                         selectedProduct.getName(),
                         quantity,
                         avgUnitPrice,
                         addedTotal
                     ));
            
            // Clear quantity field
            quantityField.clear();
            
            // Clear selection
            vegetableList.getSelectionModel().clearSelection();
            fruitList.getSelectionModel().clearSelection();
            
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
     * Refreshes products when cart window closes
     */
    @FXML
    private void handleViewCart(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/ShoppingCart.fxml")
            );
            Parent root = loader.load();

            ShoppingCartController cartController = loader.getController();
            cartController.setUser(currentUser); 
            
            // Create new stage for cart
            Stage cartStage = new Stage();
            Scene scene = new Scene(root, 960, 540);
            scene.getStylesheets().add(
                getClass().getResource("/css/style.css").toExternalForm()
            );
            
            cartStage.setScene(scene);
            cartStage.setTitle("Shopping Cart");
            cartStage.centerOnScreen();
            
            // Refresh products when cart window closes
            cartStage.setOnHidden(e -> {
                loadProducts(); // Update stock display
            });
            
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
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MyOrders.fxml"));
            Parent root = loader.load();

            MyOrdersController controller = loader.getController();
            controller.setCustomer(currentUser); 

            Stage stage = new Stage();
            stage.setTitle("My Orders History");
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
            stage.show();
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Could not open orders screen!\n" + e.getMessage());
        }
    }
    
    /**
     * Handle Profile Button
     * Opens profile editing window
     */
    @FXML
    private void handleProfile(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/EditProfile.fxml")
            );
            Parent root = loader.load();
            
            // Pass current user to profile controller
            EditProfileController controller = loader.getController();
            controller.setUser(currentUser);
            
            // Create new stage for profile
            Stage profileStage = new Stage();
            Scene scene = new Scene(root, 600, 500);
            scene.getStylesheets().add(
                getClass().getResource("/css/style.css").toExternalForm()
            );
            
            profileStage.setScene(scene);
            profileStage.setTitle("Edit Profile");
            profileStage.centerOnScreen();
            profileStage.show();
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", 
                      "Could not open profile screen!\n\n" + e.getMessage());
        }
    }
    
    /**
     * Handle Messages Button - open chat window
     */
    @FXML
    private void handleMessages(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Chat.fxml"));
            Parent root = loader.load();

            ChatController controller = loader.getController();
            controller.setUser(currentUser);

            Stage chatStage = new Stage();
            Scene scene = new Scene(root, 400, 600);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            chatStage.setScene(scene);
            chatStage.setTitle("Messages - Owner");
            chatStage.centerOnScreen();
            chatStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Could not open chat!\n" + e.getMessage());
        }
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