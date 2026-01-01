package controllers;

import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.util.Duration;
import models.User;
import models.Message;
import services.MessageDAO;
import services.UserDAO;
import java.util.List;

/**
 * Simple chat controller for Customer <-> Owner messaging demo
 */
public class ChatController {

    @FXML private Label chatWithLabel;
    @FXML private ListView<String> messagesList;
    @FXML private TextField messageField;
    @FXML private Button sendButton;

    private User currentUser;
    private ObservableList<String> messages;
    private MessageDAO messageDAO = new MessageDAO();
    private int ownerId = -1;

    @FXML
    private void initialize() {
        messages = FXCollections.observableArrayList();
        messagesList.setItems(messages);

        // Message cell styling based on sender prefix
        messagesList.setCellFactory(lv -> new javafx.scene.control.ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    getStyleClass().removeAll("chat-msg-owner", "chat-msg-customer");
                } else {
                    setText(item);
                    getStyleClass().removeAll("chat-msg-owner", "chat-msg-customer");
                    if (item.startsWith("Owner:") || item.startsWith("Owner -")) {
                        getStyleClass().add("chat-msg-owner");
                    } else if (currentUser != null && item.startsWith(currentUser.getUsername() + ":")) {
                        getStyleClass().add("chat-msg-customer");
                    }
                }
            }
        });
    }

    public void setUser(User user) {
        this.currentUser = user;
        UserDAO userDAO = new UserDAO();
        if (user != null) {
            // Refresh from DB (in case fullname needs non-ascii characters)
            User fresh = userDAO.getUserById(user.getId());
            if (fresh != null) this.currentUser = fresh;
            chatWithLabel.setText("Owner - Chatting as: " + (this.currentUser.getUsername() != null ? this.currentUser.getUsername() : ""));

            // If full name seems ASCII-only and username is 'cust', ensure Turkish diacritics
            String fn = this.currentUser.getFullName();
            if ("cust".equals(this.currentUser.getUsername()) && (fn == null || fn.matches("^[\u0000-\u007F]*$"))) {
                userDAO.updateFullNameByUsername("cust", "Ahmet Müşteri");
                this.currentUser.setFullName("Ahmet Müşteri");
            }
        }

        // Load conversation with owner
        ownerId = messageDAO.getDefaultOwnerId();
        if (ownerId == -1) {
            messages.add("System: No owner configured in the system.");
            return;
        }

        // Load conversation and display using the fixed display name "Owner"
        List<Message> convo = messageDAO.getConversation(this.currentUser.getId(), ownerId);

        if (convo.isEmpty()) {
            messages.add("Owner: Hello! How can I help you today?");
        } else {
            for (Message m : convo) {
                String senderName = (m.getFromUserId() == this.currentUser.getId()) ? (this.currentUser.getUsername() != null ? this.currentUser.getUsername() : "") : "Owner";
                messages.add(senderName + ": " + m.getContent());
            }
        }
        messagesList.scrollTo(messages.size() - 1);
    }

    @FXML
    private void handleSend(ActionEvent event) {
        String text = messageField.getText();
        if (text == null || text.trim().isEmpty()) return;

        // Save customer message
        Message m = new Message(currentUser.getId(), ownerId, text.trim());
        messageDAO.saveMessage(m);

        String sender = currentUser != null ? (currentUser.getUsername() != null ? currentUser.getUsername() : "") : "You";
        messages.add(sender + ": " + text.trim());
        messageField.clear();
        messagesList.scrollTo(messages.size() - 1);

        // Simulate owner reply (and persist)
        PauseTransition pause = new PauseTransition(Duration.seconds(1.0));
        pause.setOnFinished(e -> {
            String reply = "Thanks for your message! We'll get back to you shortly.";
            Message ownerMsg = new Message(ownerId, currentUser.getId(), reply);
            messageDAO.saveMessage(ownerMsg);
            messages.add("Owner: " + reply);
            messagesList.scrollTo(messages.size() - 1);
        });
        pause.play();
    }
}
