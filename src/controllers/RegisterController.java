package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label; // <--- İŞTE KRİTİK IMPORT BU!
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import services.AuthenticationService;
import services.DatabaseAdapter;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class RegisterController {

    // --- FXML BİLEŞENLERİ ---
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextArea addressField;
    @FXML private TextField phoneField;
    @FXML private Label errorLabel; // Artık JavaFX Label olduğunu biliyor!

    @FXML
    private void initialize() {
        if(errorLabel != null) errorLabel.setVisible(false);
    }

    @FXML
    private void handleRegister(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String confirmPass = confirmPasswordField.getText().trim();
        String address = addressField.getText().trim();
        String phone = phoneField.getText().trim();

        // Zorunlu alan kontrolü
        if (username.isEmpty() || password.isEmpty() || address.isEmpty()) {
            showError("Lütfen zorunlu alanları doldurun.");
            return;
        }

        // Şifre eşleşme kontrolü
        if (!password.equals(confirmPass)) {
            showError("Şifreler uyuşmuyor!");
            return;
        }

        // Şifreleme (Hash)
        String hashedPassword = AuthenticationService.hashPassword(password);

        // Veritabanına kayıt (full_name ve password_hash eklendi)
        String sql = "INSERT INTO userinfo (username, password_hash, role, phone, address, full_name) VALUES (?, ?, 'customer', ?, ?, ?)";

        try (Connection conn = DatabaseAdapter.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            pstmt.setString(2, hashedPassword);
            pstmt.setString(3, phone);
            pstmt.setString(4, address);
            pstmt.setString(5, username); // full_name yoksa username yazıyoruz
            
            int rows = pstmt.executeUpdate();
            
            if (rows > 0) {
                showSuccessAndExit();
            } else {
                showError("Kayıt oluşturulamadı.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showError("Kullanıcı adı alınmış olabilir!");
        }
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/Login.fxml"));
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void showError(String msg) {
        if(errorLabel != null) {
            errorLabel.setText(msg);
            errorLabel.setTextFill(Color.RED);
            errorLabel.setVisible(true);
        } else {
            // Eğer FXML'de label yoksa Alert göster
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText(msg);
            alert.showAndWait();
        }
    }

    private void showSuccessAndExit() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Başarılı");
        alert.setHeaderText(null);
        alert.setContentText("Kayıt başarılı! Giriş yapabilirsiniz.");
        alert.showAndWait();
        handleBack(null);
    }
}