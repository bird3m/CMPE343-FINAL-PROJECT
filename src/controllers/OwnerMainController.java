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

/**
 * Owner Main Controller
 * Manages products, orders, carriers, and reports
 * 
 * @author Group04
 */
public class OwnerMainController {
    
    // Product Table
    @FXML private TableView<Product> productTable;
    @FXML private TableColumn<Product, Integer> productIdColumn;
    @FXML private TableColumn<Product, String> productNameColumn;
    @FXML private TableColumn<Product, String> productTypeColumn;
    @FXML private TableColumn<Product, Double> productPriceColumn;
    @FXML private TableColumn<Product, Double> productStockColumn;
    @FXML private TableColumn<Product, Double> productThresholdColumn;
    
    // Order Table
    @FXML private TableView<OrderItem> orderTable;
    @FXML private TableColumn<OrderItem, Integer> orderIdColumn;
    @FXML private TableColumn<OrderItem, String> orderCustomerColumn;
    @FXML private TableColumn<OrderItem, String> orderDateColumn;
    @FXML private TableColumn<OrderItem, Double> orderTotalColumn;
    @FXML private TableColumn<OrderItem, String> orderStatusColumn;
    
    // Carrier Table
    @FXML private TableView<CarrierItem> carrierTable;
    @FXML private TableColumn<CarrierItem, Integer> carrierIdColumn;
    @FXML private TableColumn<CarrierItem, String> carrierNameColumn;
    @FXML private TableColumn<CarrierItem, String> carrierPhoneColumn;
    @FXML private TableColumn<CarrierItem, Double> carrierRatingColumn;
    @FXML private TableColumn<CarrierItem, Integer> carrierDeliveriesColumn;
    
    @FXML private Button addProductButton;
    @FXML private Button editProductButton;
    @FXML private Button deleteProductButton;
    @FXML private Button logoutButton;
    
    private ObservableList<Product> products;
    private ObservableList<OrderItem> orders;
    private ObservableList<CarrierItem> carriers;
    
    /**
     * Initialize - Setup tables
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
    }
    
    /**
     * Load sample data (Replace with database calls)
     */
    private void loadSampleData() {
        // Sample Products
        products = FXCollections.observableArrayList();
        products.add(new Product(1, "Tomato", "vegetable", 20.50, 50, 5, "/images/vegetables/tomato.jpg"));
        products.add(new Product(2, "Apple", "fruit", 24.50, 70, 8, "/images/fruits/apple.jpg"));
        products.add(new Product(3, "Potato", "vegetable", 15.80, 100, 10, "/images/vegetables/potato.jpg"));
        productTable.setItems(products);
        
        // Sample Orders
        orders = FXCollections.observableArrayList();
        orders.add(new OrderItem(1001, "Ali Yılmaz", "23.12.2025", 125.50, "Delivered"));
        orders.add(new OrderItem(1002, "Ayşe Demir", "23.12.2025", 89.90, "In Transit"));
        orders.add(new OrderItem(1003, "Mehmet Can", "22.12.2025", 210.00, "Pending"));
        orderTable.setItems(orders);
        
        // Sample Carriers
        carriers = FXCollections.observableArrayList();
        carriers.add(new CarrierItem(1, "Ahmet Yıldız", "+90 532 111 2233", 4.8, 156));
        carriers.add(new CarrierItem(2, "Fatma Kara", "+90 533 444 5566", 4.6, 98));
        carrierTable.setItems(carriers);
    }
    
    /**
     * Add product handler
     */
    @FXML
    private void handleAddProduct(ActionEvent event) {
        showAlert(Alert.AlertType.INFORMATION, "Add Product", 
                 "Product addition dialog coming soon!\n\n" +
                 "This will open a form to enter:\n" +
                 "- Product name\n- Type (vegetable/fruit)\n- Price\n- Stock\n- Threshold\n- Image");
    }
    
    /**
     * Edit product handler
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
                 "Editing: " + selected.getName() + "\n\n" +
                 "Product edit dialog coming soon!");
    }
    
    /**
     * Delete product handler
     */
    @FXML
    private void handleDeleteProduct(ActionEvent event) {
        Product selected = productTable.getSelectionModel().getSelectedItem();
        
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", 
                     "Please select a product to delete!");
            return;
        }
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Product");
        confirm.setHeaderText("Are you sure?");
        confirm.setContentText("Delete product: " + selected.getName() + "?");
        
        if (confirm.showAndWait().get() == ButtonType.OK) {
            products.remove(selected);
            showAlert(Alert.AlertType.INFORMATION, "Deleted", 
                     "Product deleted successfully!");
        }
    }
    
    /**
     * Logout handler
     */
    @FXML
    private void handleLogout(ActionEvent event) {
        try {
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
     * Show alert dialog
     */
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    // ============= INNER CLASSES FOR TABLE DATA =============
    
    /**
     * Order Item for table
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
        
        public int getOrderId() { return orderId; }
        public String getCustomerName() { return customerName; }
        public String getOrderDate() { return orderDate; }
        public double getTotal() { return total; }
        public String getStatus() { return status; }
    }
    
    /**
     * Carrier Item for table
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
        
        public int getCarrierId() { return carrierId; }
        public String getCarrierName() { return carrierName; }
        public String getPhone() { return phone; }
        public double getRating() { return rating; }
        public int getDeliveryCount() { return deliveryCount; }
    }
}