package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.User;
import services.UserDAO;
import services.AuthenticationService;

public class CarrierFormController {

    @FXML private TextField usernameField;
    @FXML private TextField fullNameField;
    @FXML private TextField phoneField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private Button saveButton;

    private UserDAO userDAO;

    @FXML
    private void initialize() {
        userDAO = new UserDAO();
        if (errorLabel != null) errorLabel.setVisible(false);
    }

    @FXML
    private void handleSave(ActionEvent event) {
        String username = usernameField.getText().trim();
        String fullName = fullNameField.getText().trim();
        String phone = phoneField.getText().trim();
        String password = passwordField.getText().trim();

        // 1. Validation using InputValidation
        String err;
        err = utils.InputValidation.validateUsername(username);
        if (err != null) { showError(err); return; }

        err = utils.InputValidation.validateFullName(fullName);
        if (err != null) { showError(err); return; }

        err = utils.InputValidation.validatePhone(phone);
        if (err != null) { showError(err); return; }

        err = utils.InputValidation.validatePassword(password);
        if (err != null) { showError(err); return; }

        if (userDAO.usernameExists(username)) {
            showError("Username already taken!");
            return;
        }

        // 2. Create User Object
        User newCarrier = new User();
        newCarrier.setUsername(username);
        newCarrier.setFullName(fullName);
        newCarrier.setPhone(phone);
        newCarrier.setPassword(AuthenticationService.hashPassword(password));
        newCarrier.setRole("carrier");
        newCarrier.setAddress("Office"); // Kurye adresi varsayılan olarak ofis

        // 3. Save to DB
        boolean success = userDAO.createUser(newCarrier);

        if (success) {
            // Başarılıysa pencereyi kapat
            Stage stage = (Stage) saveButton.getScene().getWindow();
            stage.close();
        } else {
            showError("Database error! Could not employ carrier.");
        }
    }

    private void showError(String msg) {
        if (errorLabel != null) {
            errorLabel.setText(msg);
            errorLabel.setVisible(true);
        }
    }
}