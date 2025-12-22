package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Green Grocer Application - Main Entry Point
 * @author Group04
 */
public class Main extends Application {
    
    public static final String CURRENCY = "$";
    public static final String APP_TITLE = "Group04 GreenGrocer";
    
    @Override
    public void start(Stage primaryStage) {
        try {
            // Login screen
            Parent root = FXMLLoader.load(
                getClass().getResource("/fxml/Login.fxml")
            );
            
            Scene scene = new Scene(root, 960, 540);
            scene.getStylesheets().add(
                getClass().getResource("/css/style.css").toExternalForm()
            );
            
            primaryStage.setScene(scene);
            primaryStage.setTitle(APP_TITLE + " - Login");
            primaryStage.setResizable(true);
            primaryStage.centerOnScreen();
            primaryStage.show();
            
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error loading Login screen: " + e.getMessage());
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}