package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

// --- EXPLICIT IMPORTS (KARISIKLIGI ONLEMEK ICIN) ---
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.PrintWriter;
import java.time.format.DateTimeFormatter;
import java.util.List;

import models.Product;
import models.Order;
import models.User;
import services.UserDAO;
import services.CarrierRatingDAO;
import services.ProductDAO;
import services.OrderDAO;


/**
 * Controller for the Owner (Admin) Dashboard.
 * Fixed: Imports are now explicit to avoid "does not take parameters" error.
 */
public class OwnerMainController {
    
    // --- 1. PRODUCT TABLE ---
    @FXML private TableView<Product> productTable;
    @FXML private TableColumn<Product, Integer> productIdColumn;
    @FXML private TableColumn<Product, String> productNameColumn;
    @FXML private TableColumn<Product, String> productTypeColumn;
    @FXML private TableColumn<Product, Double> productPriceColumn;
    @FXML private TableColumn<Product, Double> productStockColumn;     
    @FXML private TableColumn<Product, Double> productThresholdColumn; 
    
    // --- 2. ORDER TABLE ---
    @FXML private TableView<OrderItem> orderTable;
    @FXML private TableColumn<OrderItem, Integer> orderIdColumn;
    @FXML private TableColumn<OrderItem, String> orderCustomerColumn;
    @FXML private TableColumn<OrderItem, String> orderDateColumn;
    @FXML private TableColumn<OrderItem, Double> orderTotalColumn;
    @FXML private TableColumn<OrderItem, String> orderStatusColumn;
    
    // --- 3. CARRIER TABLE ---
    @FXML private TableView<CarrierItem> carrierTable;
    @FXML private TableColumn<CarrierItem, Integer> carrierIdColumn;
    @FXML private TableColumn<CarrierItem, String> carrierNameColumn;
    @FXML private TableColumn<CarrierItem, String> carrierPhoneColumn;
    @FXML private TableColumn<CarrierItem, Double> carrierRatingColumn;
    @FXML private TableColumn<CarrierItem, Integer> carrierDeliveriesColumn;
    
    @FXML private TextArea reportPreviewArea;
    @FXML private Button logoutButton;
    @FXML private Button messagesButton;
    
    private User currentUser;
    private ObservableList<Product> products;
    private ObservableList<OrderItem> orders;
    private ObservableList<CarrierItem> carriers;
    
    public void setUser(User user) {
        this.currentUser = user;
    }
    
    @FXML
    private void initialize() {
        setupProductTable();
        setupOrderTable();
        setupCarrierTable();
        loadSampleData(); 
        //if(reportPreviewArea != null) handleRefreshReport(null);
    }
    
    private void setupProductTable() {
        productIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        productNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        productTypeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        productPriceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        productStockColumn.setCellValueFactory(new PropertyValueFactory<>("stock"));        
        productThresholdColumn.setCellValueFactory(new PropertyValueFactory<>("threshold")); 
        
        // Custom cell formatting
        productStockColumn.setCellFactory(tc -> new TableCell<Product, Double>() {
            @Override protected void updateItem(Double value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty || value == null ? null : String.format("%.2f kg", value));
            }
        });
        
