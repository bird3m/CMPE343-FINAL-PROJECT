package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import models.Order;
import models.OrderItem;
import models.User;
import services.CartService;
import services.OrderDAO;

import java.time.LocalDateTime;
import java.util.List;

public class ShoppingCartController {

    // --- FXML BİLEŞENLERİ (FXML'deki fx:id'lerle birebir aynı olmalı) ---
    @FXML private TableView<OrderItem> cartTable;
    @FXML private TableColumn<OrderItem, String> productColumn;
    @FXML private TableColumn<OrderItem, Double> quantityColumn;
    @FXML private TableColumn<OrderItem, Double> priceColumn;
    @FXML private TableColumn<OrderItem, Double> totalColumn;

    @FXML private Label subtotalLabel;
    @FXML private Label vatLabel;
    @FXML private Label discountLabel;
    @FXML private Label totalLabel;

    @FXML private Button checkoutButton;
    @FXML private Button removeButton;
    @FXML private Button continueShoppingButton;

    private User currentUser; 

    @FXML
    private void initialize() {
        setupTable();
        refreshCart();
    }

    public void setUser(User user) {
        this.currentUser = user;
    }

    // Tablo sütunlarını Model ile eşleştiriyoruz
    private void setupTable() {
        productColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("pricePerUnit"));
        // OrderItem içinde getTotalPrice() metodu olduğu için "totalPrice" yazıyoruz
        totalColumn.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));

        // Hücre formatları (₺ ve kg eklemek için)
        quantityColumn.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("%.2f kg", item));
            }
        });

        priceColumn.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("%.2f ₺", item));
            }
        });

        totalColumn.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("%.2f ₺", item));
            }
        });
    }

    private void refreshCart() {
        // Sepetteki ürünleri çek
        List<OrderItem> items = CartService.getCartItems();
        ObservableList<OrderItem> observableItems = FXCollections.observableArrayList(items);
        cartTable.setItems(observableItems);
        
        // Hesaplamalar
        double subtotal = CartService.getTotal();
        double vatRate = 0.18; // %18 KDV
        double vat = subtotal * vatRate;
        double discount = 0.0; // İstersen indirim mantığı ekleyebilirsin
        
        // 200 TL üzeri kargo bedava gibi bir indirim eklenebilir
        if (subtotal > 200) {
             discount = subtotal * 0.05; // %5 indirim
        }

        double finalTotal = subtotal + vat - discount;

        // Etiketleri güncelle
        subtotalLabel.setText(String.format("%.2f ₺", subtotal));
        vatLabel.setText(String.format("%.2f ₺", vat));
        discountLabel.setText(String.format("-%.2f ₺", discount));
        totalLabel.setText(String.format("%.2f ₺", finalTotal));

        // Sepet boşsa checkout'u kapat
        checkoutButton.setDisable(items.isEmpty());
    }

    // --- BUTON AKSİYONLARI ---

    @FXML
    private void handleRemove(ActionEvent event) {
        OrderItem selected = cartTable.getSelectionModel().getSelectedItem();
        
        if (selected != null) {
            // Service'den ve tablodan sil
            CartService.getCartItems().remove(selected);
            refreshCart(); // Ekranı güncelle
        } else {
            showAlert("Lütfen silinecek ürünü seçin.");
        }
    }
    
    // İŞTE EKSİK OLAN METOT BUYDU!
    @FXML
    private void handleContinueShopping(ActionEvent event) {
        // Pencereyi kapatır, alışverişe devam edersin
        ((Stage) continueShoppingButton.getScene().getWindow()).close();
    }

    @FXML
    private void handleCheckout(ActionEvent event) {
        if (CartService.getCartItems().isEmpty()) {
            showAlert("Sepet boş!");
            return;
        }
        
        if (currentUser == null) {
            showAlert("Kullanıcı hatası! Lütfen tekrar giriş yapın.");
            return;
        }

        OrderDAO orderDAO = new OrderDAO();
        // Faturayı hazırla
        double finalTotal = Double.parseDouble(totalLabel.getText().replace(" ₺", "").replace(",", "."));
        
        Order newOrder = new Order(0, currentUser.getId(), currentUser.getUsername(), 0, "CREATED", LocalDateTime.now(), finalTotal);
        
        boolean success = orderDAO.createOrder(newOrder, CartService.getCartItems());
        
        if (success) {
            CartService.clearCart();
            refreshCart();
            showAlert("Siparişiniz başarıyla alındı! 🎉\nAfiyet olsun!");
            ((Stage) checkoutButton.getScene().getWindow()).close(); 
        } else {
            showAlert("Sipariş oluşturulurken veritabanı hatası oluştu! ❌");
        }
    }
    
    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Bilgi");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}