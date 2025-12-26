package models;

public class User {
    private int id;
    private String username;
    private String password; 
    private String role;    
    private String fullName;
    private String address;
    private String phone;

    // 1. FULL CONSTRUCTOR (DB'den çekerken)
    public User(int id, String username, String password, String role, String fullName, String address, String phone) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.fullName = fullName;
        this.address = address;
        this.phone = phone;
    }

    public User(int id, String username, String role, String fullName, String address, String phone) {
    this.id = id;
    this.username = username;
    this.role = role;
    this.fullName = fullName;
    this.address = address;
    this.phone = phone;
}

    // 2. REGISTRATION CONSTRUCTOR (Yeni kayıt)
    public User(String username, String password, String role, String fullName, String address, String phone) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.fullName = fullName;
        this.address = address;
        this.phone = phone;
    }

    // --- GETTERS ---
    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getRole() { return role; }
    public String getFullName() { return fullName; }
    public String getAddress() { return address; }
    public String getPhone() { return phone; }

    // --- SETTERS (STRICT VALIDATION & ENGLISH MESSAGES) ---

    public void setId(int id) { this.id = id; }
    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
    public void setRole(String role) { this.role = role; }

    public void setFullName(String fullName) { 
        // RULE: Only Letters (English+Turkish), spaces, hyphens (-), apostrophes (').
        // NO NUMBERS, NO SYMBOLS like { } ; *
        String regex = "^[a-zA-ZğüşıöçĞÜŞİÖÇ\\s\\-\\']+$";
        
        if (fullName != null && fullName.trim().length() >= 3 && fullName.matches(regex)) {
            this.fullName = fullName; 
        } else {
            throw new IllegalArgumentException("Invalid Name! No numbers or special symbols ({, }, ;, * etc.) allowed. Must be at least 3 chars.");
        }
    }

    public void setAddress(String address) { 
        // RULE: Min 10 chars. Only Letters, Numbers, Space, and / - , . :
        String regex = "^[a-zA-ZğüşıöçĞÜŞİÖÇ0-9\\s\\/\\-\\,\\.\\:]+$";

        if (address != null && address.trim().length() >= 10) {
            if (address.matches(regex)) {
                this.address = address; 
            } else {
                throw new IllegalArgumentException("Address contains invalid symbols! Only letters, numbers and / - , . : are allowed.");
            }
        } else {
            throw new IllegalArgumentException("Address too short! Please enter a full address (Min 10 chars).");
        }
    }

    public void setPhone(String phone) { 
        // RULE: Exactly 10 or 11 digits.
        if (phone != null && phone.matches("\\d{10,11}")) {
            this.phone = phone; 
        } else {
            throw new IllegalArgumentException("Invalid Phone Format! Please enter 10 or 11 digits only (e.g., 05551234567).");
        }
    }
    
    @Override
    public String toString() {
        return username + " (" + role + ")";
    }

    // CarrierMainController bu metodu arıyor!
    public String getDisplayName() {
        if (username == null) return "Unknown.";
        return username.substring(0, 1).toUpperCase() + username.substring(1);
    }
}