        productPriceColumn.setCellFactory(tc -> new TableCell<Product, Double>() {
            @Override protected void updateItem(Double value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty || value == null ? null : String.format("%.2f ₺", value));
            }
        });
    }
    
    private void setupOrderTable() {
        orderIdColumn.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        orderCustomerColumn.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        orderDateColumn.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
        orderTotalColumn.setCellValueFactory(new PropertyValueFactory<>("total"));
        orderStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        orderTotalColumn.setCellFactory(tc -> new TableCell<OrderItem, Double>() {
            @Override protected void updateItem(Double value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty || value == null ? null : String.format("%.2f ₺", value));
            }
        });
    }

    private void setupCarrierTable() {
        carrierIdColumn.setCellValueFactory(new PropertyValueFactory<>("carrierId"));
        carrierNameColumn.setCellValueFactory(new PropertyValueFactory<>("carrierName"));
        carrierPhoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        carrierRatingColumn.setCellValueFactory(new PropertyValueFactory<>("rating"));
        carrierDeliveriesColumn.setCellValueFactory(new PropertyValueFactory<>("deliveryCount"));
        
        carrierRatingColumn.setCellFactory(tc -> new TableCell<CarrierItem, Double>() {
            @Override protected void updateItem(Double value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty || value == null ? null : String.format("⭐ %.1f", value));
            }
        });
    }
    
    private void loadSampleData() {
        // PRODUCTS
        ProductDAO productDAO = new ProductDAO();
        products = FXCollections.observableArrayList(productDAO.getAllProducts());
        productTable.setItems(products);
        
        // ORDERS
        OrderDAO orderDAO = new OrderDAO();
        List<Order> dbOrders = orderDAO.getAllOrders();
        orders = FXCollections.observableArrayList();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        
        for (Order o : dbOrders) {
            String dateStr = (o.getDeliveryTime() != null) ? o.getDeliveryTime().format(formatter) : "N/A";
            orders.add(new OrderItem(
                o.getId(),
                o.getCustomerName(),
                dateStr,
                o.getTotalCost(),
                o.getStatus()
            ));
        }
        orderTable.setItems(orders);
        
        // CARRIERS
        UserDAO userDAO = new UserDAO();
        CarrierRatingDAO ratingDAO = new CarrierRatingDAO();
        List<User> dbCarriers = userDAO.getAllCarriers();
        carriers = FXCollections.observableArrayList();
        
        for (User u : dbCarriers) {
            double avgRating = ratingDAO.getAverageRating(u.getId());
            int deliveryCount = ratingDAO.getDeliveryCount(u.getId());
            
            carriers.add(new CarrierItem(
                u.getId(),
                u.getFullName(), 
                u.getPhone(), 
                avgRating, 
                deliveryCount
            ));
        }
        carrierTable.setItems(carriers);
    }
    
    @FXML
    private void handleAddProduct(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ProductForm.fxml"));
            Parent page = loader.load();
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Add New Product");
            dialogStage.setScene(new Scene(page));
            dialogStage.showAndWait();
            loadSampleData(); 
           // handleRefreshReport(null);
        } catch (Exception e) { 
            e.printStackTrace(); 
            showAlert(Alert.AlertType.ERROR, "Error", "Could not open Add Product form.");
        }
    }

    @FXML
    private void handleEditProduct(ActionEvent event) {
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a product to edit.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ProductForm.fxml"));
            Parent page = loader.load();
            ProductFormController controller = loader.getController();
            controller.setProduct(selected);
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Edit Product");
            dialogStage.setScene(new Scene(page));
            dialogStage.showAndWait();
            loadSampleData(); 
        } catch (Exception e) { 
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Could not open Edit Product form.");
        }
    }

    @FXML
    private void handleDeleteProduct(ActionEvent event) {
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete " + selected.getName() + "?", ButtonType.YES, ButtonType.NO);
            confirm.showAndWait();
            if (confirm.getResult() == ButtonType.YES) {
                ProductDAO dao = new ProductDAO();
                if(dao.deleteProduct(selected.getId())) {
                    products.remove(selected);
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Product deleted successfully.");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Could not delete product.");
                }
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a product to delete.");
        }
    }
    
    @FXML
    private void handleEmployCarrier(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/CarrierForm.fxml"));
            Parent page = loader.load();
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Employ New Carrier");
            dialogStage.setScene(new Scene(page));
            dialogStage.showAndWait();
            loadSampleData();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Could not open Carrier form.");
        }
    }

    @FXML
    private void handleFireCarrier(ActionEvent event) {
        CarrierItem selected = carrierTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a carrier to fire.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, 
            "Are you sure you want to fire " + selected.getCarrierName() + "?", 
            ButtonType.YES, ButtonType.NO);
        confirm.showAndWait();
        if (confirm.getResult() == ButtonType.YES) {
            UserDAO dao = new UserDAO();
            if (dao.deleteUser(selected.getCarrierId())) {
                carriers.remove(selected);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Carrier fired successfully.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Could not fire carrier.");
            }
        }
    }
    
 

    @FXML
    private void handleSaveReport(ActionEvent event) {
        String reportContent = reportPreviewArea.getText();
        if (reportContent == null || reportContent.isEmpty()) return;
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Report");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
        fileChooser.setInitialFileName("Report_" + System.currentTimeMillis() + ".txt");
        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            try (PrintWriter writer = new PrintWriter(file)) {
                writer.println(reportContent);
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/Login.fxml"));
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void handleOpenMessages(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/OwnerChat.fxml"));
            Parent root = loader.load();

            OwnerChatController controller = loader.getController();
            controller.setUser(currentUser);

            Stage chatStage = new Stage();
            Scene scene = new Scene(root, 900, 600);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            chatStage.setScene(scene);
            chatStage.setTitle("Owner Messages");
            chatStage.centerOnScreen();
            chatStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Could not open owner chat!\n" + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    // --- INNER CLASSES ---
    public static class OrderItem {
        private int orderId; private String customerName, orderDate, status; private double total;
        public OrderItem(int id, String name, String date, double total, String status) {
            this.orderId = id; this.customerName = name; this.orderDate = date; this.total = total; this.status = status;
        }
        public int getOrderId() { return orderId; }
        public String getCustomerName() { return customerName; }
        public String getOrderDate() { return orderDate; }
        public double getTotal() { return total; }
        public String getStatus() { return status; }
    }
    
    public static class CarrierItem {
        private int carrierId, deliveryCount; private String carrierName, phone; private double rating;
        public CarrierItem(int id, String name, String phone, double rating, int count) {
            this.carrierId = id; this.carrierName = name; this.phone = phone; this.rating = rating; this.deliveryCount = count;
        }
        public int getCarrierId() { return carrierId; }
        public String getCarrierName() { return carrierName; }
        public String getPhone() { return phone; }
        public double getRating() { return rating; }
        public int getDeliveryCount() { return deliveryCount; }
    }
}