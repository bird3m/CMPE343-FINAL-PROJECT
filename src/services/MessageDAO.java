package services;

import models.Message;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MessageDAO {

    public int getDefaultOwnerId() {
        String sql = "SELECT id FROM userinfo WHERE role = 'owner' LIMIT 1";
        try (Connection conn = DatabaseAdapter.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) return rs.getInt("id");
        } catch (SQLException e) { e.printStackTrace(); }
        return -1;
    }

    public boolean saveMessage(Message m) {
        String sql = "INSERT INTO messageinfo (from_user_id, to_user_id, content, sent_at, is_read) VALUES (?, ?, ?, ?, 0)";
        try (Connection conn = DatabaseAdapter.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, m.getFromUserId());
            pstmt.setInt(2, m.getToUserId());
            pstmt.setString(3, m.getContent());
            pstmt.setTimestamp(4, Timestamp.valueOf(m.getSentAt()));

            int affected = pstmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = pstmt.getGeneratedKeys()) {
                    if (keys.next()) m.setId(keys.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Message> getConversation(int userA, int userB) {
        String sql = "SELECT * FROM messageinfo WHERE (from_user_id = ? AND to_user_id = ?) OR (from_user_id = ? AND to_user_id = ?) ORDER BY sent_at ASC";
        List<Message> list = new ArrayList<>();
        try (Connection conn = DatabaseAdapter.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userA);
            pstmt.setInt(2, userB);
            pstmt.setInt(3, userB);
            pstmt.setInt(4, userA);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Message m = mapRow(rs);
                    list.add(m);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Integer> getConversationPartnersForOwner(int ownerId) {
        String sql = "SELECT DISTINCT CASE WHEN from_user_id = ? THEN to_user_id ELSE from_user_id END AS other FROM messageinfo WHERE from_user_id = ? OR to_user_id = ?";
        List<Integer> partners = new ArrayList<>();
        try (Connection conn = DatabaseAdapter.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, ownerId);
            pstmt.setInt(2, ownerId);
            pstmt.setInt(3, ownerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) partners.add(rs.getInt("other"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return partners;
    }

    private Message mapRow(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        int from = rs.getInt("from_user_id");
        int to = rs.getInt("to_user_id");
        String content = rs.getString("content");
        Timestamp ts = rs.getTimestamp("sent_at");
        LocalDateTime sentAt = ts != null ? ts.toLocalDateTime() : LocalDateTime.now();
        return new Message(id, from, to, content, sentAt);
    }
}
