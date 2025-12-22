package main;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MessageDAO {

    // MESAJ GÖNDER
    public boolean sendMessage(Message msg) {
        String sql = "INSERT INTO messages (sender_id, receiver_id, content) VALUES (?, ?, ?)";
        
        try (Connection conn = DatabaseAdapter.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, msg.getSenderId());
            // Eğer receiverId 0 ise "Genel Duyuru" gibi düşünülebilir
            pstmt.setInt(2, 0); // Şimdilik 0, User nesnesinden gelmeli
            pstmt.setString(3, msg.getContent());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // İKİ KİŞİ ARASINDAKİ MESAJLARI GETİR
    public List<Message> getConversation(int user1, int user2) {
        List<Message> messages = new ArrayList<>();
        // Hem giden hem gelen mesajları tarihe göre sırala
        String sql = "SELECT * FROM messages WHERE (sender_id = ? AND receiver_id = ?) OR (sender_id = ? AND receiver_id = ?) ORDER BY sent_at ASC";
        
        try (Connection conn = DatabaseAdapter.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, user1);
            pstmt.setInt(2, user2);
            pstmt.setInt(3, user2);
            pstmt.setInt(4, user1);
            
            ResultSet rs = pstmt.executeQuery();
            while(rs.next()) {
                messages.add(new Message(
                    rs.getInt("id"),
                    rs.getInt("sender_id"),
                    rs.getInt("receiver_id"),
                    rs.getString("content"),
                    rs.getTimestamp("sent_at")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return messages;
    }
}