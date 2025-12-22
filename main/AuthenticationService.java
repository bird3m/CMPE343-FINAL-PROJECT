package main;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class AuthenticationService {
    
    private UserDAO userDAO;

    public AuthenticationService() {
        this.userDAO = new UserDAO();
    }

    // Giriş İşlemi
    public User authenticate(String username, String password) {
        // Kullanıcının girdiği şifreyi önce HASH'liyoruz
        String hashedPassword = hashPassword(password);
        
        // Veritabanına hashlenmiş haliyle soruyoruz
        return userDAO.login(username, hashedPassword); 
    }

    // SHA-256 ile Şifreleme Metodu
    public String hashPassword(String originalPassword) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(originalPassword.getBytes(StandardCharsets.UTF_8));
            
            // Byte'ları Hex String'e çevirme (Veritabanındaki formatla aynı olmalı)
            StringBuilder hexString = new StringBuilder(2 * encodedhash.length);
            for (int i = 0; i < encodedhash.length; i++) {
                String hex = Integer.toHexString(0xff & encodedhash[i]);
                if(hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
}