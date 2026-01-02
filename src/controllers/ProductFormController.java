package controllers;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import models.Product;
import services.ProductDAO;
import utils.InputValidation;

import java.io.ByteArrayInputStream;
import java.io.File;

/**
 * Controller for Add/Edit Product Form.
 * Handles user input, image selection, and saving to database.
 */
public class ProductFormController {

    // --- FXML Components ---
    @FXML private TextField nameField;
    @FXML private ComboBox<String> typeComboBox; // vegetable, fruit
    @FXML private TextField priceField;
    @FXML private TextField stockField;
    @FXML private TextField thresholdField;
    @FXML private ImageView productImageView;
    @FXML private Label errorLabel;
    @FXML private Button saveButton;

    // --- Data ---
    private ProductDAO productDAO;
    private Product currentProduct;
    private File selectedImageFile;
    private boolean isEditMode = false;

    @FXML
    private void initialize() {
        productDAO = new ProductDAO();
        
        // ComboBox options
        typeComboBox.setItems(FXCollections.observableArrayList("vegetable", "fruit"));
        typeComboBox.getSelectionModel().selectFirst();
        
        if (errorLabel != null) errorLabel.setVisible(false);
    }

    /**
     * Sets the product for EDIT mode.
     * Called from OwnerController when "Edit" is clicked.
     */
    public void setProduct(Product product) {
        this.currentProduct = product;
        this.isEditMode = true;
        
        // Populate form fields
        nameField.setText(product.getName());
        typeComboBox.setValue(product.getType());
        priceField.setText(String.valueOf(product.getPrice()));
        stockField.setText(String.valueOf(product.getStock()));
        thresholdField.setText(String.valueOf(product.getThreshold()));
        
           // If product already has an image, display it
        if (product.getImage() != null && product.getImage().length > 0) {
             ByteArrayInputStream bis = new ByteArrayInputStream(product.getImage());
             productImageView.setImage(new Image(bis));
        }
        
        saveButton.setText("Update Product");
    }

    /**
     * Handle "Choose Image" button.
     * Opens a FileChooser to select PNG/JPG.
     */
    @FXML
    private void handleChooseImage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Product Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        
        Stage stage = (Stage) nameField.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);
        
        if (file != null) {
            this.selectedImageFile = file;
            // Show preview
            productImageView.setImage(new Image(file.toURI().toString()));
        }
    }

    @FXML
    private void handleSave(ActionEvent event) {
        // 1. Collect input values
        String name = nameField.getText().trim();
        String type = typeComboBox.getValue();
        String priceStr = priceField.getText().trim();
        String stockStr = stockField.getText().trim();
        String thresholdStr = thresholdField.getText().trim();

        // 2. Validation using InputValidation
        String err;
        err = InputValidation.validateProductName(name);
        if (err != null) { showError(err); return; }

        err = InputValidation.validatePrice(priceStr);
        if (err != null) { showError(err); return; }

        err = InputValidation.validateNonNegativeNumber(stockStr, "Stock");
        if (err != null) { showError(err); return; }

        err = InputValidation.validatePositiveNumber(thresholdStr, "Threshold");
        if (err != null) { showError(err); return; }

        try {
            // 3. Numeric conversion
            double price = Double.parseDouble(priceStr.replace(",", "."));
            double stock = Double.parseDouble(stockStr.replace(",", "."));
            double threshold = Double.parseDouble(thresholdStr.replace(",", "."));

            boolean success;
            if (isEditMode) {
                // UPDATE
                currentProduct.setName(name);
                currentProduct.setType(type);
                currentProduct.setPrice(price);
                currentProduct.setStock(stock);
                currentProduct.setThreshold(threshold);
                // Note: do not set image here; it will be refreshed when reloaded from DB
                
                success = productDAO.updateProduct(currentProduct, selectedImageFile);
            } else {
                // INSERT (new product)
                // Use explicit (byte[]) null for the image parameter to select the correct constructor
                Product newProduct = new Product(0, name, type, price, stock, threshold, (byte[]) null);
                
                success = productDAO.addProduct(newProduct, selectedImageFile);
            }

            if (success) {
                // Pencereyi kapat
                Stage stage = (Stage) saveButton.getScene().getWindow();
                stage.close();
            } else {
                showError("Database error! Check logs.");
            }

        } catch (NumberFormatException e) {
            showError("Price, Stock, and Threshold must be valid numbers (e.g. 10.50)");
        }
    }
    
    private void showError(String msg) {
        if (errorLabel != null) {
            errorLabel.setText(msg);
            errorLabel.setVisible(true);
        } else {
            // Fallback Alert
            Alert alert = new Alert(Alert.AlertType.ERROR, msg);
            alert.show();
        }
    }
}