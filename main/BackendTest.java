package main;

import java.util.Scanner;

public class BackendTest {
    public static void main(String[] args) {
        
        System.out.println("========== CRASH-PROOF PROFILE EDITOR ==========");
        
        Scanner scanner = new Scanner(System.in);
        AuthenticationService authService = new AuthenticationService();
        UserDAO userDAO = new UserDAO();

        System.out.println("--> Logging in as 'cust'...");
        User currentUser = authService.authenticate("cust", "cust");

        if (currentUser != null) {
            System.out.println("✅ LOGIN SUCCESSFUL!");
            System.out.println("------------------------------------------------");
            System.out.println("CURRENT INFO:");
            System.out.println("Name   : " + currentUser.getFullName());
            System.out.println("Phone  : " + currentUser.getPhone());
            System.out.println("------------------------------------------------");
            
            System.out.println("\n[Please Update Your Information]");

            // --- STEP 1: NAME INPUT LOOP ---
            while (true) {
                try {
                    System.out.print("New Full Name (Min 3 chars): ");
                    String input = scanner.nextLine();
                    currentUser.setFullName(input); // Throws error if invalid
                    break; // If successful, exit loop
                } catch (IllegalArgumentException e) {
                    System.out.println("⚠️ INPUT ERROR: " + e.getMessage());
                }
            }

            // --- STEP 2: ADDRESS INPUT LOOP ---
            while (true) {
                try {
                    System.out.print("New Address: ");
                    String input = scanner.nextLine();
                    currentUser.setAddress(input); // Throws error if empty
                    break;
                } catch (IllegalArgumentException e) {
                    System.out.println("⚠️ INPUT ERROR: " + e.getMessage());
                }
            }

            // --- STEP 3: PHONE INPUT LOOP (Critical Regex Check) ---
            while (true) {
                try {
                    System.out.print("New Phone (10-11 digits only): ");
                    String input = scanner.nextLine();
                    
                    // This creates the error seen in your screenshot if input is "adad"
                    currentUser.setPhone(input); 
                    
                    break; // Only breaks if setPhone does NOT throw an error
                } catch (IllegalArgumentException e) {
                    // We catch the bomb here! No crash!
                    System.out.println("⚠️ INPUT ERROR: " + e.getMessage());
                    System.out.println("-> Try again (e.g., 05551234567)");
                }
            }

            // --- DATABASE UPDATE ---
            System.out.println("\n--> Saving to Database...");
            boolean success = userDAO.updateProfile(currentUser);

            if (success) {
                System.out.println("✅ UPDATE SUCCESSFUL!");
                
                // Verification
                User verifiedUser = authService.authenticate("cust", "cust");
                System.out.println("\n--- VERIFIED DATA FROM DB ---");
                System.out.println("Name   : " + verifiedUser.getFullName());
                System.out.println("Phone  : " + verifiedUser.getPhone());
                
                if (verifiedUser.getPhone().equals(currentUser.getPhone())) {
                    System.out.println("\n🚀 RESULT: Perfect data persistence!");
                }
            } else {
                System.out.println("❌ ERROR: Database connection failed during update.");
            }

        } else {
            System.out.println("ERROR: Could not login.");
        }
        
        System.out.println("================================================");
        scanner.close();
    }
}