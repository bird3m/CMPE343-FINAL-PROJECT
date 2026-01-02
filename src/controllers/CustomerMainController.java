package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import models.Product;
import models.User;
import services.ProductDAO;
import services.CartService;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Customer Main Controller with Product Cards
 * 
 * @author Group04
 * @version 1.0 - Card Layout
 */
public class CustomerMainController {
    
    @FXML private BorderPane rootPane;
    @FXML private Label usernameLabel;
    @FXML private TextField searchField;
    @FXML private TitledPane vegetablePane;
    @FXML private TitledPane fruitPane;
    @FXML private TilePane vegetableTilePane;  
    @FXML private TilePane fruitTilePane;      
    @FXML private TextField quantityField;
    @FXML private Button addToCartButton;
    @FXML private Button searchButton;
    @FXML private Button viewCartButton;
    @FXML private Button viewOrdersButton;
    @FXML private Button profileButton;
    @FXML private Button messagesButton;
    @FXML private Button logoutButton;
    
    private User currentUser;
    private List<Product> masterVegetables = new ArrayList<>();
    private List<Product> masterFruits = new ArrayList<>();
    private ProductDAO productDAO;
    
    private Product selectedProduct = null; 
    private VBox selectedCard = null;        
    
    @FXML
    private void initialize() {
        productDAO = new ProductDAO();
        loadProducts();
        
        // Dynamic search
        searchField.textProperty().addListener((obs, oldText, newText) -> {
            filterProducts(newText == null ? "" : newText.trim());
        });
    }
    
    public void setUser(User user) {
        this.currentUser = user;
        usernameLabel.setText("Welcome, " + user.getUsername());
    }
    
    /**
     * Create product card with image
     */
    private VBox createProductCard(Product product) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPrefSize(180, 240);
        card.getStyleClass().add("product-card");
        card.setPadding(new Insets(15));
        
        // Image container
        VBox imageContainer = new VBox();
        imageContainer.setAlignment(Pos.CENTER);
        imageContainer.setPrefSize(150, 110);
        imageContainer.setStyle(
            "-fx-background-color: linear-gradient(from 0% 100% to 100% 0%, #ffecd2 0%, #fcb69f 100%); " +
            "-fx-background-radius: 12; " +
            "-fx-border-color: #667eea; " +
            "-fx-border-width: 2; " +
            "-fx-border-radius: 12;"
        );
        
        ImageView imageView = new ImageView();
        imageView.setFitHeight(90);
        imageView.setFitWidth(90);
        imageView.setPreserveRatio(true);
        
        if (product.getImage() != null && product.getImage().length > 0) {
            imageView.setImage(new Image(new ByteArrayInputStream(product.getImage())));
        }
        imageContainer.getChildren().add(imageView);
        
        // Product name
        Label nameLabel = new Label(product.getName());
        nameLabel.setFont(Font.font("Arial Black", 14));
        nameLabel.setWrapText(true);
        nameLabel.setAlignment(Pos.CENTER);
        nameLabel.setStyle("-fx-text-fill: #2c3e50;");
        
        // Price container
        HBox priceBox = new HBox(5);
        priceBox.setAlignment(Pos.CENTER);
        
        Label priceIcon = new Label("💰");
        Label priceLabel = new Label(String.format("%.2f₺/kg", product.getCurrentPrice()));
        priceLabel.setFont(Font.font("Arial Black", 15));
        priceLabel.setStyle("-fx-text-fill: #27ae60;");
        priceBox.getChildren().addAll(priceIcon, priceLabel);
        
        // Stock info
        HBox stockBox = new HBox(5);
        stockBox.setAlignment(Pos.CENTER);
        stockBox.setStyle(
            "-fx-background-color: rgba(52, 152, 219, 0.15); " +
            "-fx-background-radius: 8; " +
            "-fx-padding: 5 10 5 10;"
        );
        
        Label stockIcon = new Label("📦");
        Label stockLabel = new Label(String.format("Stock: %.1f kg", product.getStock()));
        stockLabel.setFont(Font.font("Arial Bold", 11));
        stockLabel.setStyle("-fx-text-fill: #3498db;");
        stockBox.getChildren().addAll(stockIcon, stockLabel);
        
        // Threshold warning
        HBox warningBox = new HBox(5);
        warningBox.setAlignment(Pos.CENTER);
        warningBox.setStyle(
            "-fx-background-color: rgba(231, 76, 60, 0.15); " +
            "-fx-background-radius: 8; " +
            "-fx-padding: 4 8 4 8;"
        );
        
        Label warningLabel = new Label("⚠️ Low Stock! 2x Price");
        warningLabel.setFont(Font.font("Arial Bold", 10));
        warningLabel.setStyle("-fx-text-fill: #e74c3c;");
        warningBox.getChildren().add(warningLabel);
        warningBox.setVisible(product.getStock() <= product.getThreshold());
        
        VBox infoBox = new VBox(5);
        infoBox.setAlignment(Pos.CENTER);
        infoBox.getChildren().addAll(priceBox, stockBox, warningBox);
        
        card.getChildren().addAll(imageContainer, nameLabel, infoBox);
        
        card.setOnMouseClicked(e -> selectCard(card, product));
        
