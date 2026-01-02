package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

// --- EXPLICIT JAVAFX IMPORTS ---
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DateCell;
import javafx.util.Callback;
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
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import javafx.collections.ObservableList;
import java.util.List;
import java.util.Collections;

/**
 * Controller for the shopping cart view.
 * Manages cart items, coupon application, and checkout flow.
 */
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
    @FXML private DatePicker deliveryDatePicker;
    @FXML private ComboBox<String> deliveryTimeCombo;
    @FXML private javafx.scene.control.ListView<String> userCouponsList; // User-owned coupons list
    @FXML private Label shippingLabel; // Shipping cost label
    @FXML private Label shippingNoteLabel; // Small note under shipping (e.g., "Free delivery")

    private User currentUser; 
    private double currentCouponRate = 0.0; // Track the applied discount percentage
    private double baseShipping = 20.0; // default shipping fee
    private boolean currentFreeShipping = false; // whether FREESHIP is applied

    @FXML
    private void initialize() {
        setupTable();
        refreshCart();
        displayAvailableCoupons(); // Call this to set the Tooltip
        setupDeliveryControls();
    }

    private void setupDeliveryControls() {
        // If controls didn't inject for some reason, skip setup to avoid NPE
        if (deliveryDatePicker == null || deliveryTimeCombo == null) return;

        ZoneId ist = ZoneId.of("Europe/Istanbul");
        LocalDateTime nowI = LocalDateTime.now(ist);

        // Populate time slots from 08:00 to 20:00 every 30 minutes
        ObservableList<String> slots = FXCollections.observableArrayList();
        for (int h = 8; h <= 20; h++) {
            slots.add(String.format("%02d:00", h));
            if (h != 20) slots.add(String.format("%02d:30", h));
        }
        deliveryTimeCombo.setItems(slots);

        // Limit DatePicker to today .. today+2
        LocalDate minDate = nowI.toLocalDate();
        LocalDate maxDate = nowI.plusHours(48).toLocalDate();
        deliveryDatePicker.setDayCellFactory(new Callback<DatePicker, DateCell>() {
            @Override public DateCell call(DatePicker param) {
                return new DateCell() {
                    @Override public void updateItem(LocalDate item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item.isBefore(minDate) || item.isAfter(maxDate)) {
                            setDisable(true);
                            setStyle("-fx-background-color: #eeeeee;");
                        }
                    }
                };
            }
        });

        // Set sensible defaults: now + 2 hours rounded to nearest slot
        LocalDateTime defaultDt = nowI.plusHours(2);
        // Round minutes to next 30-min slot
        int minute = defaultDt.getMinute();
        if (minute > 0 && minute <= 30) defaultDt = defaultDt.withMinute(30).withSecond(0).withNano(0);
        else if (minute > 30) defaultDt = defaultDt.plusHours(1).withMinute(0).withSecond(0).withNano(0);

        // If default time is before 08:00, move to 08:00; if after 20:00, move to next day 08:00
        if (defaultDt.getHour() < 8) {
            defaultDt = LocalDateTime.of(defaultDt.toLocalDate(), LocalTime.of(8,0));
        } else if (defaultDt.getHour() > 20 || (defaultDt.getHour() == 20 && defaultDt.getMinute() > 0)) {
            defaultDt = LocalDateTime.of(defaultDt.toLocalDate().plusDays(1), LocalTime.of(8,0));
        }

        deliveryDatePicker.setValue(defaultDt.toLocalDate());
        deliveryTimeCombo.setValue(String.format("%02d:%02d", defaultDt.getHour(), defaultDt.getMinute()));
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
        
        // Shipping (free if FREESHIP coupon applied OR subtotal >= 150 TL)
        boolean thresholdMet = subtotal >= 150.0;
        double shipping = (currentFreeShipping || thresholdMet) ? 0.0 : baseShipping;
        
        // 1. Automatic Discount (5% off if subtotal > 200 TL)
        double promoDiscount = (subtotal > 200) ? (subtotal * 0.05) : 0.0;
        
        // 2. Coupon Discount (Based on applied rate)
        double couponDiscount = subtotal * (currentCouponRate / 100.0);
        
        double totalDiscount = promoDiscount + couponDiscount;
        double finalTotal = subtotal + vat + shipping - totalDiscount;

        // Debug logs
        System.out.println("refreshCart: subtotal=" + subtotal + " vat=" + vat + " thresholdMet=" + thresholdMet + " currentFreeShipping=" + currentFreeShipping + " shipping=" + shipping + " totalDiscount=" + totalDiscount + " finalTotal=" + finalTotal);

        // Update UI Labels
        subtotalLabel.setText(String.format("%.2f ₺", subtotal));
        vatLabel.setText(String.format("%.2f ₺", vat));
        shippingLabel.setText(String.format("%.2f ₺", shipping));
        // Shipping note (coupon vs threshold)
        if (currentFreeShipping) {
            shippingNoteLabel.setText("Free shipping (coupon applied)");
        } else if (thresholdMet) {
            shippingNoteLabel.setText("Free delivery applied — orders ≥ 150 ₺");
        } else {
            shippingNoteLabel.setText("");
        }

        // Dynamic helper: show how much more to spend to get free delivery
        try {
            if (earnCouponsLabel != null) {
                if (thresholdMet) {
                    earnCouponsLabel.setText("You're eligible for Free Delivery — great! 🎉");
                } else {
                    double remaining = 150.0 - subtotal;
                    earnCouponsLabel.setText(String.format("Spend %.2f ₺ more to earn Free Delivery (FREESHIP)", remaining));
                }
            }
        } catch (Exception e) { e.printStackTrace(); }

        discountLabel.setText(String.format("-%.2f ₺", totalDiscount));
        totalLabel.setText(String.format("%.2f ₺", finalTotal));

        // Update UI Labels
        subtotalLabel.setText(String.format("%.2f ₺", subtotal));
        vatLabel.setText(String.format("%.2f ₺", vat));
        discountLabel.setText(String.format("-%.2f ₺", totalDiscount));
        totalLabel.setText(String.format("%.2f ₺", finalTotal));

        // Disable checkout if cart is empty
        checkoutButton.setDisable(items.isEmpty());
    }

    @FXML private javafx.scene.control.Label couponsStatusLabel; // Status below the coupons list
    @FXML private Label earnCouponsLabel; // Dynamic helper: how to earn coupons / free delivery
    @FXML private Button applyCouponButton; // Apply the selected coupon from the list (single-click/Enter)

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
                // Deduplicate by coupon code (in case DB has accidental duplicates)
                java.util.LinkedHashMap<String,String> codeMap = new java.util.LinkedHashMap<>();
                for (String s : couponsToShow) {
                    String code = s.split("\\s+")[0];
                    codeMap.putIfAbsent(code, s);
                }
                java.util.List<String> deduped = new java.util.ArrayList<>(codeMap.values());
                userCouponsList.setItems(FXCollections.observableArrayList(deduped));

                // Show count and the exact codes in the status label
                String joinedCodes = String.join(", ", codeMap.keySet());
                couponsStatusLabel.setText("You have " + codeMap.size() + " coupon(s): " + joinedCodes + ". Double-click to apply.");
                System.out.println("displayUserCoupons: rawCount=" + couponsToShow.size() + " uniqueCount=" + codeMap.size() + " codes=" + joinedCodes);

                // Prepare apply button state
                applyCouponButton.setDisable(true);

                // Selection change -> enable/disable apply button
                userCouponsList.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
                    applyCouponButton.setDisable(newV == null);
                });

                // Single-click or double-click behavior: single click enables, double-click applies
                userCouponsList.setOnMouseClicked(e -> {
                    String selected = userCouponsList.getSelectionModel().getSelectedItem();
                    applyCouponButton.setDisable(selected == null);
                    if (e.getClickCount() == 2) {
                        if (selected != null && !selected.isEmpty()) {
                            String code = selected.split("\\s+")[0]; // take first token as code
                            couponField.setText(code);
                            handleApplyCoupon();
                        }
                    }
                });

                // Enter key applies selected coupon
                userCouponsList.setOnKeyPressed(e -> {
                    switch (e.getCode()) {
                        case ENTER:
                            String selected = userCouponsList.getSelectionModel().getSelectedItem();
                            if (selected != null && !selected.isEmpty()) {
                                String code = selected.split("\\s+")[0];
                                couponField.setText(code);
                                handleApplyCoupon();
                            }
                            break;
                        default:
                            break;
                    }
                });
            } else {
                userCouponsList.setItems(FXCollections.observableArrayList());
                applyCouponButton.setDisable(true);
                couponsStatusLabel.setText("No coupons available.");
            }
        });
    }

    private void displayAvailableCoupons() {
        CouponDAO couponDAO = new CouponDAO();
        // Ensure our special coupons exist (in case DB wasn't seeded)
        couponDAO.ensureCouponExists("FREESHIP", 0.0);
        couponDAO.ensureCouponExists("LOYAL5", 5.0);

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
        // Fetch prior order count to determine "new user" status (used for SAVE20 awarding)
        int priorOrderCount = 0;
        try {
            priorOrderCount = orderDAO.getOrdersByCustomerId(currentUser.getId()).size();
        } catch (Exception ex) {
            System.out.println("handleCheckout: failed to get prior order count: " + ex.getMessage());
            ex.printStackTrace();
        }
        // Parse final total from label
        String totalStr = totalLabel.getText().replace(" ₺", "").replace(",", ".");
        double finalTotal = Double.parseDouble(totalStr);
        
        // Determine requested delivery LocalDateTime from picker/combo (Istanbul timezone)
        ZoneId ist = ZoneId.of("Europe/Istanbul");
        LocalDateTime nowI = LocalDateTime.now(ist);
        LocalDateTime requested = null;
        try {
            if (deliveryDatePicker != null && deliveryDatePicker.getValue() != null && deliveryTimeCombo != null && deliveryTimeCombo.getValue() != null) {
                LocalDate d = deliveryDatePicker.getValue();
                String t = deliveryTimeCombo.getValue();
                DateTimeFormatter tf = DateTimeFormatter.ofPattern("HH:mm");
                LocalTime lt = LocalTime.parse(t, tf);
                requested = LocalDateTime.of(d, lt);
            }
        } catch (Exception ex) { requested = null; }

        if (requested == null) {
            requested = nowI.plusHours(2);
            // adjust into working hours same as setup
            int minute = requested.getMinute();
            if (minute > 0 && minute <= 30) requested = requested.withMinute(30).withSecond(0).withNano(0);
            else if (minute > 30) requested = requested.plusHours(1).withMinute(0).withSecond(0).withNano(0);
            if (requested.getHour() < 8) requested = LocalDateTime.of(requested.toLocalDate(), LocalTime.of(8,0));
            else if (requested.getHour() > 20 || (requested.getHour() == 20 && requested.getMinute() > 0)) requested = LocalDateTime.of(requested.toLocalDate().plusDays(1), LocalTime.of(8,0));
        }

        // Validate within 48 hours
        long hrs = Duration.between(nowI, requested).toHours();
        if (hrs < 0 || hrs > 48) {
            showAlert("Please choose a delivery time within the next 48 hours.");
            return;
        }

        Order newOrder = new Order(0, currentUser.getId(), currentUser.getUsername(), 0, "CREATED", requested, finalTotal);
        
        boolean success = orderDAO.createOrder(newOrder, CartService.getCartItems());
        
        if (success) {
            CartService.clearCart();
            refreshCart();

            // Award SAVE20 coupon for large orders (>= 500 TL) but only to new users (no prior orders)
            CouponDAO couponDAO = new CouponDAO();
            boolean awarded = false;
            try {
                if (finalTotal >= 500.0 && priorOrderCount == 0) {
                    awarded = couponDAO.assignCouponToUserByCode(currentUser.getId(), "SAVE20", 20.0);
                }
            } catch (Exception ex) {
                // Non-fatal - log and continue
                System.out.println("Failed to award SAVE20: " + ex.getMessage());
                ex.printStackTrace();
            }

            // Award LOYAL5 when the user reaches multiples of 5 orders (5,10,15,...)
            boolean loyalAwarded = false;
            try {
                int orderCount = orderDAO.getOrdersByCustomerId(currentUser.getId()).size();
                if (orderCount > 0 && orderCount % 5 == 0) {
                    loyalAwarded = couponDAO.assignCouponToUserByCode(currentUser.getId(), "LOYAL5", 5.0);
                }
            } catch (Exception ex) {
                System.out.println("Failed to award LOYAL5: " + ex.getMessage());
                ex.printStackTrace();
            }

            String message = "Order placed successfully! 🎉\nEnjoy your fresh groceries!";
            if (awarded) message += "\nYou earned a coupon: SAVE20 (%20 OFF). Check 'Your Coupons' in the cart.";
            if (loyalAwarded) message += "\nYou earned a loyalty coupon: LOYAL5 (%5 OFF).";

            // If a coupon was applied by the user, mark it as redeemed (if it's an assigned coupon)
            try {
                String appliedCode = couponField.getText() == null ? "" : couponField.getText().trim().toUpperCase();
                if (!appliedCode.isEmpty()) {
                    CouponDAO cd = new CouponDAO();
                    boolean redeemed = cd.redeemUserCoupon(currentUser.getId(), appliedCode);
                    System.out.println("handleCheckout: attempted to redeem applied coupon='" + appliedCode + "' -> " + redeemed);
                    if (!redeemed) {
                        // If no user_coupons row existed (public/global coupon), assign it to the user then redeem
                        double rate = cd.getDiscountRate(appliedCode);
                        boolean assigned = cd.assignCouponToUserByCode(currentUser.getId(), appliedCode, rate);
                        System.out.println("handleCheckout: assigned applied coupon='" + appliedCode + "' -> " + assigned);
                        if (assigned) {
                            boolean redeemed2 = cd.redeemUserCoupon(currentUser.getId(), appliedCode);
                            System.out.println("handleCheckout: redeemed after assign='" + appliedCode + "' -> " + redeemed2);
                        }
                    }
                }
                // Also handle FREESHIP case if user had that assigned or not
                if (currentFreeShipping) {
                    CouponDAO cd = new CouponDAO();
                    boolean redeemed = cd.redeemUserCoupon(currentUser.getId(), "FREESHIP");
                    System.out.println("handleCheckout: attempted to redeem FREESHIP -> " + redeemed);
                    if (!redeemed) {
                        boolean assigned = cd.assignCouponToUserByCode(currentUser.getId(), "FREESHIP", 0.0);
                        System.out.println("handleCheckout: assigned FREESHIP -> " + assigned);
                        if (assigned) {
                            boolean redeemed2 = cd.redeemUserCoupon(currentUser.getId(), "FREESHIP");
                            System.out.println("handleCheckout: redeemed FREESHIP after assign -> " + redeemed2);
                        }
                    }
                }
            } catch (Exception ex) {
                System.out.println("Failed to redeem applied coupon: " + ex.getMessage());
                ex.printStackTrace();
            }

            showAlert(message);

            // Cleanup applied coupon state
            couponField.clear();
            currentCouponRate = 0.0;
            currentFreeShipping = false;
            displayUserCoupons();
            refreshCart();

            ((Stage) checkoutButton.getScene().getWindow()).close(); 
        } else {
            showAlert("Database error while creating order! ❌");
        }
    }

    @FXML
    private void handleApplyCoupon() {
        String inputCode = couponField.getText().trim().toUpperCase();
        if (inputCode.isEmpty()) {
            showAlert("Please enter a coupon code.");
            return;
        }

        CouponDAO couponDAO = new CouponDAO();
        // First, verify coupon exists and active
        boolean exists = couponDAO.couponExists(inputCode);
        if (!exists) {
            showAlert("Invalid or expired coupon code! ❌");
            return;
        }

        // Handle FREESHIP specially
        if (inputCode.equalsIgnoreCase("FREESHIP")) {
            currentFreeShipping = true;
            currentCouponRate = 0.0;
            // Remove applied coupon from UI list if present
            try {
                javafx.collections.ObservableList<String> items = userCouponsList.getItems();
                if (items != null) {
                    items.removeIf(s -> s != null && s.split("\\s+")[0].equalsIgnoreCase("FREESHIP"));
                    userCouponsList.setItems(items);
                }
            } catch (Exception ex) { /* ignore if list not present */ }
            refreshCart();
            showAlert("Free shipping applied! 🚚");
            return;
        }

        double rate = couponDAO.getDiscountRate(inputCode);
        // Some coupons may have 0% (like FREESHIP), but we already handled FREESHIP above
        if (rate > 0) {
            currentCouponRate = rate;
            currentFreeShipping = false;
            // Remove applied coupon from UI list if present
            try {
                javafx.collections.ObservableList<String> items = userCouponsList.getItems();
                if (items != null) {
                    items.removeIf(s -> {
                        if (s == null || s.isEmpty()) return false;
                        String code = s.split("\\s+")[0];
                        return code.equalsIgnoreCase(inputCode);
                    });
                    userCouponsList.setItems(items);
                    // update status label
                    couponsStatusLabel.setText("You have " + (items.size()) + " coupon(s).");
                }
            } catch (Exception ex) { /* ignore if list not present */ }

            refreshCart(); // Refresh the labels with new discount
            showAlert("Coupon applied successfully: %" + rate + " discount!");
        } else {
            // Coupon exists but offers no percent discount (inform the user)
            currentCouponRate = 0.0;
            currentFreeShipping = false;
            refreshCart();
            showAlert("Coupon applied (no percent discount).");
        }
    }

    @FXML
    private void handleApplySelectedCoupon(ActionEvent event) {
        String selected = userCouponsList.getSelectionModel().getSelectedItem();
        if (selected == null || selected.isEmpty()) return;
        String code = selected.split("\\s+")[0];
        couponField.setText(code);
        handleApplyCoupon();
    }
    
    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}