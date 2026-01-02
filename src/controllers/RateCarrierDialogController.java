package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import models.User;

/**
 * Dialog controller allowing customers to rate a carrier after delivery.
 */
public class RateCarrierDialogController {

    @FXML private Label titleLabel;
    @FXML private Button star1;
    @FXML private Button star2;
    @FXML private Button star3;
    @FXML private Button star4;
    @FXML private Button star5;
    @FXML private Button okBtn;
    @FXML private Button cancelBtn;

    private int carrierId;
    private int orderId;
    private User user;
    private int selectedRating = 0;
    private String comment = "";

    public void setData(int carrierId, int orderId, User user) {
        this.carrierId = carrierId;
        this.orderId = orderId;
        this.user = user;
        titleLabel.setText("Rate Carrier #" + carrierId);
    }

    public int getSelectedRating() {
        return selectedRating;
    }

    public String getComment() {
        return comment;
    }

    private void updateStars() {
        star1.setText(selectedRating >= 1 ? "★" : "☆");
        star2.setText(selectedRating >= 2 ? "★" : "☆");
        star3.setText(selectedRating >= 3 ? "★" : "☆");
        star4.setText(selectedRating >= 4 ? "★" : "☆");
        star5.setText(selectedRating >= 5 ? "★" : "☆");
    }

    @FXML private void onStar1() { selectedRating = 1; updateStars(); }
    @FXML private void onStar2() { selectedRating = 2; updateStars(); }
    @FXML private void onStar3() { selectedRating = 3; updateStars(); }
    @FXML private void onStar4() { selectedRating = 4; updateStars(); }
    @FXML private void onStar5() { selectedRating = 5; updateStars(); }

    @FXML private void onOk() {
        // simply close; MyOrdersController will read selectedRating
        Stage s = (Stage) okBtn.getScene().getWindow();
        s.close();
    }

    @FXML private void onCancel() {
        selectedRating = 0;
        Stage s = (Stage) cancelBtn.getScene().getWindow();
        s.close();
    }
}
