package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

// --- EXPLICIT JAVAFX IMPORTS ---
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip; // NEW IMPORT
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import models.Order;
import models.OrderItem;
import models.User;
import services.CartService;
import services.OrderDAO;
import services.CouponDAO; 

import java.time.LocalDateTime;
import java.util.List;
import java.util.Collections;

public class ShoppingCartController {

    // --- FXML COMPONENTS ---
    @FXML private TableView<OrderItem> cartTable;
    @FXML private TableColumn<OrderItem, String> productColumn;
    @FXML private TableColumn<OrderItem, Double> quantityColumn;
    @FXML private TableColumn<OrderItem, Double> priceColumn;
    @FXML private TableColumn<OrderItem, Double> totalColumn;

    @FXML private Label subtotalLabel;
    @FXML private Label vatLabel;
    @FXML private Label discountLabel;
    @FXML private Label totalLabel;

    @FXML private Button checkoutButton;
    @FXML private Button removeButton;
    @FXML private Button continueShoppingButton;

    @FXML private TextField couponField; // Coupon input field
    @FXML private javafx.scene.control.ListView<String> userCouponsList; // User-owned coupons list

    private User currentUser; 
    private double currentCouponRate = 0.0; // Track the applied discount percentage

    @FXML
    private void initialize() {
        setupTable();
        refreshCart();
        displayAvailableCoupons(); // Call this to set the Tooltip
    }

    public void setUser(User user) {
        this.currentUser = user;
        System.out.println("ShoppingCartController.setUser: user=" + (user==null?"null":user.getUsername()) + " id=" + (user==null?"null":user.getId()));
        displayUserCoupons();
    }

