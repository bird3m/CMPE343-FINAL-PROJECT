package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import models.Product;
import services.ProductService;

/**
 * Owner Main Controller
 * 
 * Features:
 * - Product management (add, edit, delete)
 * - View all orders
 * - Carrier management
 * - Business reports and analytics
 * 
 * @author Group04
 * @version 1.0
 */
public class OwnerMainController {
    
    // Product Table Components
    @FXML private TableView<Product> productTable;
    @FXML private TableColumn<Product, Integer> productIdColumn;
    @FXML private TableColumn<Product, String> productNameColumn;
    @FXML private TableColumn<Product, String> productTypeColumn;
    @FXML private TableColumn<Product, Double> productPriceColumn;
    @FXML private TableColumn<Product, Double> productStockColumn;
    @FXML private TableColumn<Product, Double> productThresholdColumn;
    
    // Order Table Components
    @FXML private TableView<OrderItem> orderTable;
    @FXML private TableColumn<OrderItem, Integer> orderIdColumn;
    @FXML private TableColumn<OrderItem, String> orderCustomerColumn;
    @FXML private TableColumn<OrderItem, String> orderDateColumn;
    @FXML private TableColumn<OrderItem, Double> orderTotalColumn;
    @FXML private TableColumn<OrderItem, String> orderStatusColumn;
    
    // Carrier Table Components
    @FXML private TableView<CarrierItem> carrierTable;
    @FXML private TableColumn<CarrierItem, Integer> carrierIdColumn;
    @FXML private TableColumn<CarrierItem, String> carrierNameColumn;
    @FXML private TableColumn<CarrierItem, String> carrierPhoneColumn;
    @FXML private TableColumn<CarrierItem, Double> carrierRatingColumn;
    @FXML private TableColumn<CarrierItem, Integer> carrierDeliveriesColumn;
    
    // Buttons
    @FXML private Button addProductButton;
    @FXML private Button editProductButton;
    @FXML private Button deleteProductButton;
    @FXML private Button logoutButton;
    
    // Data Lists
    private ObservableList<Product> products;
    private ObservableList<OrderItem> orders;
    private ObservableList<CarrierItem> carriers;
    
    /**
     * Initialize - Called automatically after FXML is loaded
     */
    @FXML
    private void initialize() {
        setupProductTable();
        setupOrderTable();
        setupCarrierTable();
        
        loadSampleData();
    }
    
    /**
     * Setup product table columns
     */
    private void setupProductTable() {
        productIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        productNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        productTypeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        productPriceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        productStockColumn.setCellValueFactory(new PropertyValueFactory<>("stock"));
        productThresholdColumn.setCellValueFactory(new PropertyValueFactory<>("threshold"));
        
        // Format numeric columns
        productPriceColumn.setCellFactory(tc -> new TableCell<Product, Double>() {
            @Override
            protected void updateItem(Double value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty || value == null ? null : String.format("%.2f ₺", value));
            }
        });
        
