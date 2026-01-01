package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import services.RegistrationService;
import utils.InputValidation;

/**
 * RegisterController - Customer Registration Handler
 * 
 * Handles customer registration with validation using RegistrationService.
 * Displays error messages and navigates to login screen after successful registration.
 * 
 * @author Group04
 * @version 1.0
 */
public class RegisterController {

    // --- FXML Components ---
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField fullNameField; 
    @FXML private TextArea addressField;
    @FXML private TextField phoneField;
    @FXML private Label errorLabel;

    private RegistrationService registrationService;

    /**
     * Initialize controller
     * Creates RegistrationService instance and hides error label
     */
    @FXML
    private void initialize() {
        registrationService = new RegistrationService();
        if (errorLabel != null) {
            errorLabel.setVisible(false);
        }
    }

    /**
     * Handle registration button click
     * Validates input and registers new customer
     * 
     * @param event Button click event
     */
    @FXML
    private void handleRegister(ActionEvent event) {
        // Get input values
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String confirmPass = confirmPasswordField.getText().trim();
        String fullName = fullNameField.getText().trim();
        String address = addressField.getText().trim();
        String phone = phoneField.getText().trim();

        // 1. Validate Username
        String userError = InputValidation.validateUsername(username);
        if (userError != null) {
            showError(userError);
            return;
        }

        // 2. Validate Password
        String passError = InputValidation.validatePassword(password);
        if (passError != null) {
            showError(passError);
            return;
        }

        // 3. Confirm Password Match
        if (!password.equals(confirmPass)) {
            showError("Passwords do not match!");
            return;
        }

        // 4. Validate Full Name 
        if (fullName.isEmpty()) {
            showError("Full name cannot be empty!");
            return;
        }
        
        if (fullName.length() < 3) {
            showError("Full name must be at least 3 characters!");
            return;
        }
        
        if (!fullName.matches("^[a-zA-ZğüşıöçĞÜŞİÖÇ\\s]+$")) {
            showError("Full name can only contain letters and spaces!");
            return;
        }

        // 5. Validate Address
        String addrError = InputValidation.validateAddress(address);
        if (addrError != null) {
            showError(addrError);
            return;
        }

        // 6. Validate Phone
        String phError = InputValidation.validatePhone(phone);
        if (phError != null) {
            showError(phError);
            return;
        }

        // Attempt registration with FULL NAME
        boolean success = registrationService.registerCustomer(
            username, password, address, phone, fullName
        );

        if (success) {
            showSuccessAndExit();
        } else {
            showError("Username already exists or registration failed!");
        }
    }

    /**
     * Handle back button click
     * Returns to login screen
     * 
     * @param event Button click event
     */
    @FXML
    private void handleBack(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/Login.fxml"));
            Stage stage;
            if (event != null) {
                stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            } else {
                // Called programmatically (e.g., after successful registration)
                // Use a known control to get the current window
                if (usernameField != null && usernameField.getScene() != null) {
                    stage = (Stage) usernameField.getScene().getWindow();
                } else {
                    // Fallback: use any showing window
                    stage = (Stage) javafx.stage.Window.getWindows().stream()
                            .filter(javafx.stage.Window::isShowing)
                            .findFirst()
                            .orElseThrow(() -> new IllegalStateException("No showing window found"));
                }
            }

            stage.setScene(new javafx.scene.Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Could not return to login screen.");
        }
    }

    /**
     * Display error message
     * Shows error in label or alert dialog
     * 
     * @param message Error message to display
     */
    private void showError(String message) {
        if (errorLabel != null) {
            errorLabel.setText("* " + message);
            errorLabel.setTextFill(Color.web("#e74c3c"));
            errorLabel.setVisible(true);
        } else {
            // Fallback to Alert if label not available
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Registration Error");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        }
    }

    /**
     * Show success message and return to login
     * Displays success alert and navigates to login screen
     */
    private void showSuccessAndExit() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Registration Successful");
        alert.setHeaderText(null);
        alert.setContentText("Account created successfully! You can now login.");
        alert.showAndWait();
        
        // Return to login screen
        handleBack(null);
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
    Alert alert = new Alert(type);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
}

}