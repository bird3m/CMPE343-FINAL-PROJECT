package controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;


import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.util.Callback;

import models.Order;
import models.User;
import services.OrderDAO;

import java.io.File;
import java.io.FileOutputStream;
import java.awt.Desktop;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

public class MyOrdersController {

    @FXML private TableView<Order> ordersTable;
    @FXML private TableColumn<Order, Integer> colId;
    @FXML private TableColumn<Order, String> colStatus;
    @FXML private TableColumn<Order, Double> colTotal;
    @FXML private TableColumn<Order, String> colDate;
    @FXML private TableColumn<Order, String> colCarrier;
    
    private TableColumn<Order, Void> colAction;

    private User currentUser;
    private OrderDAO orderDAO;

    @FXML
    public void initialize() {
        orderDAO = new OrderDAO();
        setupColumns();
        addActionButtonColumn();
    }

    public void setCustomer(User user) {
        this.currentUser = user;
        loadOrders();
    }

    private void setupColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("totalCost"));
        
        colDate.setCellValueFactory(cellData -> {
            if (cellData.getValue().getDeliveryTime() != null)
                return new SimpleStringProperty(cellData.getValue().getDeliveryTime().toString().replace("T", " "));
            return new SimpleStringProperty("ASAP");
        });

        colCarrier.setCellValueFactory(cellData -> {
            int cId = cellData.getValue().getCarrierId();
            return new SimpleStringProperty(cId == 0 ? "Waiting..." : "Courier #" + cId);
        });
        
        // Custom formatting for currency
        colTotal.setCellFactory(tc -> new TableCell<Order, Double>() {
            @Override protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                setText(empty || price == null ? null : String.format("%.2f TL", price));
            }
        });
    }

    private void addActionButtonColumn() {
        colAction = new TableColumn<Order, Void>("Action");
        colAction.setPrefWidth(160);

        Callback<TableColumn<Order, Void>, TableCell<Order, Void>> cellFactory = param -> new TableCell<Order, Void>() {
            private final Button cancelBtn = new Button("Cancel");
            private final Button viewBtn = new Button("Invoice");

            {
                cancelBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-cursor: hand;");
                cancelBtn.setOnAction(event -> {
                    Order order = getTableView().getItems().get(getIndex());
                    handleCancelOrder(order);
                });

                viewBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-cursor: hand;");
                viewBtn.setOnAction(event -> {
                    Order order = getTableView().getItems().get(getIndex());
                    handleViewInvoice(order);
                });
            }

            @Override
            public void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Order order = getTableView().getItems().get(getIndex());
                    if ("CREATED".equalsIgnoreCase(order.getStatus())) {
                        setGraphic(cancelBtn);
                    } else {
                        // For ASSIGNED or DELIVERED status, show Invoice button
                        setGraphic(viewBtn);
                    }
                }
            }
        };

        colAction.setCellFactory(cellFactory);
        ordersTable.getColumns().add(colAction);
    }

    private void handleViewInvoice(Order order)
{
    String selectSql = "SELECT invoice_pdf, invoice_log FROM orderinfo WHERE id = ?";
    String updateSql = "UPDATE orderinfo SET invoice_pdf = ? WHERE id = ?";

        String savedPath = null;
        try (Connection conn = services.DatabaseAdapter.getConnection();
            PreparedStatement selectStmt = conn.prepareStatement(selectSql))
        {
        selectStmt.setInt(1, order.getId());

        try (ResultSet rs = selectStmt.executeQuery())
        {
            if (!rs.next())
            {
                showAlert(Alert.AlertType.WARNING, "No Data", "Order not found in DB.");
                return;
            }

            byte[] data = rs.getBytes("invoice_pdf");
            String base64Log = null;

            if (data == null || data.length == 0) {
                // try to read invoice_log (Base64 CLOB)
                base64Log = rs.getString("invoice_log");
                if (base64Log != null && !base64Log.isEmpty()) {
                    try { data = java.util.Base64.getDecoder().decode(base64Log); } catch (Exception ex) { data = null; }
                }
            }
            // If still missing OR content doesn't look like a PDF, generate on the fly and store both forms
            boolean looksLikePdf = (data != null && data.length >= 4 && new String(data, 0, 4, java.nio.charset.StandardCharsets.ISO_8859_1).startsWith("%PDF"));
            if (!looksLikePdf) {
                data = services.PDFInvoiceGenerator.generateInvoicePDF(order);
                data = services.PDFInvoiceGenerator.generateInvoicePDF(order);

                if (data == null || data.length == 0) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Invoice generation failed.");
                    return;
                }

                try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                    updateStmt.setBytes(1, data);
                    updateStmt.setInt(2, order.getId());
                    updateStmt.executeUpdate();
                }

                // Also store base64 into invoice_log
                try (PreparedStatement pstmtLog = conn.prepareStatement("UPDATE orderinfo SET invoice_log = ? WHERE id = ?")) {
                    String base64 = java.util.Base64.getEncoder().encodeToString(data);
                    pstmtLog.setString(1, base64);
                    pstmtLog.setInt(2, order.getId());
                    pstmtLog.executeUpdate();
                } catch (Exception e) {
                    System.err.println("Warning: failed to save invoice_log: " + e.getMessage());
                }
            }

            // Save to temp PDF file and open
            File tempFile = new File("Invoice_" + order.getId() + ".pdf");
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                fos.write(data);
            }
            savedPath = tempFile.getAbsolutePath();

            boolean opened = false;
            if (Desktop.isDesktopSupported()) {
                try {
                    Desktop.getDesktop().open(tempFile);
                    opened = true;
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            if (!opened) {
                // Fallback to platform specific open commands
                try {
                    String path = tempFile.getAbsolutePath();
                    String os = System.getProperty("os.name").toLowerCase();
                    if (os.contains("win")) {
                        new ProcessBuilder("rundll32", "url.dll,FileProtocolHandler", path).start();
                    } else if (os.contains("mac")) {
                        new ProcessBuilder("open", path).start();
                    } else {
                        new ProcessBuilder("xdg-open", path).start();
                    }
                    opened = true;
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            if (!opened) {
                showAlert(Alert.AlertType.INFORMATION, "Saved", "Invoice saved to: " + tempFile.getAbsolutePath());
            }
        }
    }
    catch (Exception e)
    {
        e.printStackTrace();
        String msg = "Could not open invoice: " + (e.getMessage() != null ? e.getMessage() : "Unknown error");
        if (savedPath != null) msg += "\nSaved to: " + savedPath;
        showAlert(Alert.AlertType.ERROR, "Error", msg);
    }
}


    private void handleCancelOrder(Order order) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Cancel Order #" + order.getId() + "?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait();
        if (alert.getResult() == ButtonType.YES) {
            if (orderDAO.cancelOrder(order.getId())) {
                loadOrders(); // Refresh table
            }
        }
    }

    private void loadOrders() {
        if (currentUser != null) {
            List<Order> myOrders = orderDAO.getOrdersByCustomerId(currentUser.getId());
            ordersTable.setItems(FXCollections.observableArrayList(myOrders));
        }
    }

    @FXML private void handleClose() {
        ((Stage) ordersTable.getScene().getWindow()).close();
    }
    
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}