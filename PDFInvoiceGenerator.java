
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

import models.Product;

/**
 * PDFInvoiceGenerator class for GreenGrocer Application.
 * Generates PDF invoices for orders (stored as BLOB in database).
 * 
 * Note: This is a simple text-based PDF generator.
 * For production, consider using libraries like iText or Apache PDFBox.
 * 
 * @author GroupXX
 * @version 1.0
 */
public class PDFInvoiceGenerator {

    /**
     * Generates a PDF invoice for an order.
     * Returns byte array to be stored in database as BLOB.
     * 
     * @param order The order object
     * @return Byte array of PDF content
     */
    public static byte[] generateInvoicePDF(Order order) {
        try {
            // Generate invoice content
            String content = generateInvoiceContent(order);
            
            // Convert to byte array (in real app, use PDF library)
            return content.getBytes("UTF-8");
            
        } catch (Exception e) {
            System.err.println("Error generating PDF: " + e.getMessage());
            return null;
        }
    }

    /**
     * Generates invoice content as formatted string.
     * 
     * @param order The order object
     * @return Formatted invoice content
     */
    private static String generateInvoiceContent(Order order) {
        StringBuilder invoice = new StringBuilder();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        
        // Header
        invoice.append("================================================================\n");
        invoice.append("                    GREENGROCER INVOICE                         \n");
        invoice.append("================================================================\n\n");
        
        // Invoice details
        invoice.append("Invoice Number: INV-").append(String.format("%06d", order.getOrderId())).append("\n");
        invoice.append("Invoice Date: ").append(java.time.LocalDateTime.now().format(formatter)).append("\n");
        invoice.append("Order Date: ").append(order.getOrderTime().format(formatter)).append("\n");
        invoice.append("Order ID: #").append(order.getOrderId()).append("\n\n");
        
        // Customer information
        invoice.append("----------------------------------------------------------------\n");
        invoice.append("CUSTOMER INFORMATION\n");
        invoice.append("----------------------------------------------------------------\n");
        invoice.append("Name: ").append(order.getCustomerName()).append("\n");
        invoice.append("Address: ").append(order.getCustomerAddress()).append("\n");
        invoice.append("Phone: ").append(order.getCustomerPhone()).append("\n\n");
        
        // Delivery information
        invoice.append("----------------------------------------------------------------\n");
        invoice.append("DELIVERY INFORMATION\n");
        invoice.append("----------------------------------------------------------------\n");
        invoice.append("Requested Delivery: ").append(order.getRequestedDeliveryTime().format(formatter)).append("\n");
        if (order.getActualDeliveryTime() != null) {
            invoice.append("Actual Delivery: ").append(order.getActualDeliveryTime().format(formatter)).append("\n");
        }
        invoice.append("Status: ").append(order.getStatus()).append("\n\n");
        
        // Items table
        invoice.append("================================================================\n");
        invoice.append("ITEMS\n");
        invoice.append("================================================================\n");
        invoice.append(String.format("%-25s %10s %12s %15s\n", 
            "Product", "Quantity", "Unit Price", "Total"));
        invoice.append("----------------------------------------------------------------\n");
        
        for (Order.OrderItem item : order.getItems()) {
            invoice.append(String.format("%-25s %8.2f kg %10.2f TL %13.2f TL\n",
                truncate(item.getProductName(), 25),
                item.getAmount(),
                item.getPricePerKg(),
                item.getTotalPrice()));
        }
        
        invoice.append("================================================================\n\n");
        
        // Pricing summary
        invoice.append("----------------------------------------------------------------\n");
        invoice.append("PRICING SUMMARY\n");
        invoice.append("----------------------------------------------------------------\n");
        invoice.append(String.format("%-40s %15.2f TL\n", "Subtotal:", order.getSubtotal()));
        invoice.append(String.format("%-40s %15.2f TL\n", "VAT (18%):", order.getVatAmount()));
        
        if (order.getDiscountAmount() > 0) {
            invoice.append(String.format("%-40s %15.2f TL\n", "Discount:", -order.getDiscountAmount()));
            
            if (order.getCouponDiscount() > 0) {
                invoice.append(String.format("  - Coupon (%s): %.0f%%\n", 
                    order.getCouponCode(), order.getCouponDiscount()));
            }
            if (order.getLoyaltyDiscount() > 0) {
                invoice.append(String.format("  - Loyalty Discount: %.0f%%\n", 
                    order.getLoyaltyDiscount()));
            }
        }
        
        invoice.append("----------------------------------------------------------------\n");
        invoice.append(String.format("%-40s %15.2f TL\n", "TOTAL:", order.getTotalCost()));
        invoice.append("================================================================\n\n");
        
        // Footer
        invoice.append("Payment Status: PAID\n");
        invoice.append("Payment Method: Cash on Delivery\n\n");
        
        invoice.append("----------------------------------------------------------------\n");
        invoice.append("           Thank you for shopping with GreenGrocer!             \n");
        invoice.append("                  Visit us again soon!                          \n");
        invoice.append("----------------------------------------------------------------\n\n");
        
        invoice.append("For questions or concerns, please contact:\n");
        invoice.append("Email: support@greengrocer.com\n");
        invoice.append("Phone: +90 212 555 0000\n\n");
        
        invoice.append("This is a computer-generated invoice.\n");
        invoice.append("Generated on: ").append(java.time.LocalDateTime.now().format(formatter)).append("\n");
        
        return invoice.toString();
    }

