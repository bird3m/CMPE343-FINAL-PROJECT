import services.PDFInvoiceGenerator;
import models.Order;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Small test utility to generate an invoice PDF for a sample order.
 * This is used for quick manual testing and debugging.
 */
public class TestPdf {
    public static void main(String[] args) {
        Order order = new Order(24, 1, "Test Customer", 0, "DELIVERED", LocalDateTime.now(ZoneId.of("Europe/Istanbul")), 123.45);
        byte[] pdf = PDFInvoiceGenerator.generateInvoicePDF(order);
        if (pdf == null) {
            System.err.println("PDF generation returned null");
            return;
        }
        // PDF generated for manual inspection during development
    }
}
