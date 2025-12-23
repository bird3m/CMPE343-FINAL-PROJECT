

import java.time.LocalDateTime;

/**
 * Test class for Shopping Cart and Order functionality.
 * Run this to verify everything works before integrating with UI.
 * 
 * @author Group04
 * @version 1.0
 */
public class TestMain {
    
    public static void main(String[] args) {
        System.out.println("==============================================");
        System.out.println("   GREENGROCER - SHOPPING CART TEST");
        System.out.println("==============================================\n");
        
        // Test 1: Input Validation
        testInputValidation();
        
        // Test 2: Shopping Cart with Merge Logic
        testShoppingCart();
        
        // Test 3: Order Creation
        testOrderCreation();
        
        // Test 4: Order Cancellation
        testOrderCancellation();
        
        // Test 5: Threshold Pricing
        testThresholdPricing();
        
        System.out.println("\n==============================================");
        System.out.println("   ALL TESTS COMPLETED!");
        System.out.println("==============================================");
    }
    
    // ==================== TEST 1: Input Validation ====================
    
    public static void testInputValidation() {
        System.out.println("\n--- TEST 1: Input Validation ---\n");
        
        InputValidation validator = new InputValidation();
        
        // Test valid amount
        System.out.println("Testing valid amount (2.5):");
        boolean result1 = validator.validateProductAmount("2.5");
        System.out.println("Result: " + (result1 ? "✓ PASS" : "✗ FAIL") + "\n");
        
        // Test negative amount
        System.out.println("Testing negative amount (-1):");
        boolean result2 = validator.validateProductAmount("-1");
        System.out.println("Result: " + (!result2 ? "✓ PASS (correctly rejected)" : "✗ FAIL") + "\n");
        
        // Test zero amount
        System.out.println("Testing zero amount (0):");
        boolean result3 = validator.validateProductAmount("0");
        System.out.println("Result: " + (!result3 ? "✓ PASS (correctly rejected)" : "✗ FAIL") + "\n");
        
        // Test non-numeric input
        System.out.println("Testing non-numeric input (abc):");
        boolean result4 = validator.validateProductAmount("abc");
        System.out.println("Result: " + (!result4 ? "✓ PASS (correctly rejected)" : "✗ FAIL") + "\n");
        
        // Test password validation
        System.out.println("Testing strong password (Test123!):");
        boolean result5 = validator.validatePassword("Test123!");
        System.out.println("Result: " + (result5 ? "✓ PASS" : "✗ FAIL") + "\n");
        
        // Test weak password
        System.out.println("Testing weak password (weak):");
        boolean result6 = validator.validatePassword("weak");
        System.out.println("Result: " + (!result6 ? "✓ PASS (correctly rejected)" : "✗ FAIL") + "\n");
    }
    
    // ==================== TEST 2: Shopping Cart ====================
    
    public static void testShoppingCart() {
        System.out.println("\n--- TEST 2: Shopping Cart with MERGE Logic ---\n");
        
        // Create products
        Product tomato = new Product(1, "Tomato", "vegetable", 15.0, 100, 5, "tomato.jpg");
        Product cucumber = new Product(2, "Cucumber", "vegetable", 12.0, 50, 10, "cucumber.jpg");
        Product apple = new Product(3, "Apple", "fruit", 20.0, 80, 8, "apple.jpg");
        
        // Create shopping cart
        ShoppingCart cart = new ShoppingCart(1001);
        
        // Add items
        System.out.println("Adding 1.25 kg tomatoes:");
        cart.addItem(tomato, 1.25);
        
        System.out.println("\nAdding 0.75 kg tomatoes (should MERGE):");
        cart.addItem(tomato, 0.75);
        
        System.out.println("\nAdding 2 kg cucumbers:");
        cart.addItem(cucumber, 2.0);
        
        System.out.println("\nAdding 3 kg apples:");
        cart.addItem(apple, 3.0);
        
        // Test invalid amount
        System.out.println("\nTrying to add -1 kg cucumbers (should fail):");
        cart.addItem(cucumber, -1);
        
        // Test stock overflow
        System.out.println("\nTrying to add 200 kg tomatoes (exceeds stock, should fail):");
        cart.addItem(tomato, 200);
        
        // Apply coupon
        System.out.println("\nApplying 10% discount coupon:");
        cart.applyCoupon("SUMMER10", 10);
        
        // Set loyalty discount
        System.out.println("\nApplying 5% loyalty discount:");
        cart.setLoyaltyDiscount(5);
        
        // Display cart
        System.out.println("\n" + cart.getCartSummary());
        
        // Check minimum value
        System.out.println("Cart meets minimum value: " + (cart.meetsMinimumValue() ? "✓ YES" : "✗ NO"));
    }
    
    // ==================== TEST 3: Order Creation ====================
    
