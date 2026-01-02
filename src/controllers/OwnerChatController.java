package controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import models.Message;
import models.User;
import services.MessageDAO;
import services.UserDAO;

import java.util.List;

/**
 * Controller for Owner's Chat Interface with Modern Message Bubbles.
 * 
 * <p>Features:
 * <ul>
 *   <li>Display list of customers who have messaged the owner</li>
 *   <li>View conversation history with selected customer</li>
 *   <li>Send messages to customers with visual message bubbles</li>
 *   <li>Automatic Turkish character handling for customer names</li>
 * </ul>
 * 
 * <p>UI Components:
 * <ul>
 *   <li>Left panel: List of conversation partners (customers)</li>
 *   <li>Center: Message bubbles (owner messages on right, customer on left)</li>
 *   <li>Bottom: Text input and send button</li>
 * </ul>
 * 
 * @author Group04
 * @version 2.0 - Modern Chat with Message Bubbles
 * @see Message
 * @see MessageDAO
 * @see User
 */
public class OwnerChatController {

    /**
     * ListView displaying all customers who have conversations with the owner.
     * Shows customer full name and username.
     */
    @FXML private ListView<User> conversationsList;
    
    /**
     * Label showing the name of currently selected conversation partner.
     */
    @FXML private Label chatWithLabel;
    
    /**
     * VBox container for message bubbles.
     * Messages are added as HBox elements containing styled Labels.
     */
    @FXML private VBox messagesContainer;
    
    /**
     * Text field for typing new messages.
     */
    @FXML private TextField messageField;
    
    /**
     * Button to send the message typed in messageField.
     */
    @FXML private Button sendButton;

    /**
     * Currently logged-in owner user.
     */
    private User currentUser;
    
    /**
     * Data Access Object for message operations.
     */
    private MessageDAO messageDAO = new MessageDAO();
    
    /**
     * Data Access Object for user operations.
     */
    private UserDAO userDAO = new UserDAO();
    
    /**
     * Observable list of conversation partners (customers).
     * Automatically updates the conversationsList ListView.
     */
    private ObservableList<User> partners = FXCollections.observableArrayList();
    
    /**
     * User ID of currently selected conversation partner.
     * Set to -1 when no conversation is selected.
     */
    private int selectedPartnerId = -1;

    /**
     * Initializes the controller after FXML loading.
     * 
     * <p>Sets up:
     * <ul>
     *   <li>Conversations list with custom cell factory</li>
     *   <li>Selection listener for loading conversations</li>
     * </ul>
     */
    @FXML
    private void initialize() {
        conversationsList.setItems(partners);
        
        // Custom cell factory to display user info
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

        // Load conversation when a partner is selected
        conversationsList.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) loadConversation(newV.getId());
        });
    }

    /**
     * Sets the current logged-in owner user and loads their conversation partners.
     * 
     * <p>This method is called by OwnerMainController after opening the chat window.
     * 
     * @param user The owner user who is logged in
     */
    public void setUser(User user) {
        this.currentUser = user;
        loadPartners();
    }

    /**
     * Loads all customers who have messaged the owner.
     * 
     * <p>Special handling for Turkish characters:
     * If the sample customer "cust" has ASCII-only full name, updates it to
     * "Ahmet Müşteri" in the database.
     */
    private void loadPartners() {
        partners.clear();
        if (currentUser == null) return;
        
        List<Integer> partnerIds = messageDAO.getConversationPartnersForOwner(currentUser.getId());
        for (Integer id : partnerIds) {
            User u = userDAO.getUserById(id);
            if (u != null) {
                // Fix Turkish characters for sample customer if needed
                if ("cust".equals(u.getUsername()) && 
                    (u.getFullName() == null || u.getFullName().matches("^[\\u0000-\\u007F]*$"))) {
                    boolean updated = userDAO.updateFullNameByUsername("cust", "Ahmet Müşteri");
                    if (updated) {
                        u.setFullName("Ahmet Müşteri");
                    }
                }
                partners.add(u);
            }
        }
    }

    /**
     * Loads the conversation history with a specific customer.
     * 
     * <p>Clears the messages container and displays all messages as bubbles.
     * Owner's messages appear on the right with purple/blue styling,
     * customer's messages appear on the left with white styling.
     * 
     * @param partnerId The user ID of the customer to load conversation with
     */
    private void loadConversation(int partnerId) {
        selectedPartnerId = partnerId;
        User partner = userDAO.getUserById(partnerId);
        chatWithLabel.setText((partner != null) ? partner.getFullName() : "Conversation");
        
        // Clear previous messages
        messagesContainer.getChildren().clear();
        
        // Load and display all messages
        List<Message> convo = messageDAO.getConversation(currentUser.getId(), partnerId);
        for (Message m : convo) {
            boolean isSent = (m.getFromUserId() == currentUser.getId());
            addMessageBubble(m.getContent(), isSent);
        }
    }

    /**
     * Handles the Send button click or Enter key press.
     * 
     * <p>Validates input, saves message to database, and displays it as a bubble.
     * Does nothing if:
     * <ul>
     *   <li>Message text is empty or only whitespace</li>
     *   <li>No conversation partner is selected</li>
     * </ul>
     * 
     * @param event The action event from button click or Enter key
     */
    @FXML
    private void handleSend(ActionEvent event) {
    String text = messageField.getText();
    
    if (text == null || text.trim().isEmpty() || selectedPartnerId == -1) {
        return;
    }

    Message m = new Message(currentUser.getId(), selectedPartnerId, text.trim());
    messageDAO.saveMessage(m);
    addMessageBubble(text.trim(), true);
    messageField.clear();
}
    
    /**
     * Creates and adds a styled message bubble to the messages container.
     * 
     * <p>Styling:
     * <ul>
     *   <li>Sent messages (owner): Right-aligned, purple gradient background</li>
     *   <li>Received messages (customer): Left-aligned, white background</li>
     * </ul>
     * 
     * <p>CSS classes applied:
     * <ul>
     *   <li>{@code message-bubble-sent} for owner messages</li>
     *   <li>{@code message-bubble-received} for customer messages</li>
     * </ul>
     * 
     * @param text The message text to display
     * @param isSent {@code true} if message is from owner (sent),
     *               {@code false} if from customer (received)
     */
    private void addMessageBubble(String text, boolean isSent) {
        // Create message label with text wrapping
        Label msgLabel = new Label(text);
        msgLabel.setWrapText(true);
        msgLabel.setMaxWidth(400);
        msgLabel.getStyleClass().add(isSent ? "message-bubble-sent" : "message-bubble-received");
        
        // Wrap in HBox for alignment
        HBox msgBox = new HBox(msgLabel);
        msgBox.setAlignment(isSent ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        msgBox.setStyle("-fx-padding: 5;");
        
        // Add to messages container
        messagesContainer.getChildren().add(msgBox);
    }
}