package main;

import java.sql.Timestamp;

public class Message {
    private int id;
    private int senderId;
    private int receiverId;
    private String content;
    private Timestamp sentAt;

    public Message(int senderId, int receiverId, String content) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
    }
    
    // DB'den okurken kullanılan constructor
    public Message(int id, int senderId, int receiverId, String content, Timestamp sentAt) {
        this.id = id;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
        this.sentAt = sentAt;
    }

    public String getContent() { return content; }
    public int getSenderId() { return senderId; }
    public Timestamp getSentAt() { return sentAt; }
    
    @Override
    public String toString() {
        return senderId + ": " + content;
    }
}