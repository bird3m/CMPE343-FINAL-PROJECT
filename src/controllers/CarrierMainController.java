package controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import models.Order;
import models.User;
import services.OrderDAO;
import java.util.List;

/**
 * Controller for carrier dashboard: shows available orders and assigned deliveries.
 */
public class CarrierMainController {

    // --- FXML COMPONENTS ---
    @FXML private Label welcomeLabel;

    // TABLE 1: Available Orders
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
        // Prompt carrier to enter actual delivered date/time
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Complete Delivery");
        dialog.setHeaderText("Enter actual delivery date and time:");

        DatePicker datePicker = new DatePicker();
        ComboBox<String> timeCombo = new ComboBox<>();

        // populate 30-min slots
        javafx.collections.ObservableList<String> slots = FXCollections.observableArrayList();
        for (int h = 0; h < 24; h++) {
            slots.add(String.format("%02d:00", h));
            slots.add(String.format("%02d:30", h));
        }
        timeCombo.setItems(slots);

        // sensible defaults (Istanbul timezone)
        ZoneId ist = ZoneId.of("Europe/Istanbul");
        LocalDateTime nowI = LocalDateTime.now(ist);
        LocalDateTime defaultDt = nowI;
        int minute = defaultDt.getMinute();
        if (minute > 0 && minute <= 30) defaultDt = defaultDt.withMinute(30).withSecond(0).withNano(0);
        else if (minute > 30) defaultDt = defaultDt.plusHours(1).withMinute(0).withSecond(0).withNano(0);

        datePicker.setValue(defaultDt.toLocalDate());
        timeCombo.setValue(String.format("%02d:%02d", defaultDt.getHour(), defaultDt.getMinute()));

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        grid.add(new Label("Date:"), 0, 0);
        grid.add(datePicker, 1, 0);
        grid.add(new Label("Time:"), 0, 1);
        grid.add(timeCombo, 1, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> dialogButton);
        ButtonType result = dialog.showAndWait().orElse(ButtonType.CANCEL);
        if (result != ButtonType.OK) return;

        LocalDate d = datePicker.getValue();
        String t = timeCombo.getValue();
        if (d == null || t == null || t.isEmpty()) {
            showAlert("Please provide both date and time for delivery.");
            return;
        }
        DateTimeFormatter tf = DateTimeFormatter.ofPattern("HH:mm");
        LocalTime lt = LocalTime.parse(t, tf);
        LocalDateTime delivered = LocalDateTime.of(d, lt);

        boolean success = orderDAO.updateOrderStatus(selectedOrder.getId(), "DELIVERED", delivered);
        if (success) {
            showAlert("Delivery Completed! ✅");
            refreshData();
        } else {
            showAlert("Failed to update delivery status in DB.");
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