package models;

/**
 * User Model - Professional Version
 * Represents a user in the GreenGrocer system.
 * This model corresponds to the 'userinfo' table in the database.
 *
 * <p>Roles:</p>
 * <ul>
 * <li><b>owner:</b> System administrator, manages everything.</li>
 * <li><b>carrier:</b> Delivery person, handles orders.</li>
 * <li><b>customer:</b> Regular user, places orders.</li>
 * </ul>
 *
 * @author Group04
 * @version 2.0
 */
public class User {

    private int id;
    private String username;
    private String password; // Added for authentication compatibility
    private String role;
    private String address;
    private String phone;

    /**
     * Full Constructor.
     * Matches the database structure and includes the password for authentication.
     *
     * @param id       User ID (auto-generated in database)
     * @param username Unique username
     * @param password User's password (hashed or plain text)
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

    /**
     * Gets the unique ID of the user.
     * @return the user ID.
     */
    public int getId() {
        return id;
    }

    /**
     * Gets the username.
     * @return the username.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Gets the password.
     * Important for authentication service and registration flow.
     * @return the password string.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Gets the user's role.
     * @return the role (customer, carrier, or owner).
     */
    public String getRole() {
        return role;
    }

    /**
     * Gets the user's physical address.
     * @return the address string.
     */
    public String getAddress() {
        return address;
    }

    /**
     * Gets the user's phone number.
     * @return the phone number string.
     */
    public String getPhone() {
        return phone;
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
     * Gets a display-friendly version of the username.
     * Capitalizes the first letter (e.g., "ahmet" -> "Ahmet").
     *
     * @return The capitalized username.
     */
    public String getDisplayName() {
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

    /**
     * Returns a string representation of the User object.
     * Useful for debugging and logging.
     * Note: Password is excluded from the string for security.
     *
     * @return A string containing user details.
     */
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", role='" + role + '\'' +
                ", address='" + (address != null ? address.substring(0, Math.min(20, address.length())) + "..." : "N/A") + '\'' +
                ", phone='" + (phone != null ? phone : "N/A") + '\'' +
                '}';
    }

    /**
     * Checks if two User objects are equal based on their ID.
     *
     * @param o The object to compare with.
     * @return true if IDs match, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id == user.id;
    }

    /**
     * Generates a hash code for the User object based on ID.
     *
     * @return The hash code.
     */
    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}