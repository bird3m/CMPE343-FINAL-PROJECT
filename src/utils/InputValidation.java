package utils;

import java.util.regex.Pattern;

/**
 * Input Validation Utility Class.
 * Centralizes all input validation logic using strict Regex patterns for security.
 * * @author Group04
 */
public class InputValidation {

    // --- REGEX PATTERNS ---
    
    // Username: 3-20 characters, starts with a letter, allows alphanumeric and underscore.
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z][a-zA-Z0-9_]{2,19}$");

    // Phone: Allows optional '+' at start, followed by 10 to 15 digits. 
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[0-9]{10,15}$");

    // Password: Min 2 chars, no whitespace allowed.
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^\\S{2,}$");

    /**
     * Validates the username using Regex.
     * Rule: Must start with a letter, 3-20 chars, alphanumeric or underscore only.
     * * @param username The input username.
     * @return Error message string if invalid, null if valid.
     */
    public static String validateUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return "Username cannot be empty.";
        }
        // Regex Check
        if (!USERNAME_PATTERN.matcher(username.trim()).matches()) {
            return "Username must start with a letter and use only letters, numbers, or '_'. (3-20 chars)";
        }
        return null; // Valid
    }

    /**
     * Validates the password.
     * Rule: At least 2 chars, no spaces.
     * * @param password The input password.
     * @return Error message string if invalid, null if valid.
     */
    public static String validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            return "Password cannot be empty.";
        }
        // Updated check for 2 characters
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            return "Password must be at least 2 characters and contain no spaces.";
        }
        return null;
    }

    /**
     * Validates the address.
     * Rule: Cannot be null/empty, must be meaningful (at least 5 chars).
     * * @param address The input address.
     * @return Error message string if invalid, null if valid.
     */
    public static String validateAddress(String address) {
        if (address == null || address.trim().isEmpty()) {
            return "Address cannot be empty.";
        }
        if (address.trim().length() < 10) {
            return "Address is too short. Please include Street, District, and City.";
        }
        return null;
    }

    /**
     * Validates the phone number using Regex.
     * Rule: Digits only (10-15), optional '+' at start.
     * * @param phone The input phone number.
     * @return Error message string if invalid, null if valid.
     */
    public static String validatePhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return "Phone number is required.";
        }
        // Remove spaces or dashes just in case user formatted it (e.g. 555 123)
        String cleanPhone = phone.replaceAll("[\\s-]", "");

        // Regex Check
        if (!PHONE_PATTERN.matcher(cleanPhone).matches()) {
            return "Phone must be 10-15 digits (e.g., 05551234567).";
        }
        return null;
    }

    /**
     * Validate full name
     * 
     * @param fullName Full name to validate
     * @return Error message or null if valid
     */
    public static String validateFullName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return "Full name cannot be empty";
        }
        
        fullName = fullName.trim();
        
        if (fullName.length() < 3) {
            return "Full name must be at least 3 characters";
        }
        
        if (fullName.length() > 50) {
            return "Full name must be at most 50 characters";
        }
        
        // Allow letters (including Turkish characters) and spaces
        if (!fullName.matches("^[a-zA-ZğüşıöçĞÜŞİÖÇ\\s]+$")) {
            return "Full name can only contain letters and spaces";
        }
        
        return null; // Valid
    }
}