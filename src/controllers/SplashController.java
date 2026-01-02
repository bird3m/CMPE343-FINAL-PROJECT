package controllers;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;

public class SplashController {
    
    @FXML private HBox fruitsRow1;
    @FXML private HBox fruitsRow2;
    @FXML private Label titleLabel;
    @FXML private Label loadingLabel;
    @FXML private Label dot1, dot2, dot3;
    @FXML private Circle circle1, circle2, circle3, circle4;
    
    @FXML
    private void initialize() {
        // Animate fruits - Row 1 (bounce up)
        animateFruits(fruitsRow1, -40, 0);
        
        // Animate fruits - Row 2 (bounce down) 
        animateFruits(fruitsRow2, 40, 200);
        
        // Floating circles
        animateCircles();
        
        // Title zoom in
        ScaleTransition titleZoom = new ScaleTransition(Duration.seconds(0.8), titleLabel);
        titleZoom.setFromX(0);
        titleZoom.setFromY(0);
        titleZoom.setToX(1);
        titleZoom.setToY(1);
        titleZoom.setInterpolator(Interpolator.EASE_OUT);
        titleZoom.play();
        
        // Loading dots animation
        animateDots();
        
        // Transition to Login after 4 seconds
        PauseTransition pause = new PauseTransition(Duration.seconds(4));
        pause.setOnFinished(e -> loadLoginScreen());
        pause.play();
    }
    
    private void animateFruits(HBox container, double translateY, double delayStart) {
        for (int i = 0; i < container.getChildren().size(); i++) {
            Label fruit = (Label) container.getChildren().get(i);
            
            // Bounce animation
            TranslateTransition bounce = new TranslateTransition(Duration.seconds(0.6), fruit);
            bounce.setByY(translateY);
            bounce.setCycleCount(Animation.INDEFINITE);
            bounce.setAutoReverse(true);
            bounce.setDelay(Duration.millis(delayStart + i * 120));
            bounce.setInterpolator(Interpolator.EASE_BOTH);
            bounce.play();
            
            // Rotation animation
            RotateTransition rotate = new RotateTransition(Duration.seconds(3), fruit);
            rotate.setByAngle(360);
            rotate.setCycleCount(Animation.INDEFINITE);
            rotate.setDelay(Duration.millis(delayStart + i * 150));
            rotate.setInterpolator(Interpolator.LINEAR);
            rotate.play();
            
            // Scale pulse
            ScaleTransition pulse = new ScaleTransition(Duration.seconds(1), fruit);
            pulse.setFromX(1);
            pulse.setFromY(1);
            pulse.setToX(1.2);
            pulse.setToY(1.2);
            pulse.setCycleCount(Animation.INDEFINITE);
            pulse.setAutoReverse(true);
            pulse.setDelay(Duration.millis(delayStart + i * 100));
            pulse.play();
        }
    }
    
    private void animateCircles() {
        animateCircle(circle1, 100, 80, 0);
        animateCircle(circle2, -120, 100, 500);
        animateCircle(circle3, 80, -90, 1000);
        animateCircle(circle4, -100, 70, 1500);
    }
    
    private void animateCircle(Circle circle, double byX, double byY, double delay) {
        TranslateTransition move = new TranslateTransition(Duration.seconds(4), circle);
        move.setByX(byX);
        move.setByY(byY);
        move.setCycleCount(Animation.INDEFINITE);
        move.setAutoReverse(true);
        move.setDelay(Duration.millis(delay));
        move.setInterpolator(Interpolator.EASE_BOTH);
        move.play();
    }
    
    private void animateDots() {
        animateDot(dot1, 0);
        animateDot(dot2, 200);
        animateDot(dot3, 400);
    }
    
    private void animateDot(Label dot, double delay) {
        FadeTransition fade = new FadeTransition(Duration.seconds(0.6), dot);
        fade.setFromValue(0.2);
        fade.setToValue(1);
        fade.setCycleCount(Animation.INDEFINITE);
        fade.setAutoReverse(true);
        fade.setDelay(Duration.millis(delay));
        fade.play();
        
        TranslateTransition jump = new TranslateTransition(Duration.seconds(0.6), dot);
        jump.setByY(-15);
        jump.setCycleCount(Animation.INDEFINITE);
        jump.setAutoReverse(true);
        jump.setDelay(Duration.millis(delay));
        jump.play();
    }
    
    private void loadLoginScreen() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/Login.fxml"));
            Scene scene = new Scene(root, 960, 540);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            
            Stage stage = (Stage) titleLabel.getScene().getWindow();
            
            FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.7), titleLabel.getScene().getRoot());
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(e -> {
                stage.setScene(scene);
                stage.setTitle("GreenGrocer - Login");
                stage.centerOnScreen();
                
                FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.7), scene.getRoot());
                fadeIn.setFromValue(0);
                fadeIn.setToValue(1);
                fadeIn.play();
            });
            fadeOut.play();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}