package controllers;

import javafx.animation.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import models.User;
import services.UserDAO;

/**
 * Edit Profile Controller
 * 
 * Features:
 * - View current profile information
 * - Edit address and phone
 * - Username and role are read-only
 * - Success/error animations
 * - Data validation
 * 
 * @author Group04
 * @version 1.0
 */
public class EditProfileController {
    
    @FXML private VBox profileBox;
    @FXML private TextField usernameField;
    @FXML private TextField roleField;
    @FXML private TextArea addressField;
    @FXML private TextField phoneField;
    @FXML private Label messageLabel;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    
    private User currentUser;
    private UserDAO userDAO;
    
    /**
     * Initialize - Called automatically after FXML is loaded
     */
    @FXML
    private void initialize() {
        userDAO = new UserDAO();
        messageLabel.setVisible(false);
        
        // Play entrance animation
        playEntranceAnimation();
    }
    
    /**
     * Set user data to edit
     * Called from CustomerMainController
     * 
     * @param user User to edit
     */
    public void setUser(User user) {
        this.currentUser = user;
        
        // Fill form with current data
        usernameField.setText(user.getUsername());
        roleField.setText(user.getRoleDisplayName());
        addressField.setText(user.getAddress() != null ? user.getAddress() : "");
        phoneField.setText(user.getPhone() != null ? user.getPhone() : "");
        
        System.out.println("✏️ Editing profile for: " + user.getUsername());
    }
    
    /**
     * Entrance animation
     */
    private void playEntranceAnimation() {
        profileBox.setOpacity(0);
        profileBox.setScaleX(0.85);
        profileBox.setScaleY(0.85);
        
        FadeTransition fade = new FadeTransition(Duration.millis(600), profileBox);
        fade.setFromValue(0);
        fade.setToValue(1);
        
        ScaleTransition scale = new ScaleTransition(Duration.millis(600), profileBox);
        scale.setFromX(0.85);
        scale.setFromY(0.85);
        scale.setToX(1.0);
        scale.setToY(1.0);
        
        ParallelTransition entrance = new ParallelTransition(fade, scale);
        entrance.setInterpolator(Interpolator.EASE_OUT);
        entrance.play();
    }
    
    /**
     * Handle Save Button
     */
    @FXML
    private void handleSave(ActionEvent event) {
        System.out.println("\n💾 Saving profile changes...");
        
        // Get new values
        String newAddress = addressField.getText().trim();
        String newPhone = phoneField.getText().trim();
        
        // Validation
        if (newAddress.isEmpty()) {
            showErrorMessage("Address cannot be empty!");
            return;
        }
        
        // Update user object
        currentUser.setAddress(newAddress);
        currentUser.setPhone(newPhone);
        
        // Save to database
        boolean success = userDAO.updateUser(currentUser);
        
        if (success) {
            System.out.println("Profile updated successfully!");
            
            // Show success message
            showSuccessMessage("Profile updated successfully!");
            
            // Close window after delay
            PauseTransition pause = new PauseTransition(Duration.millis(1500));
            pause.setOnFinished(e -> closeWindow());
            pause.play();
            
        } else {
            System.err.println("Failed to update profile!");
            showErrorMessage("Failed to update profile. Please try again.");
        }
    }
    
    /**
     * Handle Cancel Button
     */
    @FXML
    private void handleCancel(ActionEvent event) {
        // Confirm cancel
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Cancel Edit");
        confirm.setHeaderText("Discard changes?");
        confirm.setContentText("Are you sure you want to discard your changes?");
        
        if (confirm.showAndWait().get() == ButtonType.OK) {
            closeWindow();
        }
    }
    
    /**
     * Show success message with animation
     */
    private void showSuccessMessage(String message) {
        messageLabel.setText(message);
        messageLabel.setTextFill(javafx.scene.paint.Color.web("#27ae60"));
        messageLabel.setVisible(true);
        
        // Green flash on box
        Timeline greenFlash = new Timeline(
            new KeyFrame(Duration.ZERO, 
                new KeyValue(profileBox.styleProperty(), profileBox.getStyle())
            ),
            new KeyFrame(Duration.millis(300), 
                new KeyValue(profileBox.styleProperty(), 
                    "-fx-background-color: rgba(46, 204, 113, 0.15); " +
                    "-fx-background-radius: 25; " +
                    "-fx-border-color: #27ae60; " +
                    "-fx-border-width: 4; " +
                    "-fx-border-radius: 25;")
            ),
            new KeyFrame(Duration.millis(600), 
                new KeyValue(profileBox.styleProperty(), profileBox.getStyle())
            )
        );
        
        // Fade in message
        FadeTransition fade = new FadeTransition(Duration.millis(300), messageLabel);
        fade.setFromValue(0);
        fade.setToValue(1);
        
        ParallelTransition success = new ParallelTransition(greenFlash, fade);
        success.play();
    }
    
    /**
     * Show error message with animation
     */
    private void showErrorMessage(String message) {
        messageLabel.setText(message);
        messageLabel.setTextFill(javafx.scene.paint.Color.web("#e74c3c"));
        messageLabel.setVisible(true);
        
        // Shake animation
        TranslateTransition shake = new TranslateTransition(Duration.millis(50), messageLabel);
        shake.setFromX(0);
        shake.setByX(10);
        shake.setCycleCount(8);
        shake.setAutoReverse(true);
        
        // Fade in
        FadeTransition fade = new FadeTransition(Duration.millis(300), messageLabel);
        fade.setFromValue(0);
        fade.setToValue(1);
        
        // Red flash on box
        Timeline redFlash = new Timeline(
            new KeyFrame(Duration.ZERO, 
                new KeyValue(profileBox.styleProperty(), profileBox.getStyle())
            ),
            new KeyFrame(Duration.millis(200), 
                new KeyValue(profileBox.styleProperty(), 
                    "-fx-background-color: rgba(231, 76, 60, 0.15); " +
                    "-fx-background-radius: 25; " +
                    "-fx-border-color: #e74c3c; " +
                    "-fx-border-width: 4; " +
                    "-fx-border-radius: 25;")
            ),
            new KeyFrame(Duration.millis(400), 
                new KeyValue(profileBox.styleProperty(), profileBox.getStyle())
            )
        );
        
        ParallelTransition error = new ParallelTransition(shake, fade, redFlash);
        error.play();
    }
    
    /**
     * Close window with animation
     */
    private void closeWindow() {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(400), profileBox);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        
        ScaleTransition shrink = new ScaleTransition(Duration.millis(400), profileBox);
        shrink.setToX(0.85);
        shrink.setToY(0.85);
        
        ParallelTransition exit = new ParallelTransition(fadeOut, shrink);
        exit.setOnFinished(e -> {
            Stage stage = (Stage) saveButton.getScene().getWindow();
            stage.close();
        });
        
        exit.play();
    }
}