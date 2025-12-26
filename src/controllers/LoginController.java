package controllers;

import javafx.animation.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*; 
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import models.User;
import services.AuthenticationService;

/**
 * Login Controller with Spectacular Animations
 * 
 * Features:
 * - Entrance animations (scale + fade + rotate)
 * - Floating effect on login box
 * - Error shake animation with red flash
 * - Success pulse animation with green flash
 * - Smooth window transitions
 * - Loading animation
 * 
 * @author Group04
 * @version 1.0
 */
public class LoginController {
    
    @FXML private AnchorPane rootPane;
    @FXML private VBox loginBox;
    @FXML private Label titleLabel;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private Button loginButton;
    @FXML private Button registerButton;
    
    private AuthenticationService authService;
    private ParallelTransition loadingAnimation;
    
    /**
     * Initialize - Called automatically after FXML is loaded
     */
    @FXML
    private void initialize() {
        authService = new AuthenticationService();
        errorLabel.setVisible(false);
        
        // Start spectacular entrance animation
        playEntranceAnimation();
        
        // Add floating effect
        playFloatingEffect();
    }
    
    /**
     * Spectacular entrance animation
     * Combines: Scale, Fade, Rotate
     */
    private void playEntranceAnimation() {
        // Initial state: invisible, small, rotated
        loginBox.setOpacity(0);
        loginBox.setScaleX(0.5);
        loginBox.setScaleY(0.5);
        loginBox.setRotate(-15);
        
        // Fade in animation
        FadeTransition fadeIn = new FadeTransition(Duration.millis(1200), loginBox);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        
        // Scale up animation
        ScaleTransition scaleUp = new ScaleTransition(Duration.millis(1200), loginBox);
        scaleUp.setFromX(0.5);
        scaleUp.setFromY(0.5);
        scaleUp.setToX(1.0);
        scaleUp.setToY(1.0);
        
        // Rotate animation
        RotateTransition rotate = new RotateTransition(Duration.millis(1200), loginBox);
        rotate.setFromAngle(-15);
        rotate.setToAngle(0);
        
        // Combine all animations
        ParallelTransition entrance = new ParallelTransition(fadeIn, scaleUp, rotate);
        entrance.setInterpolator(Interpolator.EASE_OUT);
        entrance.play();
        
        // Animate form fields with delay
        animateField(usernameField, 500);
        animateField(passwordField, 700);
        
        // Animate buttons with bounce
        animateButton(loginButton, 900);
        animateButton(registerButton, 1100);
    }
    
    /**
     * Animate input field with slide and fade
     */
    private void animateField(Control field, int delayMs) {
        field.setOpacity(0);
        field.setTranslateX(-50);
        
        FadeTransition fade = new FadeTransition(Duration.millis(600), field);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.setDelay(Duration.millis(delayMs));
        
        TranslateTransition slide = new TranslateTransition(Duration.millis(600), field);
        slide.setFromX(-50);
        slide.setToX(0);
        slide.setDelay(Duration.millis(delayMs));
        
        ParallelTransition animation = new ParallelTransition(fade, slide);
        animation.setInterpolator(Interpolator.EASE_OUT);
        animation.play();
    }
    
    /**
     * Animate button with bounce effect
     */
    private void animateButton(Button button, int delayMs) {
        button.setOpacity(0);
        button.setTranslateY(30);
        button.setScaleX(0.8);
        button.setScaleY(0.8);
        
        FadeTransition fade = new FadeTransition(Duration.millis(700), button);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.setDelay(Duration.millis(delayMs));
        
        TranslateTransition slide = new TranslateTransition(Duration.millis(700), button);
        slide.setFromY(30);
        slide.setToY(0);
        slide.setDelay(Duration.millis(delayMs));
        
        ScaleTransition scale = new ScaleTransition(Duration.millis(700), button);
        scale.setFromX(0.8);
        scale.setFromY(0.8);
        scale.setToX(1.0);
        scale.setToY(1.0);
        scale.setDelay(Duration.millis(delayMs));
        
        ParallelTransition animation = new ParallelTransition(fade, slide, scale);
        animation.setInterpolator(Interpolator.EASE_OUT);
        
        // Add bounce at end
        animation.setOnFinished(e -> {
            ScaleTransition bounce = new ScaleTransition(Duration.millis(300), button);
            bounce.setToX(1.1);
            bounce.setToY(1.1);
            bounce.setAutoReverse(true);
            bounce.setCycleCount(2);
            bounce.play();
        });
        
        animation.play();
    }
    
    /**
     * Continuous floating effect
     */
    private void playFloatingEffect() {
        Timeline floatAnimation = new Timeline(
            new KeyFrame(Duration.ZERO, 
                new KeyValue(loginBox.translateYProperty(), 0, Interpolator.EASE_BOTH)
            ),
            new KeyFrame(Duration.seconds(3), 
                new KeyValue(loginBox.translateYProperty(), -8, Interpolator.EASE_BOTH)
            ),
            new KeyFrame(Duration.seconds(6), 
                new KeyValue(loginBox.translateYProperty(), 0, Interpolator.EASE_BOTH)
            )
        );
        floatAnimation.setCycleCount(Timeline.INDEFINITE);
        floatAnimation.play();
    }
    
