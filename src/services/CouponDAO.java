package services;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CouponDAO {

    /**
     * Checks if a coupon is valid and returns its discount rate.
     * Returns 0.0 if invalid or inactive.
     */
    public double getDiscountRate(String code) {
        String sql = "SELECT discount_rate FROM couponinfo WHERE UPPER(code) = UPPER(?) AND is_active = 1";
        
        try (Connection conn = DatabaseAdapter.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, code);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getDouble("discount_rate");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    /**
     * For Owner: Create a new coupon.
     */
    public boolean addCoupon(String code, double rate) {
        String sql = "INSERT INTO couponinfo (code, discount_rate) VALUES (?, ?)";
        try (Connection conn = DatabaseAdapter.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, code.toUpperCase());
            pstmt.setDouble(2, rate);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Fetches all active coupons from the database.
     * Used to show customers what codes are available.
     */
    public List<String> getAllActiveCoupons() {
        List<String> coupons = new ArrayList<>();
        String sql = "SELECT code, discount_rate FROM couponinfo WHERE is_active = 1";
        
        try (Connection conn = DatabaseAdapter.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                // Format: CODE (%Rate OFF)
                coupons.add(rs.getString("code") + " (%" + rs.getDouble("discount_rate") + " OFF)");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return coupons;
    }

    /**
     * Fetches coupons assigned to a specific user (not yet redeemed).
     */
    public List<String> getCouponsForUser(int userId) {
        List<String> coupons = new ArrayList<>();
        String sql = "SELECT c.code, c.discount_rate FROM couponinfo c JOIN user_coupons uc ON c.id = uc.coupon_id WHERE uc.user_id = ? AND uc.redeemed = 0 AND c.is_active = 1";
        try (Connection conn = DatabaseAdapter.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                coupons.add(rs.getString("code") + " (%" + rs.getDouble("discount_rate") + " OFF)");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return coupons;
    }

    /**
     * Ensures a coupon with the given code exists. Returns the coupon id (existing or newly created).
     */
    public int ensureCouponExists(String code, double rate) {
        String select = "SELECT id FROM couponinfo WHERE UPPER(code) = UPPER(?) LIMIT 1";
        try (Connection conn = DatabaseAdapter.getConnection();
             PreparedStatement ps = conn.prepareStatement(select)) {
            ps.setString(1, code);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int id = rs.getInt("id");
                System.out.println("ensureCouponExists: found existing coupon " + code + " id=" + id);
                return id;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String insert = "INSERT INTO couponinfo (code, discount_rate, is_active) VALUES (?, ?, 1)";
        try (Connection conn = DatabaseAdapter.getConnection();
             PreparedStatement ps = conn.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, code.toUpperCase());
            ps.setDouble(2, rate);
            int affected = ps.executeUpdate();
            if (affected > 0) {
                ResultSet gk = ps.getGeneratedKeys();
                if (gk.next()) {
                    int newId = gk.getInt(1);
                    System.out.println("ensureCouponExists: created coupon " + code + " id=" + newId);
                    return newId;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("ensureCouponExists: failed to create/find coupon " + code);
        return -1;
    }

    /**
     * Assigns a coupon (by code) to a user if not already assigned. Returns true if assigned or already present.
     */
    public boolean assignCouponToUserByCode(int userId, String code, double defaultRateIfMissing) {
        int couponId = ensureCouponExists(code, defaultRateIfMissing);
        if (couponId <= 0) return false;

        // Check if user already has this coupon assigned and whether it's redeemed.
        // If an unredeemed row exists, treat as already assigned (return true).
        // If only a redeemed row exists, allow reassign for general coupons but
        // do NOT reassign for one-time welcome coupons (WELCOME10).
        String check = "SELECT redeemed FROM user_coupons WHERE user_id = ? AND coupon_id = ? LIMIT 1";
        try (Connection conn = DatabaseAdapter.getConnection();
             PreparedStatement ps = conn.prepareStatement(check)) {
            ps.setInt(1, userId);
            ps.setInt(2, couponId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int redeemed = rs.getInt("redeemed");
                if (redeemed == 0) return true; // already assigned and usable
                // redeemed == 1 -> previously used by this user
                // Do not reassign WELCOME10; allow others (like LOYAL5)
                if (code != null && code.equalsIgnoreCase("WELCOME10")) {
                    return false;
                }
                // otherwise fall through and insert a new assignment
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        String insert = "INSERT INTO user_coupons (user_id, coupon_id, redeemed, assigned_at) VALUES (?, ?, 0, NOW())";
        try (Connection conn = DatabaseAdapter.getConnection();
             PreparedStatement ps = conn.prepareStatement(insert)) {
            ps.setInt(1, userId);
            ps.setInt(2, couponId);
            boolean ok = ps.executeUpdate() > 0;
            System.out.println("assignCouponToUserByCode: assigning couponId=" + couponId + " to userId=" + userId + " result=" + ok);
            return ok;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Convenience method: ensure the WELCOME10 is assigned to the user (10% discount)
     */
    public boolean ensureWelcomeAssigned(int userId) {
        return assignCouponToUserByCode(userId, "WELCOME10", 10.0);
    }

    /**
     * Debug helper: print counts of coupon tables
     */
    public void debugPrintCounts() {
        String sql1 = "SELECT COUNT(*) AS c FROM couponinfo";
        String sql2 = "SELECT COUNT(*) AS c FROM user_coupons";
        try (Connection conn = DatabaseAdapter.getConnection();
             Statement s = conn.createStatement()) {
            try (ResultSet r1 = s.executeQuery(sql1)) {
                if (r1.next()) System.out.println("couponinfo count=" + r1.getInt("c"));
            }
            try (ResultSet r2 = s.executeQuery(sql2)) {
                if (r2.next()) System.out.println("user_coupons count=" + r2.getInt("c"));
            }
        } catch (SQLException e) {
            System.out.println("debugPrintCounts: failed to query counts: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Returns true if coupon exists and is active
     */
    public boolean couponExists(String code) {
        String sql = "SELECT id FROM couponinfo WHERE UPPER(code) = UPPER(?) AND is_active = 1 LIMIT 1";
        try (Connection conn = DatabaseAdapter.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Marks a user's assigned coupon as redeemed (if present and not already redeemed).
     * Returns true if a row was updated.
     */
    public boolean redeemUserCoupon(int userId, String code) {
        String sql = "UPDATE user_coupons uc JOIN couponinfo c ON uc.coupon_id = c.id " +
                 "SET uc.redeemed = 1 WHERE uc.user_id = ? AND UPPER(c.code) = UPPER(?) AND uc.redeemed = 0";
        try (Connection conn = DatabaseAdapter.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, code);
            int updated = ps.executeUpdate();
            System.out.println("redeemUserCoupon: userId=" + userId + " code=" + code + " updated=" + updated);
            return updated > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
