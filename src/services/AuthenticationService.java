package services; 

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import models.User;

public class AuthenticationService {
    
    private UserDAO userDAO;

    public AuthenticationService() {
        this.userDAO = new UserDAO();
    }

    // Giriş İşlemi
    public User authenticate(String username, String password) {
        // DİKKAT LORDUM: Veritabanındaki varsayılan kullanıcıların (cust, carr) şifreleri
        // şifrelenmemiş (plain text). O yüzden şimdilik düz gönderiyoruz.
        // Eğer tüm sistemi şifreli yaparsak burayı: hashPassword(password) yapacağız.
        return userDAO.login(username, password); 
    }

    // Şifreleme Metodu (SHA-256) - Register ekranında bunu kullanacağız
    public static String hashPassword(String originalPassword) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(originalPassword.getBytes(StandardCharsets.UTF_8));
            
            // Byte'ları Hex String'e çevirme
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
            return originalPassword; // Hata olursa şifrelemeden döndür
        }
    }

    
}