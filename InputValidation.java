package utils;

import java.util.regex.Pattern;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Input validation class for GreenGrocer Application.
 * Handles validation for customer, carrier, and owner interfaces.
 * 
 * @version 1.0
 */
public class InputValidation {

    // ==================== GENERAL VALIDATIONS ====================
    
    /**
     * Validates username input. Must be 3-50 characters, no spaces.
     * 
     * @param username The username to validate
     * @return true if valid, false otherwise
     */
    public boolean validateUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            System.err.println("Username cannot be empty!");
            return false;
        }

        if (username.contains(" ") || username.contains("\t")) {
            System.err.println("Username cannot contain spaces!");
            return false;
        }

        if (username.length() < 3 || username.length() > 50) {
            System.err.println("Username must be between 3 and 50 characters!");
            return false;
        }

        return true;
    }

    /**
     * Validates password strength according to project requirements.
     * - Minimum 2 characters, maximum 50
     * - No spaces allowed
     * 
     * @param password The password to validate
     * @return true if password meets requirements, false otherwise
     */
    public boolean validatePassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            System.err.println("Password cannot be empty!");
            return false;
        }

        if (password.contains(" ") || password.contains("\t")) {
            System.err.println("Password cannot contain spaces!");
            return false;
        }

        if (password.length() < 2 || password.length() > 50) {
            System.err.println("Password must be between 2 and 50 characters!");
            return false;
        }

        return true;
    }

    // ==================== PRODUCT AMOUNT VALIDATIONS ====================
    
    /**
     * Validates product amount input (for customer cart).
     * Must be a positive double value (e.g., 0.5, 1, 2.25).
     * 
     * @param amountStr The amount as string from TextField
     * @return true if valid, false otherwise
     */
    public boolean validateProductAmount(String amountStr) {
        if (amountStr == null || amountStr.trim().isEmpty()) {
            System.err.println("Amount cannot be empty!");
            return false;
        }

        if (amountStr.contains(" ") || amountStr.contains("\t")) {
            System.err.println("Amount cannot contain spaces!");
            return false;
        }

        try {
            double amount = Double.parseDouble(amountStr);
            return validateProductAmount(amount);
        } catch (NumberFormatException e) {
            System.err.println("Please enter a valid number (e.g., 0.5, 1, 2.25)!");
            return false;
        }
    }

    /**
     * Validates product amount as double value.
     * Must be positive and greater than zero.
     * 
     * @param amount The amount in kilograms
     * @return true if valid, false otherwise
     */
    public boolean validateProductAmount(double amount) {
        if (amount <= 0) {
            System.err.println("Amount must be greater than zero!");
            return false;
        }

        if (amount > 1000) {
            System.err.println("Amount cannot exceed 1000 kg!");
            return false;
        }

        return true;
    }

    // ==================== STOCK VALIDATIONS ====================
    
    /**
     * Validates if requested amount is available in stock.
     * 
     * @param requestedAmount Amount customer wants to buy
     * @param availableStock Current stock amount
     * @return true if enough stock available, false otherwise
     */
    public boolean validateStockAvailability(double requestedAmount, double availableStock) {
        if (availableStock <= 0) {
            System.err.println("Product is out of stock!");
            return false;
        }

        if (requestedAmount > availableStock) {
            System.err.println("Insufficient stock! Available: " + availableStock + " kg");
            return false;
        }

        return true;
    }

    /**
     * Validates threshold value for owner interface.
     * Threshold must be positive.
     * 
     * @param thresholdStr The threshold value as string
     * @return true if valid, false otherwise
     */
    public boolean validateThreshold(String thresholdStr) {
        if (thresholdStr == null || thresholdStr.trim().isEmpty()) {
            System.err.println("Threshold cannot be empty!");
            return false;
        }

        try {
            double threshold = Double.parseDouble(thresholdStr);
            return validateThreshold(threshold);
        } catch (NumberFormatException e) {
            System.err.println("Please enter a valid threshold value!");
            return false;
        }
    }

    /**
     * Validates threshold value as double.
     * 
     * @param threshold The threshold value
     * @return true if valid, false otherwise
     */
    public boolean validateThreshold(double threshold) {
        if (threshold <= 0) {
            System.err.println("Threshold must be greater than zero!");
            return false;
        }

        if (threshold > 10000) {
            System.err.println("Threshold cannot exceed 10000 kg!");
            return false;
        }

        return true;
    }

    // ==================== PRICE VALIDATIONS ====================
    
    /**
     * Validates product price for owner interface.
     * 
     * @param priceStr The price as string
     * @return true if valid, false otherwise
     */
    public boolean validatePrice(String priceStr) {
        if (priceStr == null || priceStr.trim().isEmpty()) {
            System.err.println("Price cannot be empty!");
            return false;
        }

        try {
            double price = Double.parseDouble(priceStr);
            return validatePrice(price);
        } catch (NumberFormatException e) {
            System.err.println("Please enter a valid price!");
            return false;
        }
    }

    /**
     * Validates price value as double.
     * 
     * @param price The price value
     * @return true if valid, false otherwise
     */
    public boolean validatePrice(double price) {
        if (price <= 0) {
            System.err.println("Price must be greater than zero!");
            return false;
        }

        if (price > 100000) {
            System.err.println("Price seems unreasonably high!");
            return false;
        }

        return true;
    }

    /**
     * Validates minimum cart value requirement.
     * 
     * @param cartTotal Current cart total
     * @param minimumRequired Minimum required amount
     * @return true if minimum met, false otherwise
     */
    public boolean validateMinimumCartValue(double cartTotal, double minimumRequired) {
        if (cartTotal < minimumRequired) {
            System.err.println("Minimum cart value is " + minimumRequired + " TL. Current: " + cartTotal + " TL");
            return false;
        }
        return true;
    }

    // ==================== DATE/TIME VALIDATIONS ====================
    
    /**
     * Validates delivery date for customer orders.
     * Must be within 48 hours from now.
     * Format: yyyy-MM-dd HH:mm
     * 
     * @param deliveryDateStr The delivery date string
     * @return true if valid, false otherwise
     */
    public boolean validateDeliveryDate(String deliveryDateStr) {
        if (deliveryDateStr == null || deliveryDateStr.trim().isEmpty()) {
            System.err.println("Delivery date cannot be empty!");
            return false;
        }

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            LocalDateTime deliveryDate = LocalDateTime.parse(deliveryDateStr, formatter);
            return validateDeliveryDate(deliveryDate);
        } catch (DateTimeParseException e) {
            System.err.println("Invalid date format! Please use: yyyy-MM-dd HH:mm (e.g., 2025-12-25 14:30)");
            return false;
        }
    }

    /**
     * Validates delivery date as LocalDateTime.
     * Must be within 48 hours from current time.
     * 
     * @param deliveryDate The delivery date
     * @return true if valid, false otherwise
     */
    public boolean validateDeliveryDate(LocalDateTime deliveryDate) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime maxDelivery = now.plusHours(48);

        if (deliveryDate.isBefore(now)) {
            System.err.println("Delivery date cannot be in the past!");
            return false;
        }

        if (deliveryDate.isAfter(maxDelivery)) {
            System.err.println("Delivery date must be within 48 hours!");
            return false;
        }

        return true;
    }

    // ==================== TEXT FIELD VALIDATIONS ====================
    
    /**
     * Validates product name (no numbers, no special chars).
     * Used for filtering and searching products.
     * 
     * @param name The product name
     * @return true if valid, false otherwise
     */
    public boolean validateProductName(String name) {
        if (name == null || name.trim().isEmpty()) {
            System.err.println("Product name cannot be empty!");
            return false;
        }

        if (name.matches(".*[0-9].*")) {
            System.err.println("Product name cannot contain numbers!");
            return false;
        }

        if (name.matches(".*[^a-zA-ZğüşıöçĞÜŞİÖÇ ].*")) {
            System.err.println("Product name cannot contain special characters!");
            return false;
        }

        if (name.length() > 50) {
            System.err.println("Product name cannot exceed 50 characters!");
            return false;
        }

        return true;
    }

    /**
     * Validates address input for customer profile.
     * 
     * @param address The address string
     * @return true if valid, false otherwise
     */
    public boolean validateAddress(String address) {
        if (address == null || address.trim().isEmpty()) {
            System.err.println("Address cannot be empty!");
            return false;
        }

        if (address.length() < 10) {
            System.err.println("Please enter a complete address (minimum 10 characters)!");
            return false;
        }

        if (address.length() > 200) {
            System.err.println("Address cannot exceed 200 characters!");
            return false;
        }

        return true;
    }

    /**
     * Validates phone number input.
     * 
     * @param phoneNo The phone number
     * @return true if valid, false otherwise
     */
    public boolean validatePhoneNumber(String phoneNo) {
        if (phoneNo == null || phoneNo.trim().isEmpty()) {
            System.err.println("Phone number cannot be empty!");
            return false;
        }

        if (phoneNo.length() > 20) {
            System.err.println("Phone number cannot exceed 20 characters!");
            return false;
        }

        if (!phoneNo.matches("[\\d\\s+()-]+")) {
            System.err.println("Please enter a valid phone number!");
            return false;
        }

        return true;
    }

    // ==================== DISCOUNT/COUPON VALIDATIONS ====================
    
    /**
     * Validates discount coupon code.
     * 
     * @param couponCode The coupon code
     * @return true if format is valid, false otherwise
     */
    public boolean validateCouponCode(String couponCode) {
        if (couponCode == null || couponCode.trim().isEmpty()) {
            System.err.println("Coupon code cannot be empty!");
            return false;
        }

        if (couponCode.contains(" ") || couponCode.contains("\t")) {
            System.err.println("Coupon code cannot contain spaces!");
            return false;
        }

        if (couponCode.length() < 4 || couponCode.length() > 20) {
            System.err.println("Coupon code must be between 4 and 20 characters!");
            return false;
        }

        return true;
    }

    /**
     * Validates discount percentage value.
     * 
     * @param discountStr The discount percentage as string
     * @return true if valid, false otherwise
     */
    public boolean validateDiscountPercentage(String discountStr) {
        if (discountStr == null || discountStr.trim().isEmpty()) {
            System.err.println("Discount percentage cannot be empty!");
            return false;
        }

        try {
            double discount = Double.parseDouble(discountStr);
            return validateDiscountPercentage(discount);
        } catch (NumberFormatException e) {
            System.err.println("Please enter a valid discount percentage!");
            return false;
        }
    }

    /**
     * Validates discount percentage as double (0-100).
     * 
     * @param discount The discount percentage
     * @return true if valid, false otherwise
     */
    public boolean validateDiscountPercentage(double discount) {
        if (discount < 0 || discount > 100) {
            System.err.println("Discount must be between 0 and 100%!");
            return false;
        }
        return true;
    }

    // ==================== RATING VALIDATIONS ====================
    
    /**
     * Validates carrier rating (1-5 stars).
     * 
     * @param ratingStr The rating as string
     * @return true if valid, false otherwise
     */
    public boolean validateRating(String ratingStr) {
        if (ratingStr == null || ratingStr.trim().isEmpty()) {
            System.err.println("Rating cannot be empty!");
            return false;
        }

        try {
            int rating = Integer.parseInt(ratingStr);
            return validateRating(rating);
        } catch (NumberFormatException e) {
            System.err.println("Please enter a valid rating (1-5)!");
            return false;
        }
    }

    /**
     * Validates rating as integer (1-5).
     * 
     * @param rating The rating value
     * @return true if valid, false otherwise
     */
    public boolean validateRating(int rating) {
        if (rating < 1 || rating > 5) {
            System.err.println("Rating must be between 1 and 5!");
            return false;
        }
        return true;
    }

    // ==================== MESSAGE VALIDATIONS ====================
    
    /**
     * Validates message content for customer-owner communication.
     * 
     * @param message The message content
     * @return true if valid, false otherwise
     */
    public boolean validateMessage(String message) {
        if (message == null || message.trim().isEmpty()) {
            System.err.println("Message cannot be empty!");
            return false;
        }

        if (message.length() < 5) {
            System.err.println("Message is too short (minimum 5 characters)!");
            return false;
        }

        if (message.length() > 500) {
            System.err.println("Message cannot exceed 500 characters!");
            return false;
        }

        return true;
    }

    // ==================== INTEGER VALIDATIONS ====================
    
    /**
     * Validates positive integer input (for IDs, counts, etc.).
     * 
     * @param inputStr The integer as string
     * @return true if valid, false otherwise
     */
    public boolean validatePositiveInteger(String inputStr) {
        if (inputStr == null || inputStr.trim().isEmpty()) {
            System.err.println("Input cannot be empty!");
            return false;
        }

        if (inputStr.contains(" ") || inputStr.contains("\t")) {
            System.err.println("Input cannot contain spaces!");
            return false;
        }

        try {
            int value = Integer.parseInt(inputStr);
            if (value <= 0) {
                System.err.println("Value must be greater than zero!");
                return false;
            }
            return true;
        } catch (NumberFormatException e) {
            System.err.println("Please enter a valid integer!");
            return false;
        }
    }
}
