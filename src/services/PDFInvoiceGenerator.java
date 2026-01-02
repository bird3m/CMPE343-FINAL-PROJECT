package services;

import models.Order;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;

/**
 * Minimal PDF generator without external libraries.
 * Produces a simple single-page PDF compliant with PDF 1.4 spec.
 */
public class PDFInvoiceGenerator {

    public static byte[] generateInvoicePDF(Order order) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            int[] offsets = new int[6];

            // Header (LF only)
            write(baos, "%PDF-1.4\n");
            write(baos, "%\u00E2\u00E3\u00CF\u00D3\n");

            // 1: Catalog
            offsets[1] = baos.size();
            write(baos, "1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n");

            // 2: Pages
            offsets[2] = baos.size();
            write(baos, "2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n");

            // 3: Page
            offsets[3] = baos.size();
            write(baos, "3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] /Resources << /Font << /F1 4 0 R >> >> /Contents 5 0 R >>\nendobj\n");

            // 4: Font
            offsets[4] = baos.size();
            write(baos, "4 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>\nendobj\n");

            // 5: Content stream - build into a separate buffer using LF newlines
            java.io.ByteArrayOutputStream content = new java.io.ByteArrayOutputStream();
            int y = 750;
            java.util.List<String> lines = new java.util.ArrayList<>();
            lines.add("GREEN GROCER - OFFICIAL INVOICE");
            lines.add("----------------------------------------");
            lines.add("Order ID: #" + order.getId());
            lines.add("Customer: " + (order.getCustomerName() != null ? order.getCustomerName() : "Guest"));
            lines.add("Status:   " + (order.getStatus() != null ? order.getStatus() : ""));
            lines.add(String.format("Total:    %.2f TL", order.getTotalCost()));
            String date = (order.getDeliveryTime() != null) ? order.getDeliveryTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "ASAP";
            lines.add("Delivery: " + date);
            lines.add("");
            lines.add("Thank you for choosing Group04!");

            for (String l : lines) {
                String esc = escape(l);
                String lineCmd = "BT /F1 12 Tf 50 " + y + " Td (" + esc + ") Tj ET\n";
                content.write(lineCmd.getBytes(java.nio.charset.StandardCharsets.ISO_8859_1));
                y -= 14;
                if (y < 40) break;
            }

            byte[] contentBytes = content.toByteArray();

            offsets[5] = baos.size();
            baos.write(("5 0 obj\n<< /Length " + contentBytes.length + " >>\nstream\n").getBytes(java.nio.charset.StandardCharsets.ISO_8859_1));
            baos.write(contentBytes);
            baos.write("\nendstream\nendobj\n".getBytes(java.nio.charset.StandardCharsets.ISO_8859_1));

            // xref
            int xrefPos = baos.size();
            baos.write("xref\n0 6\n0000000000 65535 f \n".getBytes(java.nio.charset.StandardCharsets.ISO_8859_1));
            for (int i = 1; i <= 5; i++) {
                String off = String.format("%010d 00000 n \n", offsets[i]);
                baos.write(off.getBytes(java.nio.charset.StandardCharsets.ISO_8859_1));
            }

            baos.write(("trailer\n<< /Size 6 /Root 1 0 R >>\nstartxref\n" + xrefPos + "\n%%EOF\n").getBytes(java.nio.charset.StandardCharsets.ISO_8859_1));

            byte[] pdf = baos.toByteArray();
            return pdf;
        } catch (Exception e) {
            System.err.println("PDF generation failed: " + e.getMessage());
            return null;
        }
    }

    private static void write(ByteArrayOutputStream baos, String s) throws Exception {
        baos.write(s.getBytes(StandardCharsets.ISO_8859_1));
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
    }

    private static String buildContent(Order order) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        StringBuilder sb = new StringBuilder();
        
        sb.append("BT\r\n"); // Begin Text
        sb.append("/F1 14 Tf\r\n"); // Font Helvetica, Size 14
        
        int x = 50;
        int y = 750;
        int leading = 20;

        // Draw Lines
        writeText(sb, x, y, "GREEN GROCER - OFFICIAL INVOICE"); y -= leading * 2;
        writeText(sb, x, y, "----------------------------------------"); y -= leading;
        writeText(sb, x, y, "Order ID: #" + order.getId()); y -= leading;
        writeText(sb, x, y, "Customer: " + (order.getCustomerName() != null ? order.getCustomerName() : "Guest")); y -= leading;
        writeText(sb, x, y, "Status:   " + order.getStatus()); y -= leading;
        writeText(sb, x, y, String.format("Total:    %.2f TL", order.getTotalCost())); y -= leading;
        
        String date = (order.getDeliveryTime() != null) ? order.getDeliveryTime().format(dtf) : "ASAP";
        writeText(sb, x, y, "Delivery: " + date); y -= leading * 2;
        
        writeText(sb, x, y, "Thank you for choosing Group04!");
        
        sb.append("ET\r\n"); // End Text
        return sb.toString();
    }

    private static void writeText(StringBuilder sb, int x, int y, String text) {
        sb.append(String.format("1 0 0 1 %d %d Tm\r\n", x, y)); // Move text cursor
        sb.append("(").append(escape(text)).append(") Tj\r\n"); // Show text
    }
}