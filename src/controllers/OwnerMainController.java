package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
// DİKKAT: Doğru importlar bunlar!
import javafx.scene.control.*; 
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.io.PrintWriter;
import java.time.format.DateTimeFormatter;
import java.util.List;

import models.Product;
import models.Order; // Yeni Order modelini kullanacak
import services.ProductDAO;
import services.OrderDAO;
import services.ReportGenerator;

public class OwnerMainController {
    
    // --- 1. ÜRÜN TABLOSU ---
    @FXML private TableView<Product> productTable;
    @FXML private TableColumn<Product, Integer> productIdColumn;
    @FXML private TableColumn<Product, String> productNameColumn;
    @FXML private TableColumn<Product, String> productTypeColumn;
    @FXML private TableColumn<Product, Double> productPriceColumn;
    @FXML private TableColumn<Product, Double> productStockColumn;     
    @FXML private TableColumn<Product, Double> productThresholdColumn; 
    
    // --- 2. SİPARİŞ TABLOSU ---
    // Buradaki OrderItem; senin OrderDAO'dan gelen veriyi ekrana basmak için kullandığın iç sınıf
    @FXML private TableView<OrderItem> orderTable;
    @FXML private TableColumn<OrderItem, Integer> orderIdColumn;
    @FXML private TableColumn<OrderItem, String> orderCustomerColumn;
    @FXML private TableColumn<OrderItem, String> orderDateColumn;
    @FXML private TableColumn<OrderItem, Double> orderTotalColumn;
    @FXML private TableColumn<OrderItem, String> orderStatusColumn;
    
    // --- 3. KURYE TABLOSU ---
    @FXML private TableView<CarrierItem> carrierTable;
    @FXML private TableColumn<CarrierItem, Integer> carrierIdColumn;
    @FXML private TableColumn<CarrierItem, String> carrierNameColumn;
    @FXML private TableColumn<CarrierItem, String> carrierPhoneColumn;
    @FXML private TableColumn<CarrierItem, Double> carrierRatingColumn;
    @FXML private TableColumn<CarrierItem, Integer> carrierDeliveriesColumn;
    
    @FXML private TextArea reportPreviewArea;
    @FXML private Button logoutButton;
    
    private ObservableList<Product> products;
    private ObservableList<OrderItem> orders;
    private ObservableList<CarrierItem> carriers;
    
    @FXML
    private void initialize() {
        setupProductTable();
        setupOrderTable();
        setupCarrierTable();
        loadSampleData();
        if(reportPreviewArea != null) handleRefreshReport(null);
    }
    
    private void setupProductTable() {
        productIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        productNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        productTypeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        productPriceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        productStockColumn.setCellValueFactory(new PropertyValueFactory<>("stock"));         
        productThresholdColumn.setCellValueFactory(new PropertyValueFactory<>("threshold")); 
        
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
        // ÜRÜNLER
        ProductDAO productDAO = new ProductDAO();
        products = FXCollections.observableArrayList(productDAO.getAllProducts());
        productTable.setItems(products);
        
        // SİPARİŞLER (Gerçek Veri)
        OrderDAO orderDAO = new OrderDAO();
        List<Order> dbOrders = orderDAO.getAllOrders();
        orders = FXCollections.observableArrayList();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        
        for (Order o : dbOrders) {
            String dateStr = (o.getOrderTime() != null) ? o.getOrderTime().format(formatter) : "N/A";
            orders.add(new OrderItem(
                o.getId(),
                o.getCustomerName(),
                dateStr,
                o.getTotalCost(), // Artık Order modelinde bu metod var!
                o.getStatus()
            ));
        }
        orderTable.setItems(orders);
        
        // KURYELER (Dummy)
        carriers = FXCollections.observableArrayList();
        carriers.add(new CarrierItem(1, "Hizli Ahmet", "555-1234", 4.8, 120));
        carrierTable.setItems(carriers);
    }
    
    @FXML
    private void handleAddProduct(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ProductDialog.fxml"));
            Parent page = loader.load();
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Yeni Ürün Ekle");
            dialogStage.setScene(new Scene(page));
            ProductDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            dialogStage.showAndWait();
            if (controller.isSaveClicked()) {
                loadSampleData(); 
                handleRefreshReport(null);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void handleEditProduct(ActionEvent event) {
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if(selected != null) showAlert(Alert.AlertType.INFORMATION, "Bilgi", "Düzenlenecek: " + selected.getName());
    }

    @FXML
    private void handleDeleteProduct(ActionEvent event) {
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            ProductDAO dao = new ProductDAO();
            if(dao.deleteProduct(selected.getId())) {
                products.remove(selected);
                showAlert(Alert.AlertType.INFORMATION, "Başarılı", "Ürün silindi.");
            }
        }
    }
    
    @FXML
    private void handleRefreshReport(ActionEvent event) {
        ReportGenerator generator = new ReportGenerator();
        String reportText = generator.generateGeneralReport(); 
        if(reportPreviewArea != null) reportPreviewArea.setText(reportText);
    }

    @FXML
    private void handleSaveReport(ActionEvent event) {
        String reportContent = reportPreviewArea.getText();
        if (reportContent == null || reportContent.isEmpty()) return;
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Raporu Kaydet");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
        fileChooser.setInitialFileName("Rapor_" + System.currentTimeMillis() + ".txt");
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
    
    @FXML private void handleEmployCarrier(ActionEvent e) {} 
    @FXML private void handleFireCarrier(ActionEvent e) {}

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    // INNER CLASSES
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