        return card;
    }
    
    /**
     *Select card and highlight
     */
    private void selectCard(VBox card, Product product) {
        // Remove previous selection
        if (selectedCard != null) {
            selectedCard.setStyle(
                selectedCard.getStyle().replace(
                    "-fx-border-color: #27ae60; -fx-border-width: 4;",
                    ""
                )
            );
        }
        
        // Highlight new selection
        card.setStyle(card.getStyle() + "-fx-border-color: #27ae60; -fx-border-width: 4;");
        selectedCard = card;
        selectedProduct = product;
    }
    
    /**
     * Load products and create cards
     */
    private void loadProducts() {
        List<Product> allProducts = productDAO.getAllProducts();
        
        masterVegetables.clear();
        masterFruits.clear();
        
        for (Product p : allProducts) {
            if (p.getStock() > 0) {
                if ("vegetable".equals(p.getType())) {
                    masterVegetables.add(p);
                } else if ("fruit".equals(p.getType())) {
                    masterFruits.add(p);
                }
            }
        }
        
        masterVegetables.sort((p1, p2) -> p1.getName().compareTo(p2.getName()));
        masterFruits.sort((p1, p2) -> p1.getName().compareTo(p2.getName()));
        
        displayProducts();
    }
    
    /**
     * ✅ YENİ: Display products as cards
     */
    private void displayProducts() {
        vegetableTilePane.getChildren().clear();
        fruitTilePane.getChildren().clear();
        
        for (Product p : masterVegetables) {
            vegetableTilePane.getChildren().add(createProductCard(p));
        }
        
        for (Product p : masterFruits) {
            fruitTilePane.getChildren().add(createProductCard(p));
        }
    }
    
    @FXML
    private void handleSearch(ActionEvent event) {
        filterProducts(searchField.getText().trim());
    }
    
    private void filterProducts(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            displayProducts();
            return;
        }
        
        String lower = keyword.toLowerCase();
        
        vegetableTilePane.getChildren().clear();
        fruitTilePane.getChildren().clear();
        
        for (Product p : masterVegetables) {
            if (p.getName().toLowerCase().contains(lower)) {
                vegetableTilePane.getChildren().add(createProductCard(p));
            }
        }
        
        for (Product p : masterFruits) {
            if (p.getName().toLowerCase().contains(lower)) {
                fruitTilePane.getChildren().add(createProductCard(p));
            }
        }
    }
    
    @FXML
    private void handleAddToCart(ActionEvent event) {
        if (selectedProduct == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a product first!");
            return;
        }
        
        String quantityText = quantityField.getText().trim();
        if (quantityText.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Empty Quantity", "Please enter quantity in kg!");
            return;
        }
        
        try {
            double quantity = Double.parseDouble(quantityText);
            
            if (quantity <= 0) {
                showAlert(Alert.AlertType.ERROR, "Invalid Quantity", "Quantity must be greater than zero!");
                return;
            }
            
            if (quantity > selectedProduct.getStock()) {
                showAlert(Alert.AlertType.WARNING, "Insufficient Stock", 
                    String.format("Only %.2f kg available for %s!", selectedProduct.getStock(), selectedProduct.getName()));
                return;
            }
            
            double addedTotal = CartService.addToCart(selectedProduct, quantity);
            
            showAlert(Alert.AlertType.INFORMATION, "Added to Cart",
                String.format("Successfully added!\n\nProduct: %s\nQuantity: %.2f kg\nTotal: %.2f₺",
                    selectedProduct.getName(), quantity, addedTotal));
            
            quantityField.clear();
            
            // Deselect card
            if (selectedCard != null) {
                selectedCard.setStyle(selectedCard.getStyle().replace("-fx-border-color: #27ae60; -fx-border-width: 4;", ""));
            }
            selectedProduct = null;
            selectedCard = null;
            
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please enter a valid number!");
        }
    }
    
    @FXML
    private void handleViewCart(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ShoppingCart.fxml"));
            Parent root = loader.load();
            ShoppingCartController cartController = loader.getController();
            cartController.setUser(currentUser);
            
            Stage cartStage = new Stage();
            Scene scene = new Scene(root, 960, 540);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            cartStage.setScene(scene);
            cartStage.setTitle("Shopping Cart");
            cartStage.centerOnScreen();
            cartStage.setOnHidden(e -> loadProducts());
            cartStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Could not open shopping cart!");
        }
    }
    
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
            showAlert(Alert.AlertType.ERROR, "Error", "Could not open orders screen!");
        }
    }
    
    @FXML
    private void handleProfile(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/EditProfile.fxml"));
            Parent root = loader.load();
            EditProfileController controller = loader.getController();
            controller.setUser(currentUser);
            Stage profileStage = new Stage();
            Scene scene = new Scene(root, 600, 500);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            profileStage.setScene(scene);
            profileStage.setTitle("Edit Profile");
            profileStage.centerOnScreen();
            profileStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Could not open profile screen!");
        }
    }
    
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
            showAlert(Alert.AlertType.ERROR, "Error", "Could not open chat!");
        }
    }
    
    @FXML
    private void handleLogout(ActionEvent event) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Logout");
        confirm.setHeaderText("Are you sure?");
        confirm.setContentText("Do you want to logout?");
        
        if (confirm.showAndWait().get() == ButtonType.OK) {
            try {
                Parent root = FXMLLoader.load(getClass().getResource("/fxml/Login.fxml"));
                Scene scene = new Scene(root, 960, 540);
                scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
                Stage currentStage = (Stage) logoutButton.getScene().getWindow();
                currentStage.setScene(scene);
                currentStage.setTitle("Group04 GreenGrocer - Login");
                currentStage.centerOnScreen();
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "Could not logout!");
            }
        }
    }
    
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}