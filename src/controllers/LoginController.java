package controllers;

// --- DÜZELTME 1: YANLIŞ IMPORT SİLİNDİ ---
// import java.lang.classfile.Label;  <-- BU SATIR HATALIYDI, SİLDİK.

import javafx.animation.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*; // Doğru Label bunun içinde zaten var
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import models.User;
import services.AuthenticationService;

/**
 * Enhanced Login Controller with Advanced Animations
 * Group: Group04
 */
public class LoginController {
    
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel; // Artık doğru Label (JavaFX) kullanılıyor
    @FXML private Button loginButton;
    @FXML private Button registerButton;
    @FXML private VBox loginBox;
    
    private AuthenticationService authService;
    private Timeline loadingAnimation;
    
    @FXML
    private void initialize() {
        authService = new AuthenticationService();
        errorLabel.setVisible(false);
        playEntranceAnimation();
        playFloatingEffect();
    }
    
    private void playEntranceAnimation() {
        loginBox.setOpacity(0);
        loginBox.setScaleX(0.5);
        loginBox.setScaleY(0.5);
        loginBox.setRotate(-15);
        
        FadeTransition fadeIn = new FadeTransition(Duration.millis(1000), loginBox);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        
        ScaleTransition scaleUp = new ScaleTransition(Duration.millis(1000), loginBox);
        scaleUp.setFromX(0.5);
        scaleUp.setFromY(0.5);
        scaleUp.setToX(1);
        scaleUp.setToY(1);
        
        RotateTransition rotate = new RotateTransition(Duration.millis(1000), loginBox);
        rotate.setFromAngle(-15);
        rotate.setToAngle(0);
        
        ParallelTransition entrance = new ParallelTransition(fadeIn, scaleUp, rotate);
        entrance.setInterpolator(Interpolator.EASE_OUT);
        entrance.play();
        
        animateField(usernameField, 400);
        animateField(passwordField, 600);
        playButtonEntranceAnimation(loginButton, 800);
        playButtonEntranceAnimation(registerButton, 1000);
    }
    
    private void animateField(Control field, int delay) {
        field.setOpacity(0);
        field.setTranslateX(-50);
        
        FadeTransition fade = new FadeTransition(Duration.millis(600), field);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.setDelay(Duration.millis(delay));
        
        TranslateTransition slide = new TranslateTransition(Duration.millis(600), field);
        slide.setFromX(-50);
        slide.setToX(0);
        slide.setDelay(Duration.millis(delay));
        
        ParallelTransition animation = new ParallelTransition(fade, slide);
        animation.setInterpolator(Interpolator.EASE_OUT);
        animation.play();
    }
    
