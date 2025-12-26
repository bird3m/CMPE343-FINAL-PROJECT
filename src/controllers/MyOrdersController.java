package controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import models.Order;
import models.User;
import services.OrderDAO;

import java.util.List;

public class MyOrdersController {

    @FXML private TableView<Order> ordersTable;
    @FXML private TableColumn<Order, Integer> colId;
    @FXML private TableColumn<Order, String> colStatus;
    @FXML private TableColumn<Order, Double> colTotal;
    @FXML private TableColumn<Order, String> colDate;
    @FXML private TableColumn<Order, String> colCarrier;

    private User currentUser;
    private OrderDAO orderDAO;

    @FXML
    public void initialize() {
        orderDAO = new OrderDAO();
        setupColumns();
    }

    public void setCustomer(User user) {
        this.currentUser = user;
        loadOrders();
    }

    private void setupColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("totalCost"));
        
        // Tarih formatı
        colDate.setCellValueFactory(cellData -> {
            if (cellData.getValue().getDeliveryTime() != null)
                return new SimpleStringProperty(cellData.getValue().getDeliveryTime().toString().replace("T", " "));
            return new SimpleStringProperty("ASAP");
        });

        // Kurye bilgisi (Carrier ID 0 ise 'Atanmadı' yazsın)
        colCarrier.setCellValueFactory(cellData -> {
            int cId = cellData.getValue().getCarrierId();
            return new SimpleStringProperty(cId == 0 ? "Waiting..." : "Courier #" + cId);
        });
    }

    private void loadOrders() {
        if (currentUser != null) {
            // DAO'ya eklediğimiz yeni metodu çağırıyoruz!
            List<Order> myOrders = orderDAO.getOrdersByCustomerId(currentUser.getId());
            ordersTable.setItems(FXCollections.observableArrayList(myOrders));
        }
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) ordersTable.getScene().getWindow();
        stage.close();
    }
}