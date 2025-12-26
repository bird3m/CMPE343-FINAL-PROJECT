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
import models.Order;
import models.User;
import services.OrderDAO;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CarrierMainController {
    
    @FXML private Label carrierNameLabel;
    @FXML private Label statsLabel;
    @FXML private ListView<String> availableOrdersList; // Havuzdaki siparişler
    @FXML private ListView<String> currentOrdersList;   // Benim üzerimdekiler
    @FXML private ListView<String> completedOrdersList; // Geçmişim
    @FXML private Button logoutButton;
    
    // Veri listeleri (Ekranda görünen String halleri)
    private ObservableList<String> availableItems;
    private ObservableList<String> currentItems;
    private ObservableList<String> completedItems;
    
    // Arka plandaki gerçek Order nesneleri (ID'leri bulmak için)
    private List<Order> dbAvailableOrders;
    private List<Order> dbCurrentOrders;
    private List<Order> dbCompletedOrders;

    private User loggedInCarrier; // Giriş yapan kurye
    private OrderDAO orderDAO;

    @FXML
    private void initialize() {
        orderDAO = new OrderDAO();
        
        availableItems = FXCollections.observableArrayList();
        currentItems = FXCollections.observableArrayList();
        completedItems = FXCollections.observableArrayList();
        
        availableOrdersList.setItems(availableItems);
        currentOrdersList.setItems(currentItems);
        completedOrdersList.setItems(completedItems);
    }
    
    // LoginController'dan bu metodu çağırıp kuryeyi içeri alıyoruz
    public void setUser(User user) {
        this.loggedInCarrier = user;
        carrierNameLabel.setText("Kurye: " + user.getDisplayName());
        refreshData();
    }
    
    // Tüm listeleri veritabanından çekip yeniler
    private void refreshData() {
        if(loggedInCarrier == null) return;

        // 1. HAVUZDAKİLER (Sahipsiz ve PENDING olanlar)
        dbAvailableOrders = orderDAO.getPendingOrders(); // OrderDAO'ya bu metodu ekleyeceğiz
        availableItems.clear();
        for(Order o : dbAvailableOrders) {
            availableItems.add(formatOrder(o));
        }

        // 2. BENİM ÜZERİMDEKİLER (ON_WAY ve carrier_id benim olanlar)
        dbCurrentOrders = orderDAO.getOrdersByCarrierAndStatus(loggedInCarrier.getId(), "ON_WAY");
        currentItems.clear();
        for(Order o : dbCurrentOrders) {
            currentItems.add(formatOrder(o));
        }

        // 3. TAMAMLADIKLARIM (DELIVERED ve carrier_id benim olanlar)
        dbCompletedOrders = orderDAO.getOrdersByCarrierAndStatus(loggedInCarrier.getId(), "DELIVERED");
        completedItems.clear();
        for(Order o : dbCompletedOrders) {
            completedItems.add(formatOrder(o));
        }
        
        updateStats();
    }
    
    // Listede güzel görünsün diye String formatlama
    private String formatOrder(Order o) {
        return String.format("Sipariş #%d | Tutar: %.2f ₺ | Adres: %s", 
               o.getId(), o.getTotalCost(), o.getCustomerName()); // Adres de eklenebilir
    }

    private void updateStats() {
        statsLabel.setText(String.format(
            "📊 Toplam Teslimat: %d | Şu an Üzerimde: %d",
            dbCompletedOrders.size(),
            dbCurrentOrders.size()
        ));
    }
    
    @FXML
    private void handleAcceptOrder(ActionEvent event) {
        int selectedIndex = availableOrdersList.getSelectionModel().getSelectedIndex();
        if (selectedIndex < 0) {
            showAlert("Lütfen havuzdan bir sipariş seçin.");
            return;
        }

        Order selectedOrder = dbAvailableOrders.get(selectedIndex);
        
        // Veritabanını güncelle: Status -> ON_WAY, Carrier -> Ben
        boolean success = orderDAO.assignOrderToCarrier(selectedOrder.getId(), loggedInCarrier.getId());
        
        if(success) {
            refreshData(); // Ekranı yenile
            showAlert("Sipariş üzerine alındı! İyi yolculuklar 🛵");
        } else {
            showAlert("Hata: Sipariş alınamadı.");
        }
    }
    
    @FXML
    private void handleCompleteOrder(ActionEvent event) {
        int selectedIndex = currentOrdersList.getSelectionModel().getSelectedIndex();
        if (selectedIndex < 0) {
            showAlert("Lütfen teslim ettiğiniz siparişi seçin.");
            return;
        }
        
        Order selectedOrder = dbCurrentOrders.get(selectedIndex);
        
        // Veritabanını güncelle: Status -> DELIVERED
        boolean success = orderDAO.updateOrderStatus(selectedOrder.getId(), "DELIVERED");
        
        if(success) {
            refreshData();
            showAlert("Teslimat onaylandı! Eline sağlık 👏");
        } else {
            showAlert("Hata oluştu.");
        }
    }
    
    @FXML
    private void handleRefresh(ActionEvent event) {
        refreshData();
    }
    
    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/Login.fxml"));
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) { e.printStackTrace(); }
    }
    
    private void showAlert(String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(content);
        alert.showAndWait();
    }
}