        productStockColumn.setCellFactory(tc -> new TableCell<Product, Double>() {
            @Override
            protected void updateItem(Double value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty || value == null ? null : String.format("%.2f kg", value));
            }
        });
        
        productThresholdColumn.setCellFactory(tc -> new TableCell<Product, Double>() {
            @Override
            protected void updateItem(Double value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty || value == null ? null : String.format("%.2f kg", value));
            }
        });
    }
    
    /**
     * Setup order table columns
     */
    private void setupOrderTable() {
        orderIdColumn.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        orderCustomerColumn.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        orderDateColumn.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
        orderTotalColumn.setCellValueFactory(new PropertyValueFactory<>("total"));
        orderStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        // Format total column
        orderTotalColumn.setCellFactory(tc -> new TableCell<OrderItem, Double>() {
            @Override
            protected void updateItem(Double value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty || value == null ? null : String.format("%.2f ₺", value));
            }
        });
    }
    
    /**
     * Setup carrier table columns
     */
    private void setupCarrierTable() {
        carrierIdColumn.setCellValueFactory(new PropertyValueFactory<>("carrierId"));
        carrierNameColumn.setCellValueFactory(new PropertyValueFactory<>("carrierName"));
        carrierPhoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        carrierRatingColumn.setCellValueFactory(new PropertyValueFactory<>("rating"));
        carrierDeliveriesColumn.setCellValueFactory(new PropertyValueFactory<>("deliveryCount"));
        
        // Format rating column
        carrierRatingColumn.setCellFactory(tc -> new TableCell<CarrierItem, Double>() {
            @Override
            protected void updateItem(Double value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty || value == null ? null : String.format("⭐ %.1f", value));
            }
        });
    }
    
    /**
     * Load sample data
     * TODO: Replace with real database queries
     */
    private void loadSampleData() {
        // Load Products
        products = FXCollections.observableArrayList(ProductService.getAllProducts());
        productTable.setItems(products);
        
        // Load Sample Orders
        orders = FXCollections.observableArrayList();
        orders.add(new OrderItem(1001, "Ali Yılmaz", "23.12.2025 10:30", 125.50, "Delivered"));
        orders.add(new OrderItem(1002, "Ayşe Demir", "23.12.2025 11:45", 89.90, "In Transit"));
        orders.add(new OrderItem(1003, "Mehmet Can", "23.12.2025 09:15", 210.00, "Pending"));
        orders.add(new OrderItem(1004, "Zeynep Kaya", "22.12.2025 14:20", 156.80, "Delivered"));
        orders.add(new OrderItem(1005, "Can Özdemir", "22.12.2025 16:00", 95.00, "Delivered"));
        orderTable.setItems(orders);
        
        // Load Sample Carriers
        carriers = FXCollections.observableArrayList();
        carriers.add(new CarrierItem(1, "Ahmet Yıldız", "+90 532 111 2233", 4.8, 156));
        carriers.add(new CarrierItem(2, "Fatma Kara", "+90 533 444 5566", 4.6, 98));
        carriers.add(new CarrierItem(3, "Hasan Şen", "+90 534 777 8899", 4.9, 203));
        carrierTable.setItems(carriers);
    }
    
    /**
     * Handle Add Product Button
     */
    @FXML
    private void handleAddProduct(ActionEvent event) {
        showAlert(Alert.AlertType.INFORMATION, "Add Product", 
                 "➕ Add New Product\n\n" +
                 "This feature will open a dialog to:\n" +
                 "• Enter product name\n" +
                 "• Select type (vegetable/fruit)\n" +
                 "• Set price per kg\n" +
                 "• Set initial stock\n" +
                 "• Set threshold value\n" +
                 "• Upload product image\n\n" +
                 "Coming soon!");
    }
    
    /**
     * Handle Edit Product Button
     */
    @FXML
    private void handleEditProduct(ActionEvent event) {
        Product selected = productTable.getSelectionModel().getSelectedItem();
        
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", 
                     "Please select a product to edit!");
            return;
        }
        
        showAlert(Alert.AlertType.INFORMATION, "Edit Product", 
                 "✏️ Edit Product: " + selected.getName() + "\n\n" +
                 "Current Details:\n" +
                 "• Type: " + selected.getType() + "\n" +
                 "• Price: " + String.format("%.2f₺/kg", selected.getPrice()) + "\n" +
                 "• Stock: " + String.format("%.2f kg", selected.getStock()) + "\n" +
                 "• Threshold: " + String.format("%.2f kg", selected.getThreshold()) + "\n\n" +
                 "Edit dialog coming soon!");
    }
    
    /**
     * Handle Delete Product Button
     */
    @FXML
    private void handleDeleteProduct(ActionEvent event) {
        Product selected = productTable.getSelectionModel().getSelectedItem();
        
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", 
                     "Please select a product to delete!");
            return;
        }
        
        // Confirm deletion
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Product");
        confirm.setHeaderText("Are you sure?");
        confirm.setContentText("Delete product: " + selected.getName() + "?\n\n" +
                              "This will set is_active=0 in database.\n" +
                              "Past orders will remain intact.");
        
        if (confirm.showAndWait().get() == ButtonType.OK) {
            // Remove from table
            products.remove(selected);
            
            showAlert(Alert.AlertType.INFORMATION, "Deleted", 
                     "✅ Product deleted successfully!\n\n" +
                     "Product: " + selected.getName() + "\n" +
                     "It has been removed from active products.");
            
            // TODO: Update database (set is_active = 0)
        }
    }
    
    /**
     * Handle Employ Carrier
     */
    @FXML
    private void handleEmployCarrier(ActionEvent event) {
        showAlert(Alert.AlertType.INFORMATION, "Employ Carrier", 
                 "➕ Employ New Carrier\n\n" +
                 "This feature will allow:\n" +
                 "• Register new carrier\n" +
                 "• Enter carrier details\n" +
                 "• Set initial credentials\n" +
                 "• Assign delivery areas\n\n" +
                 "Coming soon!");
    }
    
    /**
     * Handle Fire Carrier
     */
    @FXML
    private void handleFireCarrier(ActionEvent event) {
        CarrierItem selected = carrierTable.getSelectionModel().getSelectedItem();
        
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", 
                     "Please select a carrier!");
            return;
        }
        
        // Confirm firing
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Fire Carrier");
        confirm.setHeaderText("Are you sure?");
        confirm.setContentText("Fire carrier: " + selected.getCarrierName() + "?\n\n" +
                              "This will deactivate their account.");
        
        if (confirm.showAndWait().get() == ButtonType.OK) {
            carriers.remove(selected);
            showAlert(Alert.AlertType.INFORMATION, "Carrier Fired", 
                     "Carrier has been removed from active carriers.");
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
     * Show alert dialog
     */
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    // ==================== INNER CLASSES ====================
    
    /**
     * Order Item for table display
     */
    public static class OrderItem {
        private int orderId;
        private String customerName;
        private String orderDate;
        private double total;
        private String status;
        
        public OrderItem(int orderId, String customerName, String orderDate, 
                        double total, String status) {
            this.orderId = orderId;
            this.customerName = customerName;
            this.orderDate = orderDate;
            this.total = total;
            this.status = status;
        }
        
        // Getters
        public int getOrderId() { return orderId; }
        public String getCustomerName() { return customerName; }
        public String getOrderDate() { return orderDate; }
        public double getTotal() { return total; }
        public String getStatus() { return status; }
    }
    
    /**
     * Carrier Item for table display
     */
    public static class CarrierItem {
        private int carrierId;
        private String carrierName;
        private String phone;
        private double rating;
        private int deliveryCount;
        
        public CarrierItem(int carrierId, String carrierName, String phone, 
                          double rating, int deliveryCount) {
            this.carrierId = carrierId;
            this.carrierName = carrierName;
            this.phone = phone;
            this.rating = rating;
            this.deliveryCount = deliveryCount;
        }
        
        // Getters
        public int getCarrierId() { return carrierId; }
        public String getCarrierName() { return carrierName; }
        public String getPhone() { return phone; }
        public double getRating() { return rating; }
        public int getDeliveryCount() { return deliveryCount; }
    }
}