package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import models.Message;
import models.User;
import services.MessageDAO;
import services.UserDAO;

import java.util.List;

/**
 * Controller for owner's chat interface. Shows conversations and messages.
 */
public class OwnerChatController {

    @FXML private ListView<User> conversationsList;
    @FXML private Label chatWithLabel;
    @FXML private ListView<String> messagesList;
    @FXML private TextField messageField;
    @FXML private Button sendButton;

    private User currentUser;
    private MessageDAO messageDAO = new MessageDAO();
    private UserDAO userDAO = new UserDAO();
    private ObservableList<User> partners = FXCollections.observableArrayList();
    private int selectedPartnerId = -1;

    @FXML
    private void initialize() {
        conversationsList.setItems(partners);
        conversationsList.setCellFactory(lv -> new ListCell<User>() {
            @Override
            protected void updateItem(User item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getFullName() + " (" + item.getUsername() + ")");
                }
            }
        });

        conversationsList.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) loadConversation(newV.getId());
        });

        messagesList.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    getStyleClass().removeAll("chat-msg-owner", "chat-msg-customer");
                } else {
                    setText(item);
                    getStyleClass().removeAll("chat-msg-owner", "chat-msg-customer");
                    if (item.startsWith("You:")) {
                        getStyleClass().add("chat-msg-owner");
                    } else {
                        getStyleClass().add("chat-msg-customer");
                    }
                }
            }
        });
    }

    public void setUser(User user) {
        this.currentUser = user;
        loadPartners();
    }

    private void loadPartners() {
        partners.clear();
        if (currentUser == null) return;
        List<Integer> partnerIds = messageDAO.getConversationPartnersForOwner(currentUser.getId());
        for (Integer id : partnerIds) {
            User u = userDAO.getUserById(id);
            if (u != null) {
                // If this is the sample customer and full name lacks Turkish characters,
                // update DB to the correct Turkish full name and refresh the object.
                if ("cust".equals(u.getUsername()) && (u.getFullName() == null || u.getFullName().matches("^[\\u0000-\\u007F]*$"))) {
                    boolean updated = userDAO.updateFullNameByUsername("cust", "Ahmet Müşteri");
                    if (updated) {
                        u.setFullName("Ahmet Müşteri");
                    }
                }
                partners.add(u);
            }
        }
    }

    private void loadConversation(int partnerId) {
        selectedPartnerId = partnerId;
        User partner = userDAO.getUserById(partnerId);
        chatWithLabel.setText((partner != null) ? partner.getFullName() : "Conversation");
        messagesList.getItems().clear();
        List<Message> convo = messageDAO.getConversation(currentUser.getId(), partnerId);
        for (Message m : convo) {
            String sender = (m.getFromUserId() == currentUser.getId()) ? "You" : (partner != null ? partner.getFullName() : "User");
            messagesList.getItems().add(sender + ": " + m.getContent());
        }
        messagesList.scrollTo(messagesList.getItems().size() - 1 >= 0 ? messagesList.getItems().size()-1 : 0);
    }

    @FXML
    private void handleSend(ActionEvent event) {
        String text = messageField.getText();
        if (text == null || text.trim().isEmpty() || selectedPartnerId == -1) return;

        Message m = new Message(currentUser.getId(), selectedPartnerId, text.trim());
        messageDAO.saveMessage(m);
        messagesList.getItems().add("You: " + text.trim());
        messageField.clear();
        messagesList.scrollTo(messagesList.getItems().size() - 1);
    }
}
