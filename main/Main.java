package main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.sql.Connection;
import java.sql.SQLException;

// extends Application diyerek Java'ya "Bu bir pencere uygulamasıdır" diyoruz.
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception
    {
        // Login.fxml dosyasını yükle
        // "uistuff" klasörüne bakması gerektiğini belirtiyoruz
        Parent root = FXMLLoader.load(getClass().getResource("uistuff/Login.fxml"));
        
        primaryStage.setTitle("GreenGrocer Login");
        primaryStage.setScene(new Scene(root));
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    public static void main(String[] args) {
        // Önce veritabanı kontrolü (Eski kodunuzdaki kısım)
        try (Connection conn = DatabaseAdapter.getConnection()) {
            if (conn != null) System.out.println("Database Bağlandı!");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Sonra ekranı başlat (Sihirli komut burası)
        launch(args);
    }
}