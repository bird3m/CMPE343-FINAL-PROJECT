package services;

import models.Order;
import java.time.format.DateTimeFormatter;

/**
 * PDFInvoiceGenerator - Fixed Syntax.
 * Generates formatted invoice content for database storage.
 */
public class PDFInvoiceGenerator {

    /**
     * Generates a simple text-based invoice for the order.
     */
    public static byte[] generateInvoicePDF(Order order) {
        try {
            StringBuilder sb = new StringBuilder();
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            sb.append("==========================================\n");
            sb.append("         GREEN GROCER - INVOICE           \n");
            sb.append("==========================================\n\n");
            sb.append("Order ID:    #").append(order.getId()).append("\n");
            sb.append("Customer:    ").append(order.getCustomerName()).append("\n");
            sb.append("Status:      ").append(order.getStatus()).append("\n");
            sb.append("Total Cost:  ").append(String.format("%.2f TL", order.getTotalCost())).append("\n");
            
            String dateStr = (order.getDeliveryTime() != null) ? order.getDeliveryTime().format(dtf) : "ASAP";
            sb.append("Delivery:    ").append(dateStr).append("\n\n");
            
            sb.append("------------------------------------------\n");
            sb.append("   Thank you for choosing Group04!        \n");
            sb.append("==========================================\n");

            return sb.toString().getBytes("UTF-8");
        } catch (Exception e) {
            System.err.println("PDF Generation Error: " + e.getMessage());
            return null;
        }
    }
}