    public static void testOrderCreation() {
        System.out.println("\n--- TEST 3: Order Creation ---\n");
        
        // Create products and cart
        Product potato = new Product(4, "Potato", "vegetable", 8.0, 200, 20, "potato.jpg");
        Product onion = new Product(5, "Onion", "vegetable", 10.0, 150, 15, "onion.jpg");
        
        ShoppingCart cart = new ShoppingCart(1002);
        cart.addItem(potato, 5.0);
        cart.addItem(onion, 3.0);
        cart.applyCoupon("NEW15", 15);
        
        System.out.println("Cart created with items:");
        System.out.println(cart.getCartSummary());
        
        // Create order
        LocalDateTime deliveryTime = LocalDateTime.now().plusHours(24);
        
        try {
            Order order = new Order(
                cart,
                deliveryTime,
                "Ahmet Yılmaz",
                "Kadıköy, Istanbul, Turkey - Detailed address here",
                "+90 555 123 4567"
            );
            
            order.setOrderId(1); // Normally set by database
            
            System.out.println("\n" + order.getOrderSummary());
            
            // Generate invoice
            System.out.println("\n--- INVOICE ---");
            System.out.println(order.generateInvoiceContent());
            
            // Simulate carrier workflow
            System.out.println("\n--- CARRIER WORKFLOW ---");
            System.out.println("Assigning carrier (ID: 2001):");
            order.assignCarrier(2001);
            
            System.out.println("\nStarting delivery:");
            order.startDelivery();
            
            System.out.println("\nCompleting delivery:");
            order.completeDelivery(LocalDateTime.now());
            
            System.out.println("\nFinal order status: " + order.getStatus());
            
        } catch (IllegalArgumentException e) {
            System.err.println("Order creation failed: " + e.getMessage());
        }
    }
    
    // ==================== TEST 4: Order Cancellation ====================
    
    public static void testOrderCancellation() {
        System.out.println("\n--- TEST 4: Order Cancellation ---\n");
        
        // Create a simple cart
        Product carrot = new Product(6, "Carrot", "vegetable", 7.0, 100, 10, "carrot.jpg");
        ShoppingCart cart = new ShoppingCart(1003);
        cart.addItem(carrot, 4.0);
        
        LocalDateTime deliveryTime = LocalDateTime.now().plusHours(30);
        
        Order order = new Order(
            cart,
            deliveryTime,
            "Ayşe Demir",
            "Beşiktaş, Istanbul, Turkey - Detailed address",
            "+90 555 987 6543"
        );
        
        order.setOrderId(2);
        
        System.out.println("Order created. Status: " + order.getStatus());
        System.out.println("Can be cancelled: " + (order.canBeCancelled() ? "✓ YES" : "✗ NO"));
        
        System.out.println("\nCancelling order:");
        boolean cancelled = order.cancelOrder();
        System.out.println("Result: " + (cancelled ? "✓ SUCCESS" : "✗ FAILED"));
        System.out.println("Final status: " + order.getStatus());
        
        // Try to cancel again
        System.out.println("\nTrying to cancel again (should fail):");
        boolean cancelled2 = order.cancelOrder();
        System.out.println("Result: " + (!cancelled2 ? "✓ Correctly prevented" : "✗ FAIL"));
    }
    
    // ==================== TEST 5: Threshold Pricing ====================
    
    public static void testThresholdPricing() {
        System.out.println("\n--- TEST 5: Threshold Pricing (Greedy Owner!) ---\n");
        
        // Product with low stock (triggers threshold)
        Product pepper = new Product(7, "Pepper", "vegetable", 25.0, 4.0, 5.0, "pepper.jpg");
        
        System.out.println("Pepper - Normal price: " + pepper.getPrice() + " TL/kg");
        System.out.println("Current stock: " + pepper.getStock() + " kg");
        System.out.println("Threshold: " + pepper.getThreshold() + " kg");
        System.out.println("Stock <= Threshold: " + pepper.isThresholdActive());
        System.out.println("Effective price: " + pepper.getEffectivePrice() + " TL/kg");
        System.out.println((pepper.isThresholdActive() ? "⚠️ PRICE DOUBLED!" : "✓ Normal pricing"));
        
        System.out.println("\n--- Adding to cart ---");
        ShoppingCart cart = new ShoppingCart(1004);
        cart.addItem(pepper, 2.0);
        
        System.out.println("\n" + cart.getCartSummary());
        
        System.out.println("\n--- Now testing with normal stock ---");
        Product eggplant = new Product(8, "Eggplant", "vegetable", 18.0, 50.0, 5.0, "eggplant.jpg");
        
        System.out.println("Eggplant - Normal price: " + eggplant.getPrice() + " TL/kg");
        System.out.println("Current stock: " + eggplant.getStock() + " kg");
        System.out.println("Threshold: " + eggplant.getThreshold() + " kg");
        System.out.println("Stock <= Threshold: " + eggplant.isThresholdActive());
        System.out.println("Effective price: " + eggplant.getEffectivePrice() + " TL/kg");
        System.out.println((eggplant.isThresholdActive() ? "⚠️ PRICE DOUBLED!" : "✓ Normal pricing"));
        
        cart.addItem(eggplant, 3.0);
        System.out.println("\n" + cart.getCartSummary());
    }
}
