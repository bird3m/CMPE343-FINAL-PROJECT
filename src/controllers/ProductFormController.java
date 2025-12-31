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
import utils.InputValidation; // Validator'ımızı kullanıyoruz

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
    @FXML private ImageView productImageView; // Seçilen resmi göstermek için
    @FXML private Label errorLabel;
    @FXML private Button saveButton;

    // --- Data ---
    private ProductDAO productDAO;
    private Product currentProduct; // Eğer düzenleme yapıyorsak bu dolu olur
    private File selectedImageFile; // Bilgisayardan seçilen resim dosyası
    private boolean isEditMode = false;

    @FXML
    private void initialize() {
        productDAO = new ProductDAO();
        
        // ComboBox seçenekleri
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
        
        // Formu doldur
        nameField.setText(product.getName());
        typeComboBox.setValue(product.getType());
        priceField.setText(String.valueOf(product.getPrice()));
        stockField.setText(String.valueOf(product.getStock()));
        thresholdField.setText(String.valueOf(product.getThreshold()));
        
        // Varsa mevcut resmi göster (Image objesi Product modelinde olmalı)
        if (product.getImage() != null) {
             productImageView.setImage(product.getImage());
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
            // Önizleme göster
            productImageView.setImage(new Image(file.toURI().toString()));
        }
    }

    @FXML
    private void handleSave(ActionEvent event) {
        // 1. Verileri Al
        String name = nameField.getText().trim();
        String type = typeComboBox.getValue();
        String priceStr = priceField.getText().trim();
        String stockStr = stockField.getText().trim();
        String thresholdStr = thresholdField.getText().trim();

        // 2. Basit Validation (Boşluk Kontrolü)
        if (name.isEmpty() || priceStr.isEmpty() || stockStr.isEmpty() || thresholdStr.isEmpty()) {
            showError("Please fill all fields!");
            return;
        }

        try {
            // 3. Sayısal Dönüşüm
            double price = Double.parseDouble(priceStr);
            double stock = Double.parseDouble(stockStr);
            double threshold = Double.parseDouble(thresholdStr);

            if (price < 0 || stock < 0 || threshold < 0) {
                showError("Values cannot be negative!");
                return;
            }

            boolean success;
            if (isEditMode) {
                // UPDATE
                currentProduct.setName(name);
                currentProduct.setType(type);
                currentProduct.setPrice(price);
                currentProduct.setStock(stock);
                currentProduct.setThreshold(threshold);
                // Not: setImage yapmıyoruz çünkü veritabanından tekrar çekilince güncellenecek
                
                success = productDAO.updateProduct(currentProduct, selectedImageFile);
            } else {
                // INSERT (Yeni Ürün)
                // ID veritabanında oluşacak, geçici olarak 0 veriyoruz
                Product newProduct = new Product(0, name, price, stock, type, threshold, null);
                
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