    /**
     * Handle Login Button Click
     */
    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        
        // Clear previous error
        errorLabel.setVisible(false);
        
        // Validation
        if (username.isEmpty() || password.isEmpty()) {
            showErrorWithAnimation(" Please enter username and password!");
            return;
        }
        
        // Show loading animation
        playLoadingAnimation();
        
        // Simulate network delay (remove in production)
        PauseTransition pause = new PauseTransition(Duration.millis(800));
        pause.setOnFinished(e -> {
            // Authenticate user
            User user = authService.authenticate(username, password);
            
            if (user != null) {
                // Success - play animation and open main window
                playSuccessAnimation(() -> {
                    openRoleBasedWindow(user);
                    closeWindowWithAnimation();
                });
            } else {
                // Failed - show error
                showErrorWithAnimation("Invalid username or password!");
                stopLoadingAnimation();
            }
        });
        pause.play();
    }
    
    /**
     * Loading animation - Pulse and rotate button
     */
    private void playLoadingAnimation() {
        loginButton.setDisable(true);
        loginButton.setText("🔄 Loading...");
        
        // Pulse effect
        ScaleTransition pulse = new ScaleTransition(Duration.millis(500), loginButton);
        pulse.setFromX(1.0);
        pulse.setFromY(1.0);
        pulse.setToX(1.05);
        pulse.setToY(1.05);
        pulse.setAutoReverse(true);
        pulse.setCycleCount(Timeline.INDEFINITE);
        
        // Rotation effect
        RotateTransition rotate = new RotateTransition(Duration.millis(1000), loginButton);
        rotate.setByAngle(360);
        rotate.setCycleCount(Timeline.INDEFINITE);
        
        loadingAnimation = new ParallelTransition(pulse, rotate);
        loadingAnimation.play();
    }
    
    /**
     * Stop loading animation
     */
    private void stopLoadingAnimation() {
        if (loadingAnimation != null) {
            loadingAnimation.stop();
        }
        loginButton.setDisable(false);
        loginButton.setText("Login");
        loginButton.setRotate(0);
        loginButton.setScaleX(1.0);
        loginButton.setScaleY(1.0);
    }
    
    /**
     * Success animation - Green flash and pulse
     */
    private void playSuccessAnimation(Runnable onFinish) {
        // Green flash on box
        Timeline greenFlash = new Timeline(
            new KeyFrame(Duration.ZERO, 
                new KeyValue(loginBox.styleProperty(), loginBox.getStyle())
            ),
            new KeyFrame(Duration.millis(300), 
                new KeyValue(loginBox.styleProperty(), 
                    "-fx-background-color: rgba(46, 204, 113, 0.3); " +
                    "-fx-background-radius: 20; " +
                    "-fx-border-color: #27ae60; " +
                    "-fx-border-width: 3; " +
                    "-fx-border-radius: 20;")
            ),
            new KeyFrame(Duration.millis(600), 
                new KeyValue(loginBox.styleProperty(), loginBox.getStyle())
            )
        );
        
        // Pulse effect
        ScaleTransition pulse = new ScaleTransition(Duration.millis(300), loginBox);
        pulse.setToX(1.08);
        pulse.setToY(1.08);
        pulse.setAutoReverse(true);
        pulse.setCycleCount(2);
        
        SequentialTransition success = new SequentialTransition(
            new ParallelTransition(greenFlash, pulse)
        );
        
        success.setOnFinished(e -> {
            if (onFinish != null) onFinish.run();
        });
        
        success.play();
    }
    
    /**
     * Error animation - Red flash and shake
     */
    private void showErrorWithAnimation(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        
        // Horizontal shake on error label
        TranslateTransition shake = new TranslateTransition(Duration.millis(50), errorLabel);
        shake.setFromX(0);
        shake.setByX(10);
        shake.setCycleCount(8);
        shake.setAutoReverse(true);
        
        // Fade in error label
        FadeTransition fade = new FadeTransition(Duration.millis(300), errorLabel);
        fade.setFromValue(0);
        fade.setToValue(1);
        
        // Shake login box
        TranslateTransition boxShake = new TranslateTransition(Duration.millis(50), loginBox);
        boxShake.setFromX(0);
        boxShake.setByX(5);
        boxShake.setCycleCount(6);
        boxShake.setAutoReverse(true);
        
        // Red flash on login box
        Timeline redFlash = new Timeline(
            new KeyFrame(Duration.ZERO, 
                new KeyValue(loginBox.styleProperty(), loginBox.getStyle())
            ),
            new KeyFrame(Duration.millis(200), 
                new KeyValue(loginBox.styleProperty(), 
                    "-fx-background-color: rgba(231, 76, 60, 0.2); " +
                    "-fx-background-radius: 20; " +
                    "-fx-border-color: #e74c3c; " +
                    "-fx-border-width: 3; " +
                    "-fx-border-radius: 20;")
            ),
            new KeyFrame(Duration.millis(400), 
                new KeyValue(loginBox.styleProperty(), loginBox.getStyle())
            )
        );
        
        ParallelTransition errorAnimation = new ParallelTransition(
            shake, fade, boxShake, redFlash
        );
        errorAnimation.play();
    }
    
    /**
     * Handle Register Button
     */
    @FXML
    private void handleRegister(ActionEvent event) {
        // Button click animation
        ScaleTransition click = new ScaleTransition(Duration.millis(100), registerButton);
        click.setToX(0.95);
        click.setToY(0.95);
        click.setAutoReverse(true);
        click.setCycleCount(2);
        click.play();
        
        // Show info dialog
        showAlert(Alert.AlertType.INFORMATION, "Register", 
                 "Registration Feature\n\n" +
                 "Registration will be available soon!\n\n" +
                 "Test Users:\n" +
                 "• Customer: cust / cust\n" +
                 "• Carrier: carr / carr\n" +
                 "• Owner: own / own");
    }
    
    /**
     * Open appropriate window based on user role
     */
    private void openRoleBasedWindow(User user) {
        try {
            String fxmlFile = "";
            String windowTitle = "";
            
            // Determine which FXML to load
            switch (user.getRole()) {
                case "customer":
                    fxmlFile = "/fxml/CustomerMain.fxml";
                    windowTitle = "Group04 GreenGrocer - Customer Portal";
                    break;
                case "carrier":
                    fxmlFile = "/fxml/CarrierMain.fxml";
                    windowTitle = "Group04 GreenGrocer - Carrier Dashboard";
                    break;
                case "owner":
                    fxmlFile = "/fxml/OwnerMain.fxml";
                    windowTitle = "Group04 GreenGrocer - Owner Panel";
                    break;
                default:
                    showAlert(Alert.AlertType.ERROR, "Error", 
                             "Unknown user role: " + user.getRole());
                    return;
            }
            

            // Pass user to controller if customer
            // Load FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();
            
        
            if (user.getRole().equals("customer")) {
                CustomerMainController controller = loader.getController();
                controller.setUser(user);
            }
            //KURYE KONTROLÜ 
            else if (user.getRole().equals("carrier")) {
                CarrierMainController controller = loader.getController();
                controller.setUser(user); // Kurye bilgisini içeri atıyoruz
            }
            // ---------------------------------------------
            
            // Create new stage
            Stage stage = new Stage();
            Scene scene = new Scene(root, 960, 540);
            
            // Add CSS
            scene.getStylesheets().add(
                getClass().getResource("/css/style.css").toExternalForm()
            );
            
            // Entrance animation for new window
            root.setOpacity(0);
            root.setScaleX(0.8);
            root.setScaleY(0.8);
            
            FadeTransition fadeIn = new FadeTransition(Duration.millis(800), root);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            
            ScaleTransition scaleIn = new ScaleTransition(Duration.millis(800), root);
            scaleIn.setFromX(0.8);
            scaleIn.setFromY(0.8);
            scaleIn.setToX(1.0);
            scaleIn.setToY(1.0);
            
            ParallelTransition entrance = new ParallelTransition(fadeIn, scaleIn);
            entrance.setInterpolator(Interpolator.EASE_OUT);
            
            // Show stage
            stage.setScene(scene);
            stage.setTitle(windowTitle);
            stage.centerOnScreen();
            stage.setResizable(true);
            stage.show();
            
            // Play entrance animation
            entrance.play();
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", 
                     "Could not open window!\n\n" + e.getMessage());
        }
    }
    
    /**
     * Close login window with animation
     */
    private void closeWindowWithAnimation() {
        Stage stage = (Stage) loginButton.getScene().getWindow();
        
        // Fade out and shrink
        FadeTransition fadeOut = new FadeTransition(Duration.millis(500), loginBox);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        
        ScaleTransition shrink = new ScaleTransition(Duration.millis(500), loginBox);
        shrink.setToX(0.5);
        shrink.setToY(0.5);
        
        RotateTransition rotate = new RotateTransition(Duration.millis(500), loginBox);
        rotate.setToAngle(15);
        
        ParallelTransition exit = new ParallelTransition(fadeOut, shrink, rotate);
        exit.setOnFinished(e -> stage.close());
        exit.play();
    }
    
    /**
     * Show alert dialog with animation
     */
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        
        // Animate alert appearance
        alert.setOnShown(e -> {
            alert.getDialogPane().setOpacity(0);
            FadeTransition fade = new FadeTransition(
                Duration.millis(300), 
                alert.getDialogPane()
            );
            fade.setFromValue(0);
            fade.setToValue(1);
            fade.play();
        });
        
        alert.showAndWait();
    }
}