package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.SQLException;

import services.DatabaseAdapter;

/**
 * GreenGrocer Application
 * Main entry point of the system.
 *
 * Responsibilities:
 * - Tests database connection at startup
 * - Launches JavaFX application
 * - Loads Login.fxml as the first screen
 *
 * Group: Group04
 */
public class Main extends Application {

    public static final String CURRENCY = "₺";
    public static final String APP_TITLE = "Group04 GreenGrocer";

    /**
     * JavaFX lifecycle method.
     * This method is automatically called after launch().
     */
    @Override
    public void start(Stage primaryStage) {
        try {
            // Load Login screen (FXML)
            //Parent root = FXMLLoader.load(getClass().getResource("/fxml/Login.fxml"));
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/SplashScreen.fxml"));

            // Create scene with fixed size
            Scene scene = new Scene(root, 960, 540);

            // Attach global CSS
            scene.getStylesheets().add(
                    getClass().getResource("/css/style.css").toExternalForm()
            );

            // Configure stage
            primaryStage.setTitle(APP_TITLE + " - Login");
            primaryStage.setScene(scene);
            primaryStage.setResizable(true);
            primaryStage.centerOnScreen();
            primaryStage.show();

        } catch (Exception e) {
            // Critical error: UI could not be loaded
            System.err.println("ERROR: Failed to load Login.fxml");
            e.printStackTrace();
        }
    }


    /**
     * Application entry point.
     * This method:
     * 1. Tests database connection
     * 2. Launches JavaFX application
     */
    public static void main(String[] args) {

        // Database connection test before UI starts
        try (Connection conn = DatabaseAdapter.getConnection()) {
            if (conn != null) {
                System.out.println("Database connection successful.");
            } else {
                System.err.println("Database connection returned null.");
            }
        } catch (SQLException e) {
            System.err.println("Database connection failed.");
            e.printStackTrace();
        }

        // Start JavaFX application
        launch(args);
    }
}