    private void playButtonEntranceAnimation(Button button, int delay) {
        button.setOpacity(0);
        button.setTranslateY(30);
        button.setScaleX(0.8);
        button.setScaleY(0.8);
        
        FadeTransition fade = new FadeTransition(Duration.millis(700), button);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.setDelay(Duration.millis(delay));
        
        TranslateTransition slide = new TranslateTransition(Duration.millis(700), button);
        slide.setFromY(30);
        slide.setToY(0);
        slide.setDelay(Duration.millis(delay));
        
        ScaleTransition scale = new ScaleTransition(Duration.millis(700), button);
        scale.setFromX(0.8);
        scale.setFromY(0.8);
        scale.setToX(1.0);
        scale.setToY(1.0);
        scale.setDelay(Duration.millis(delay));
        
        ParallelTransition animation = new ParallelTransition(fade, slide, scale);
        animation.setInterpolator(Interpolator.EASE_OUT);
        
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
    
    private void playFloatingEffect() {
        Timeline float_anim = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(loginBox.translateYProperty(), 0)),
            new KeyFrame(Duration.seconds(3), new KeyValue(loginBox.translateYProperty(), -8, Interpolator.EASE_BOTH)),
            new KeyFrame(Duration.seconds(6), new KeyValue(loginBox.translateYProperty(), 0, Interpolator.EASE_BOTH))
        );
        float_anim.setCycleCount(Timeline.INDEFINITE);
        float_anim.play();
    }
    
    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        
        errorLabel.setVisible(false);
        
        if (username.isEmpty() || password.isEmpty()) {
            showErrorWithAnimation("Please enter username and password!");
            return;
        }
        
        playLoadingAnimation(loginButton);
        
        PauseTransition pause = new PauseTransition(Duration.millis(800));
        pause.setOnFinished(e -> {
            User user = authService.authenticate(username, password);
            
            if (user != null) {
                playSuccessAnimation(() -> {
                    openRoleBasedWindow(user);
                    closeWindowWithAnimation();
                });
            } else {
                showErrorWithAnimation("Invalid username or password!");
                stopLoadingAnimation(loginButton, "Login");
            }
        });
        pause.play();
    }
    
    private void playLoadingAnimation(Button button) {
        button.setDisable(true);
        button.setText("🔄 Loading...");
        
        ScaleTransition pulse = new ScaleTransition(Duration.millis(500), button);
        pulse.setFromX(1.0);
        pulse.setFromY(1.0);
        pulse.setToX(1.05);
        pulse.setToY(1.05);
        pulse.setAutoReverse(true);
        pulse.setCycleCount(Timeline.INDEFINITE);
        
        RotateTransition rotate = new RotateTransition(Duration.millis(1000), button);
        rotate.setByAngle(360);
        rotate.setCycleCount(Timeline.INDEFINITE);
        
        ParallelTransition loading = new ParallelTransition(pulse, rotate);
        loading.play();
        
        loadingAnimation = new Timeline();
        loadingAnimation.getKeyFrames().add(new KeyFrame(Duration.millis(10), ae -> {}));
    }
    
    private void stopLoadingAnimation(Button button, String text) {
        button.setDisable(false);
        button.setText(text);
        button.setRotate(0);
        button.setScaleX(1.0);
        button.setScaleY(1.0);
    }
    
    private void playSuccessAnimation(Runnable onFinish) {
        Timeline greenFlash = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(loginBox.styleProperty(), loginBox.getStyle())),
            new KeyFrame(Duration.millis(300), new KeyValue(loginBox.styleProperty(), loginBox.getStyle() + "-fx-background-color: rgba(46, 204, 113, 0.3);")),
            new KeyFrame(Duration.millis(600), new KeyValue(loginBox.styleProperty(), loginBox.getStyle()))
        );
        
        ScaleTransition pulse = new ScaleTransition(Duration.millis(300), loginBox);
        pulse.setToX(1.08);
        pulse.setToY(1.08);
        pulse.setAutoReverse(true);
        pulse.setCycleCount(2);
        
        SequentialTransition success = new SequentialTransition(new ParallelTransition(greenFlash, pulse));
        success.setOnFinished(e -> {
            if (onFinish != null) onFinish.run();
        });
        success.play();
    }
    
    private void showErrorWithAnimation(String message) {
        errorLabel.setText("*" + message);
        errorLabel.setVisible(true);
        
        TranslateTransition shake = new TranslateTransition(Duration.millis(50), errorLabel);
        shake.setFromX(0);
        shake.setByX(10);
        shake.setCycleCount(8);
        shake.setAutoReverse(true);
        
        FadeTransition fade = new FadeTransition(Duration.millis(300), errorLabel);
        fade.setFromValue(0);
        fade.setToValue(1);
        
        TranslateTransition boxShake = new TranslateTransition(Duration.millis(50), loginBox);
        boxShake.setFromX(0);
        boxShake.setByX(5);
        boxShake.setCycleCount(6);
        boxShake.setAutoReverse(true);
        
        Timeline redFlash = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(loginBox.styleProperty(), loginBox.getStyle())),
            new KeyFrame(Duration.millis(200), new KeyValue(loginBox.styleProperty(), loginBox.getStyle() + "-fx-background-color: rgba(231, 76, 60, 0.2);")),
            new KeyFrame(Duration.millis(400), new KeyValue(loginBox.styleProperty(), loginBox.getStyle()))
        );
        
        ParallelTransition error = new ParallelTransition(shake, fade, boxShake, redFlash);
        error.play();
    }
    
    @FXML
    private void handleRegister(ActionEvent event) {
        ScaleTransition click = new ScaleTransition(Duration.millis(100), registerButton);
        click.setToX(0.95);
        click.setToY(0.95);
        click.setAutoReverse(true);
        click.setCycleCount(2);
        click.play();
        
        showAlert(Alert.AlertType.INFORMATION, "Register", 
                 "Registration feature coming soon!\n\nDefault users:\n" +
                 "Customer: cust/cust\nCarrier: carr/carr\nOwner: own/own");
    }
    
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
            }
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();
            
            // --- DÜZELTME 2: Controller sınıfı yoksa hata vermesin diye burayı geçici olarak kapattım ---
            // Eğer "CustomerMainController" sınıfını oluşturduysan bu yorumu açabilirsin.
            /* if (user.getRole().equals("customer")) {
                 // Burayı kullanmak için import controllers.CustomerMainController; eklemelisin
                 // CustomerMainController controller = loader.getController();
                 // controller.setUser(user);
            }
            */
            // -----------------------------------------------------------------------------------------
            
            Stage stage = new Stage();
            Scene scene = new Scene(root, 960, 540);
            
            // CSS dosyasının varlığından emin ol
            if (getClass().getResource("/css/style.css") != null) {
                scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            }

            // Animasyonlar...
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
            
            stage.setScene(scene);
            stage.setTitle(windowTitle);
            stage.centerOnScreen();
            stage.show();
            
            entrance.play();
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", 
                     "Could not open window for role: " + user.getRole());
        }
    }
    
    private void closeWindowWithAnimation() {
        Stage stage = (Stage) loginButton.getScene().getWindow();
        
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
    
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        
        alert.setOnShown(e -> {
            // --- DÜZELTME 3: Senin yaptığın düzeltmeyi koruduk ---
            alert.getDialogPane().setOpacity(0.0);
            FadeTransition fade = new FadeTransition(Duration.millis(300), alert.getDialogPane());
            fade.setFromValue(0);
            fade.setToValue(1);
            fade.play();
        });
        
        alert.showAndWait();
    }
}