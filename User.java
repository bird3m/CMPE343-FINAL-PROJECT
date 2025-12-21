public class User {
    private int id;
    private String username;
    private String password; // DB'de 'password_hash' ama kodda password diyebiliriz
    private String role; // customer, carrier, owner
    private String fullName; // DB: full_name
    private String address;
    private String phone;

    public User(int id, String username, String password, String role, String fullName, String address, String phone) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.fullName = fullName;
        this.address = address;
        this.phone = phone;
    }

    
    public int getId() { 
        return id; 
    }
    public String getUsername() { 
        return username;
    }
    public String getRole() {
        return role; 
    }
    public String getFullName() { 
        return fullName; 
    }
    
}