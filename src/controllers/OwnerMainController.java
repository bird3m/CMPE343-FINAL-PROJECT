package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

// --- EXPLICIT IMPORTS (to avoid ambiguity) ---
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
import java.util.Map;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.text.Font;

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

    // Analytics charts
    @FXML private PieChart revenueByProductPie;
    @FXML private BarChart<String, Number> dailyRevenueBar;
    @FXML private BarChart<String, Number> topProductsBar;
    @FXML private LineChart<String, Number> revenueLineChart;
    
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
        loadAnalyticsCharts();
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
            loadAnalyticsCharts();
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
            loadAnalyticsCharts();
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
                    loadAnalyticsCharts();
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

    /**
     * Loads analytics charts in the Analytics tab.
     */
    private void loadAnalyticsCharts() {
        try {
            OrderDAO dao = new OrderDAO();

            // Pie Chart: Top products by revenue
            if (revenueByProductPie != null) {
                java.util.LinkedHashMap<String, Double> top = dao.getRevenueByProductTopN(6);
                ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
                for (java.util.Map.Entry<String, Double> e : top.entrySet()) {
                    pieData.add(new PieChart.Data(e.getKey(), e.getValue()));
                }
                if (pieData.isEmpty()) {
                    pieData.add(new PieChart.Data("No Data", 1));
                }
                revenueByProductPie.setData(pieData);
            }

            // Bar Chart: Daily revenue for last 7 days
            if (dailyRevenueBar != null) {
                java.util.LinkedHashMap<String, Double> daily = dao.getDailyRevenueLastNDays(7);
                XYChart.Series<String, Number> series = new XYChart.Series<>();
                series.setName("Revenue (₺)");
                for (java.util.Map.Entry<String, Double> e : daily.entrySet()) {
                    series.getData().add(new XYChart.Data<>(e.getKey(), e.getValue()));
                }
                dailyRevenueBar.getData().clear();
                if (series.getData().isEmpty()) {
                    // add empty zero bar so chart looks reasonable
                    series.getData().add(new XYChart.Data<>("-", 0));
                }
                dailyRevenueBar.getData().add(series);
            }

            // Top products by quantity (kg)
            if (topProductsBar != null) {
                java.util.LinkedHashMap<String, Double> topQty = dao.getTopProductsByQuantityTopN(6);
                XYChart.Series<String, Number> qtySeries = new XYChart.Series<>();
                qtySeries.setName("Kg sold");
                for (java.util.Map.Entry<String, Double> e : topQty.entrySet()) {
                    qtySeries.getData().add(new XYChart.Data<>(e.getKey(), e.getValue()));
                }
                topProductsBar.getData().clear();
                if (qtySeries.getData().isEmpty()) {
                    qtySeries.getData().add(new XYChart.Data<>("-", 0));
                }
                topProductsBar.getData().add(qtySeries);

                // Improve label visibility for long product names
                try {
                    CategoryAxis topXAxis = (CategoryAxis) topProductsBar.getXAxis();
                    topXAxis.setTickLabelRotation(-40);
                    topXAxis.setTickLabelGap(4);
                    topXAxis.setTickLabelFont(Font.font(11));
                    topProductsBar.setCategoryGap(12);
                    topProductsBar.setBarGap(3);
                } catch (Exception ignore) { }
            }

            // Revenue line chart: last 30 days
            if (revenueLineChart != null) {
                java.util.LinkedHashMap<String, Double> last30 = dao.getDailyRevenueLastNDays(30);
                XYChart.Series<String, Number> revSeries = new XYChart.Series<>();
                revSeries.setName("Revenue (₺)");
                for (java.util.Map.Entry<String, Double> e : last30.entrySet()) {
                    revSeries.getData().add(new XYChart.Data<>(e.getKey(), e.getValue()));
                }
                revenueLineChart.getData().clear();
                if (revSeries.getData().isEmpty()) {
                    revSeries.getData().add(new XYChart.Data<>("-", 0));
                }
                revenueLineChart.getData().add(revSeries);

                // Rotate revenue line chart x-axis labels for readability
                try {
                    CategoryAxis revXAxis = (CategoryAxis) revenueLineChart.getXAxis();
                    revXAxis.setTickLabelRotation(-45);
                    revXAxis.setTickLabelFont(Font.font(9));
                    revXAxis.setTickLabelGap(3);
                } catch (Exception ignore) { }
            }

            // Ensure daily revenue x-axis labels don't overlap
            if (dailyRevenueBar != null) {
                try {
                    CategoryAxis dayXAxis = (CategoryAxis) dailyRevenueBar.getXAxis();
                    dayXAxis.setTickLabelRotation(-30);
                    dayXAxis.setTickLabelFont(Font.font(10));
                    dayXAxis.setTickLabelGap(3);
                } catch (Exception ignore) { }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
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