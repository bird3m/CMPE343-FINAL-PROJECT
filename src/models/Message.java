package models;

import java.time.LocalDateTime;

public class Message {
    private int id;
    private int fromUserId;
    private int toUserId;
    private String content;
    private LocalDateTime sentAt;

    public Message(int id, int fromUserId, int toUserId, String content, LocalDateTime sentAt) {
        this.id = id;
        this.fromUserId = fromUserId;
        this.toUserId = toUserId;
        this.content = content;
        this.sentAt = sentAt;
    }

    public Message(int fromUserId, int toUserId, String content) {
        this(0, fromUserId, toUserId, content, LocalDateTime.now());
    }

    public int getId() { return id; }
    public int getFromUserId() { return fromUserId; }
    public int getToUserId() { return toUserId; }
    public String getContent() { return content; }
    public LocalDateTime getSentAt() { return sentAt; }

    public void setId(int id) { this.id = id; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }
}