    // Map table columns to the OrderItem model
    private void setupTable() {
        productColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("pricePerUnit"));
        totalColumn.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));

        // Cell formatting for kg and TL
        quantityColumn.setCellFactory(tc -> new TableCell<OrderItem, Double>() {
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("%.2f kg", item));
            }
        });

        priceColumn.setCellFactory(tc -> new TableCell<OrderItem, Double>() {
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("%.2f ₺", item));
            }
        });

        totalColumn.setCellFactory(tc -> new TableCell<OrderItem, Double>() {
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("%.2f ₺", item));
            }
        });
    }

    private void refreshCart() {
        // Fetch items from service
        List<OrderItem> items = CartService.getCartItems();
        ObservableList<OrderItem> observableItems = FXCollections.observableArrayList(items);
        cartTable.setItems(observableItems);
        
        // Calculations
        double subtotal = CartService.getTotal();
        double vatRate = 0.18; // 18% VAT
        double vat = subtotal * vatRate;
        
        // 1. Automatic Discount (5% off if subtotal > 200 TL)
        double promoDiscount = (subtotal > 200) ? (subtotal * 0.05) : 0.0;
        
        // 2. Coupon Discount (Based on applied rate)
        double couponDiscount = subtotal * (currentCouponRate / 100.0);
        
        double totalDiscount = promoDiscount + couponDiscount;
        double finalTotal = subtotal + vat - totalDiscount;

        // Update UI Labels
        subtotalLabel.setText(String.format("%.2f ₺", subtotal));
        vatLabel.setText(String.format("%.2f ₺", vat));
        discountLabel.setText(String.format("-%.2f ₺", totalDiscount));
        totalLabel.setText(String.format("%.2f ₺", finalTotal));

        // Disable checkout if cart is empty
        checkoutButton.setDisable(items.isEmpty());
    }

    @FXML private javafx.scene.control.Label couponsStatusLabel; // Status below the coupons list

    private void displayUserCoupons() {
        if (currentUser == null || userCouponsList == null) return;
        CouponDAO couponDAO = new CouponDAO();

        System.out.println("displayUserCoupons: fetching for userId=" + currentUser.getId());
        // Try to fetch user coupons
        List<String> userCoupons = couponDAO.getCouponsForUser(currentUser.getId());
        System.out.println("displayUserCoupons: found " + (userCoupons == null ? 0 : userCoupons.size()) + " coupons initially");

        // If user has none, ensure WELCOME10 is created and assigned, then re-fetch
        if (userCoupons == null || userCoupons.isEmpty()) {
            boolean assigned = couponDAO.ensureWelcomeAssigned(currentUser.getId());
            System.out.println("ensureWelcomeAssigned returned: " + assigned);
            userCoupons = couponDAO.getCouponsForUser(currentUser.getId());
            System.out.println("displayUserCoupons: found " + (userCoupons == null ? 0 : userCoupons.size()) + " coupons after ensure");
        }

        // Prepare final collection for use in lambdas
        final List<String> couponsToShow = (userCoupons == null ? Collections.emptyList() : userCoupons);

        // Update UI on FX thread
        javafx.application.Platform.runLater(() -> {
            if (!couponsToShow.isEmpty()) {
                userCouponsList.setItems(FXCollections.observableArrayList(couponsToShow));
                couponsStatusLabel.setText("You have " + couponsToShow.size() + " coupon(s). Double-click to apply.");

                // Double-click to apply a coupon
                userCouponsList.setOnMouseClicked(e -> {
                    if (e.getClickCount() == 2) {
                        String selected = userCouponsList.getSelectionModel().getSelectedItem();
                        if (selected != null && !selected.isEmpty()) {
                            String code = selected.split("\\s+")[0]; // take first token as code
                            couponField.setText(code);
                            handleApplyCoupon();
                        }
                    }
                });
            } else {
                userCouponsList.setItems(FXCollections.observableArrayList());
                couponsStatusLabel.setText("No coupons available.");
            }
        });
    }

    private void displayAvailableCoupons() {
        CouponDAO couponDAO = new CouponDAO();
        List<String> available = couponDAO.getAllActiveCoupons();
        System.out.println("displayAvailableCoupons: found " + (available==null?0:available.size()) + " active coupons");
        if (available != null && !available.isEmpty()) {
            String couponList = String.join("\n", available);
            // Set a tooltip to the coupon text field so user can see codes by hovering
            Tooltip tooltip = new Tooltip("Available Codes:\n" + couponList);
            // Optional: make it appear faster
            tooltip.setShowDelay(javafx.util.Duration.millis(200));
            couponField.setTooltip(tooltip);
        }
    }

    // --- BUTTON ACTIONS ---

    @FXML
    private void handleRemove(ActionEvent event) {
        OrderItem selected = cartTable.getSelectionModel().getSelectedItem();
        
        if (selected != null) {
            CartService.getCartItems().remove(selected);
            refreshCart();
        } else {
            showAlert("Please select an item to remove.");
        }
    }
    
    @FXML
    private void handleContinueShopping(ActionEvent event) {
        // Closes the window to return to main store
        ((Stage) continueShoppingButton.getScene().getWindow()).close();
    }

    @FXML
    private void handleCheckout(ActionEvent event) {
        if (CartService.getCartItems().isEmpty()) {
            showAlert("Your cart is empty!");
            return;
        }
        
        if (currentUser == null) {
            showAlert("User session error! Please log in again.");
            return;
        }

        OrderDAO orderDAO = new OrderDAO();
        // Parse final total from label
        String totalStr = totalLabel.getText().replace(" ₺", "").replace(",", ".");
        double finalTotal = Double.parseDouble(totalStr);
        
        Order newOrder = new Order(0, currentUser.getId(), currentUser.getUsername(), 0, "CREATED", LocalDateTime.now(), finalTotal);
        
        boolean success = orderDAO.createOrder(newOrder, CartService.getCartItems());
        
        if (success) {
            CartService.clearCart();
            refreshCart();

            // Award SAVE20 coupon for large orders (>= 500 TL)
            CouponDAO couponDAO = new CouponDAO();
            boolean awarded = false;
            try {
                if (finalTotal >= 500.0) {
                    awarded = couponDAO.assignCouponToUserByCode(currentUser.getId(), "SAVE20", 20.0);
                }
            } catch (Exception ex) {
                // Non-fatal - log and continue
                System.out.println("Failed to award SAVE20: " + ex.getMessage());
                ex.printStackTrace();
            }

            String message = "Order placed successfully! 🎉\nEnjoy your fresh groceries!";
            if (awarded) message += "\nYou earned a coupon: SAVE20 (%20 OFF). Check 'Your Coupons' in the cart.";

            showAlert(message);
            ((Stage) checkoutButton.getScene().getWindow()).close(); 
        } else {
            showAlert("Database error while creating order! ❌");
        }
    }

    @FXML
    private void handleApplyCoupon() {
        String inputCode = couponField.getText().trim();
        if (inputCode.isEmpty()) {
            showAlert("Please enter a coupon code.");
            return;
        }

        CouponDAO couponDAO = new CouponDAO();
        double rate = couponDAO.getDiscountRate(inputCode);

        if (rate > 0) {
            currentCouponRate = rate;
            refreshCart(); // Refresh the labels with new discount
            showAlert("Coupon applied successfully: %" + rate + " discount!");
        } else {
            currentCouponRate = 0.0;
            refreshCart();
            showAlert("Invalid or expired coupon code! ❌");
        }
    }
    
    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}