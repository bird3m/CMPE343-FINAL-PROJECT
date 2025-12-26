package controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import models.Order;
import models.User;
import services.OrderDAO;
import java.util.List;

public class CarrierMainController {

    // --- FXML BİLEŞENLERİ (Yeni Mor FXML ile %100 Uyumlu) ---
    @FXML private Label welcomeLabel;

    // TABLO 1: Available Orders
    @FXML private TableView<Order> availableOrdersTable;
    @FXML private TableColumn<Order, Integer> colAvailId;
    @FXML private TableColumn<Order, String> colAvailCustomer;
    @FXML private TableColumn<Order, Double> colAvailTotal;
    @FXML private TableColumn<Order, String> colAvailDate;

    // TABLO 2: My Deliveries
    @FXML private TableView<Order> currentOrdersTable;
    @FXML private TableColumn<Order, Integer> colCurrId;
    @FXML private TableColumn<Order, String> colCurrCustomer;
    @FXML private TableColumn<Order, Double> colCurrTotal;
    @FXML private TableColumn<Order, String> colCurrStatus;

    private User loggedInCarrier;
    private OrderDAO orderDAO;

    @FXML
    public void initialize() {
        orderDAO = new OrderDAO();
        setupTableColumns();
    }

    public void setUser(User user) {
        this.loggedInCarrier = user;
        if (user != null) {
            welcomeLabel.setText("Welcome, " + user.getDisplayName());
            refreshData();
        }
    }

    private void setupTableColumns() {
        // --- Table 1 Setup ---
        colAvailId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colAvailCustomer.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        colAvailTotal.setCellValueFactory(new PropertyValueFactory<>("totalCost"));
        colAvailDate.setCellValueFactory(cellData -> {
            if (cellData.getValue().getDeliveryTime() != null) {
                return new SimpleStringProperty(cellData.getValue().getDeliveryTime().toString().replace("T", " "));
            }
            return new SimpleStringProperty("ASAP");
        });

        // --- Table 2 Setup ---
        colCurrId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCurrCustomer.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        colCurrTotal.setCellValueFactory(new PropertyValueFactory<>("totalCost"));
        colCurrStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    @FXML
    private void handleRefresh(ActionEvent event) {
        refreshData();
    }

    private void refreshData() {
        if (loggedInCarrier == null) return;

        // 1. Available Orders
        List<Order> pendingOrders = orderDAO.getPendingOrders();
        availableOrdersTable.setItems(FXCollections.observableArrayList(pendingOrders));

        // 2. My Deliveries (ASSIGNED)
        List<Order> myOrders = orderDAO.getOrdersByCarrierAndStatus(loggedInCarrier.getId(), "ASSIGNED");
        currentOrdersTable.setItems(FXCollections.observableArrayList(myOrders));
    }

    @FXML
    private void handleAcceptOrder(ActionEvent event) {
        Order selectedOrder = availableOrdersTable.getSelectionModel().getSelectedItem();
        if (selectedOrder == null) {
            showAlert("Please select an order first!");
            return;
        }
        boolean success = orderDAO.assignOrderToCarrier(selectedOrder.getId(), loggedInCarrier.getId());
        if (success) {
            showAlert("Order Accepted! 🏍️");
            refreshData();
        }
    }

    @FXML
    private void handleCompleteOrder(ActionEvent event) {
        Order selectedOrder = currentOrdersTable.getSelectionModel().getSelectedItem();
        if (selectedOrder == null) {
            showAlert("Please select an order to complete!");
            return;
        }
        boolean success = orderDAO.updateOrderStatus(selectedOrder.getId(), "DELIVERED");
        if (success) {
            showAlert("Delivery Completed! ✅");
            refreshData();
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/Login.fxml"));
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Info");
        alert.setContentText(message);
        alert.showAndWait();
    }
}