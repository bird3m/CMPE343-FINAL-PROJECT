package main.uistuff;

// Logic klasöründen (main) gerekli sınıfları çağırıyoruz
import main.AuthenticationService;
import main.User;

// JavaFX Kütüphaneleri
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.io.IOException;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Button registerButton;
    @FXML private Label errorLabel;

    private AuthenticationService authService;

    // Constructor
    public LoginController() {
        this.authService = new AuthenticationService();
    }

    @FXML
    public void initialize() {
        loginButton.setDefaultButton(true); // Enter tuşu ile giriş
        loginButton.setOnAction(event -> handleLogin());
        registerButton.setOnAction(event -> navigateToRegister());
    }

    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Lütfen kullanıcı adı ve şifreyi giriniz.");
            return;
        }

        // Service üzerinden kontrol
        User user = authService.authenticate(username, password);

        if (user != null) {
            System.out.println("Giriş Başarılı: " + user.getUsername() + " Rol: " + user.getRole());
            errorLabel.setText(""); 
            navigateToRoleScreen(user);
        } else {
            errorLabel.setText("Hatalı kullanıcı adı veya şifre!");
        }
    }

    private void navigateToRegister() {
        try {
            // Register.fxml dosyası LoginController ile AYNI KLASÖRDE (uistuff) olmalı
            Parent root = FXMLLoader.load(getClass().getResource("Register.fxml"));
            Stage stage = (Stage) registerButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("GreenGrocer Kayıt Ol");
        } catch (IOException e) {
            e.printStackTrace();
            errorLabel.setText("Kayıt ekranı bulunamadı!");
        }
    }

    private void navigateToRoleScreen(User user) {
        try {
            String fxmlFile = "";
            String title = "";

            switch (user.getRole().toLowerCase()) {
                case "customer":
                    fxmlFile = "CustomerMain.fxml";
                    title = "Group04 GreenGrocer"; 
                    break;
                case "carrier":
                    fxmlFile = "CarrierMain.fxml";
                    title = "Kurye Paneli";
                    break;
                case "owner":
                    fxmlFile = "OwnerMain.fxml";
                    title = "Yönetici Paneli";
                    break;
                default:
                    errorLabel.setText("Bu kullanıcının rolü tanımsız!");
                    return;
            }

            // Dosya yükleme (Dosyalar uistuff klasöründe olmalı)
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();
            
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.centerOnScreen(); 

        } catch (IOException e) {
            System.err.println("FXML Yükleme Hatası: " + e.getMessage());
            e.printStackTrace();
            errorLabel.setText("Ekran yüklenirken hata oluştu!");
        }
    }
}