    /**
     * Saves invoice as PDF file to disk (for testing/debugging).
     * 
     * @param order The order object
     * @param filename The output filename
     * @return true if saved successfully, false otherwise
     */
    public static boolean saveInvoiceToFile(Order order, String filename) {
        try {
            byte[] pdfData = generateInvoicePDF(order);
            
            if (pdfData == null) {
                return false;
            }
            
            FileOutputStream fos = new FileOutputStream(filename);
            fos.write(pdfData);
            fos.close();
            
            System.out.println("✓ Invoice saved to: " + filename);
            return true;
            
        } catch (IOException e) {
            System.err.println("Error saving invoice: " + e.getMessage());
            return false;
        }
    }

    /**
     * Generates a simple receipt (shorter version of invoice).
     * 
     * @param order The order object
     * @return Receipt content as byte array
     */
    public static byte[] generateReceiptPDF(Order order) {
        try {
            StringBuilder receipt = new StringBuilder();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            
            receipt.append("========================================\n");
            receipt.append("       GREENGROCER RECEIPT              \n");
            receipt.append("========================================\n\n");
            
            receipt.append("Order #").append(order.getOrderId()).append("\n");
            receipt.append(order.getOrderTime().format(formatter)).append("\n\n");
            
            receipt.append("Customer: ").append(order.getCustomerName()).append("\n\n");
            
            receipt.append("Items:\n");
            receipt.append("----------------------------------------\n");
            
            for (Order.OrderItem item : order.getItems()) {
                receipt.append(String.format("%-20s %6.2f kg\n", 
                    truncate(item.getProductName(), 20), item.getAmount()));
                receipt.append(String.format("  %.2f TL/kg × %.2f = %.2f TL\n",
                    item.getPricePerKg(), item.getAmount(), item.getTotalPrice()));
            }
            
            receipt.append("========================================\n");
            receipt.append(String.format("Subtotal: %20.2f TL\n", order.getSubtotal()));
            receipt.append(String.format("VAT: %25.2f TL\n", order.getVatAmount()));
            
            if (order.getDiscountAmount() > 0) {
                receipt.append(String.format("Discount: %20.2f TL\n", -order.getDiscountAmount()));
            }
            
            receipt.append("========================================\n");
            receipt.append(String.format("TOTAL: %23.2f TL\n", order.getTotalCost()));
            receipt.append("========================================\n\n");
            
            receipt.append("Thank you for your purchase!\n");
            
            return receipt.toString().getBytes("UTF-8");
            
        } catch (Exception e) {
            System.err.println("Error generating receipt: " + e.getMessage());
            return null;
        }
    }

    /**
     * Helper method to truncate long strings.
     */
    private static String truncate(String str, int maxLength) {
        if (str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength - 3) + "...";
    }

    /**
     * Formats a double value as Turkish Lira currency.
     */
    private static String formatCurrency(double amount) {
        return String.format("%.2f TL", amount);
    }

    /**
     * Test method to demonstrate PDF generation.
     */
    public static void main(String[] args) {
        System.out.println("=== PDF Invoice Generator Test ===\n");
        
        // Create a sample order for testing
        ShoppingCart cart = new ShoppingCart(1001);
        
        Product tomato = new Product(1, "Tomato", "vegetable", 15.0, 100, 5, "tomato.jpg");
        Product cucumber = new Product(2, "Cucumber", "vegetable", 12.0, 50, 10, "cucumber.jpg");
        
        cart.addItem(tomato, 2.5);
        cart.addItem(cucumber, 1.5);
        cart.applyCoupon("SUMMER10", 10);
        
        Order order = new Order(
            cart,
            java.time.LocalDateTime.now().plusHours(24),
            "Ahmet Yılmaz",
            "Kadıköy Mah. Bahariye Cad. No:123 Kadıköy/İSTANBUL",
            "+90 555 123 4567"
        );
        
        order.setOrderId(1);
        
        // Generate and display invoice
        byte[] pdfData = generateInvoicePDF(order);
        
        if (pdfData != null) {
            System.out.println("Invoice generated successfully!");
            System.out.println("Size: " + pdfData.length + " bytes\n");
            
            // Display content
            try {
                System.out.println(new String(pdfData, "UTF-8"));
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            // Save to file
            saveInvoiceToFile(order, "invoice_" + order.getOrderId() + ".txt");
        } else {
            System.out.println("Failed to generate invoice!");
        }
    }
}