package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.User;
import services.AuthenticationService;

/**
 * Login Controller
 * @author Group04
 */
public class LoginController {
    
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private Button loginButton;
    @FXML private Button registerButton;
    
    private AuthenticationService authService;
    
    /**
     * Initialize - Called automatically
     */
    @FXML
    private void initialize() {
        authService = new AuthenticationService();
        errorLabel.setVisible(false);
    }
    
    /**
     * Login button handler
     */
    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        
        // Validation
        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter username and password!");
            return;
        }
        
        // Authenticate
        User user = authService.authenticate(username, password);
        
        if (user != null) {
            // Success - redirect based on role
            openRoleBasedWindow(user);
            closeWindow();
        } else {
            showError("Invalid username or password!");
        }
    }
    
    /**
     * Register button handler
     */
    @FXML
    private void handleRegister(ActionEvent event) {
        showAlert(Alert.AlertType.INFORMATION, "Register", 
                 "Registration feature coming soon!");
        // TODO: Open Register.fxml
    }
    
    /**
     * Open window based on user role
     */
    private void openRoleBasedWindow(User user) {
        try {
            String fxmlFile = "";
            String windowTitle = "";
            
            switch (user.getRole()) {
                case "customer":
                    fxmlFile = "/fxml/CustomerMain.fxml";
                    windowTitle = "Group04 GreenGrocer - Customer";
                    break;
                case "carrier":
                    fxmlFile = "/fxml/CarrierMain.fxml";
                    windowTitle = "Group04 GreenGrocer - Carrier";
                    break;
                case "owner":
                    fxmlFile = "/fxml/OwnerMain.fxml";
                    windowTitle = "Group04 GreenGrocer - Owner";
                    break;
                default:
                    showError("Unknown role: " + user.getRole());
                    return;
            }
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();
            
            // Pass user to controller
            if (user.getRole().equals("customer")) {
                CustomerMainController controller = loader.getController();
                controller.setUser(user);
            }
            
            Stage stage = new Stage();
            Scene scene = new Scene(root, 960, 540);
            scene.getStylesheets().add(
                getClass().getResource("/css/style.css").toExternalForm()
            );
            stage.setScene(scene);
            stage.setTitle(windowTitle);
            stage.centerOnScreen();
            stage.show();
            
        } catch (Exception e) {
            e.printStackTrace();
            showError("Error opening main window!");
        }
    }
    
    /**
     * Show error message
     */
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
    
    /**
     * Show alert dialog
     */
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    /**
     * Close login window
     */
    private void closeWindow() {
        Stage stage = (Stage) loginButton.getScene().getWindow();
        stage.close();
    }
}