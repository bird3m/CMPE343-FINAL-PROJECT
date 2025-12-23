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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Carrier Main Controller
 * 
 * Features:
 * - View available orders
 * - Accept orders
 * - View current deliveries
 * - Complete deliveries
 * - View delivery history
 * 
 * @author Group04
 * @version 1.0
 */
public class CarrierMainController {
    
    @FXML private Label carrierNameLabel;
    @FXML private Label statsLabel;
    @FXML private ListView<String> availableOrdersList;
    @FXML private ListView<String> currentOrdersList;
    @FXML private ListView<String> completedOrdersList;
    @FXML private Button acceptOrderButton;
    @FXML private Button refreshButton;
    @FXML private Button completeOrderButton;
    @FXML private Button logoutButton;
    
    private ObservableList<String> availableOrders;
    private ObservableList<String> currentOrders;
    private ObservableList<String> completedOrders;
    
    private int totalDeliveries = 0;
    
    /**
     * Initialize - Called automatically after FXML is loaded
     */
    @FXML
    private void initialize() {
        // Initialize lists
        availableOrders = FXCollections.observableArrayList();
        currentOrders = FXCollections.observableArrayList();
        completedOrders = FXCollections.observableArrayList();
        
        // Set lists to views
        availableOrdersList.setItems(availableOrders);
        currentOrdersList.setItems(currentOrders);
        completedOrdersList.setItems(completedOrders);
        
        // Load sample data
        loadSampleOrders();
        
        // Update stats
        updateStats();
    }
    
    /**
     * Load sample order data
     * TODO: Replace with real database queries
     */
    private void loadSampleOrders() {
        // Available orders
        availableOrders.add("Order #1001 | Customer: Ali Yılmaz | 125.50₺ | Beşiktaş, İstanbul");
        availableOrders.add("Order #1002 | Customer: Ayşe Demir | 89.90₺ | Şişli, İstanbul");
        availableOrders.add("Order #1003 | Customer: Mehmet Can | 210.00₺ | Kadıköy, İstanbul");
        availableOrders.add("Order #1004 | Customer: Zeynep Kaya | 156.80₺ | Üsküdar, İstanbul");
        
        // Current orders (already accepted)
        currentOrders.add("Order #999 | Customer: Can Özdemir | 134.50₺ | Beşiktaş, İstanbul");
        
        // Completed orders
        completedOrders.add("Order #998 | Customer: Deniz Aydın | 95.00₺ | Delivered: 22.12.2025");
        completedOrders.add("Order #997 | Customer: Elif Şen | 78.20₺ | Delivered: 21.12.2025");
        completedOrders.add("Order #996 | Customer: Fatma Yıldız | 189.50₺ | Delivered: 20.12.2025");
        
        totalDeliveries = completedOrders.size();
    }
    
    /**
     * Update statistics label
     */
    private void updateStats() {
        statsLabel.setText(String.format(
            "📊 Total Deliveries: %d | Current: %d",
            totalDeliveries,
            currentOrders.size()
        ));
    }
    
    /**
     * Handle Accept Order Button
     */
    @FXML
    private void handleAcceptOrder(ActionEvent event) {
        String selected = availableOrdersList.getSelectionModel().getSelectedItem();
        
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", 
                     "Please select an order to accept!");
            return;
        }
        
        // Confirm acceptance
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Accept Order");
        confirm.setHeaderText("Accept this delivery?");
        confirm.setContentText(selected);
        
        if (confirm.showAndWait().get() == ButtonType.OK) {
            // Move from available to current
            availableOrders.remove(selected);
            currentOrders.add(selected);
            
            // Update stats
            updateStats();
            
            showAlert(Alert.AlertType.INFORMATION, "Order Accepted", 
                     "✅ Order successfully accepted!\n\n" +
                     "It has been added to your current deliveries.\n" +
                     "Please deliver within the specified timeframe.");
        }
    }
    
    /**
     * Handle Refresh Button
     */
    @FXML
    private void handleRefresh(ActionEvent event) {
        // TODO: Fetch new orders from database
        showAlert(Alert.AlertType.INFORMATION, "Refresh", 
                 "🔄 Checking for new orders...\n\n" +
                 "No new orders at this time.");
    }
    
    /**
     * Handle Complete Order Button
     */
    @FXML
    private void handleCompleteOrder(ActionEvent event) {
        String selected = currentOrdersList.getSelectionModel().getSelectedItem();
        
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", 
                     "Please select an order to complete!");
            return;
        }
        
        // Show completion dialog
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Complete Delivery");
        dialog.setHeaderText("Complete this delivery?");
        dialog.setContentText("Enter any delivery notes (optional):");
        
        dialog.showAndWait().ifPresent(notes -> {
            // Get current date/time
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
            String completionTime = LocalDateTime.now().format(formatter);
            
            // Remove from current orders
            currentOrders.remove(selected);
            
            // Add to completed with timestamp
            String completedOrder = selected.replace("İstanbul", 
                "İstanbul | Completed: " + completionTime);
            completedOrders.add(0, completedOrder); // Add to top
            
            // Update stats
            totalDeliveries++;
            updateStats();
            
            showAlert(Alert.AlertType.INFORMATION, "Delivery Completed", 
                     "✅ Delivery marked as complete!\n\n" +
                     "Completed at: " + completionTime + "\n" +
                     "Payment collected successfully.\n\n" +
                     "Great job!");
        });
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
     * Show alert dialog
     */
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}