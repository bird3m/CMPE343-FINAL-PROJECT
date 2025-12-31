package models;

/**
 * User Model - Professional Version.
 * Represents a user in the GreenGrocer system.
 * This model corresponds to the 'userinfo' table in the database.
 * * <p>Roles:</p>
 * <ul>
 * <li><b>owner:</b> System administrator, manages everything.</li>
 * <li><b>carrier:</b> Delivery person, handles orders.</li>
 * <li><b>customer:</b> Regular user, places orders.</li>
 * </ul>
 *
 * @author Group04
 * @version 2.2 (Integrated Fix)
 */
public class User {

    private int id;
    private String username;
    private String password; 
    private String role;
    private String address;
    private String phone;
    
    /**
     * Full Name of the user.
     * Added to support profile features from the latest update.
     */
    private String fullName; 

    // ==================== CONSTRUCTORS ====================

    /**
     * No-Argument Constructor.
     * CRITICAL: Required for 'RegistrationService' and some DAO operations.
     */
    public User() {
    }

    /**
     * Full Constructor.
     * Matches the database structure including phone number.
     *
     * @param id       User ID (auto-generated in database)
     * @param username Unique username
     * @param password User's password
     * @param role     User role (owner, carrier, customer)
     * @param address  User's delivery address
     * @param phone    User's contact number
     */
    public User(int id, String username, String password, String role, String address, String phone) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.address = address;
        this.phone = phone;
    }

    // ==================== GETTERS ====================

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getRole() {
        return role;
    }

    public String getAddress() {
        return address;
    }

    public String getPhone() {
        return phone;
    }

    /**
     * Gets the full name of the user.
     * @return the full name string.
     */
    public String getFullName() { 
        return fullName; 
    }

    // ==================== SETTERS ====================

    public void setId(int id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    /**
     * Sets the full name of the user.
     * @param fullName The full name to set.
     */
    public void setFullName(String fullName) { 
        this.fullName = fullName; 
    }

    // ==================== UTILITY METHODS ====================

    /**
     * Checks if the user has administrative privileges (Owner).
     * @return true if the role is 'owner', false otherwise.
     */
    public boolean isOwner() {
        return "owner".equalsIgnoreCase(this.role);
    }

    /**
     * Checks if the user is a courier (Carrier).
     * @return true if the role is 'carrier', false otherwise.
     */
    public boolean isCarrier() {
        return "carrier".equalsIgnoreCase(this.role);
    }

    /**
     * Checks if the user is a standard customer.
     * @return true if the role is 'customer', false otherwise.
     */
    public boolean isCustomer() {
        return "customer".equalsIgnoreCase(this.role);
    }

    /**
     * Gets a display-friendly version of the user's name.
     * Priority: Full Name > Capitalized Username.
     *
     * @return The display name.
     */
    public String getDisplayName() {
        // If Full Name is available, use it (Prokaryot Feature)
        if (fullName != null && !fullName.isEmpty()) {
            return fullName;
        }
        
        // Fallback to your logic (Capitalized Username)
        if (username == null || username.isEmpty()) return "Unknown";
        return username.substring(0, 1).toUpperCase() + username.substring(1);
    }

    /**
     * Gets a display-friendly version of the role.
     * Capitalizes the first letter (e.g., "carrier" -> "Carrier").
     *
     * @return The capitalized role name.
     */
    public String getRoleDisplayName() {
        if (role == null || role.isEmpty()) return "Unknown";
        return role.substring(0, 1).toUpperCase() + role.substring(1);
    }

    // ==================== OBJECT OVERRIDES ====================

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", role='" + role + '\'' +
                ", fullName='" + fullName + '\'' +
                ", address='" + (address != null ? address.substring(0, Math.min(20, address.length())) + "..." : "N/A") + '\'' +
                ", phone='" + (phone != null ? phone : "N/A") + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id == user.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}