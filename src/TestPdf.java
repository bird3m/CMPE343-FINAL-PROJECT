import services.PDFInvoiceGenerator;
import models.Order;
import java.time.LocalDateTime;

public class TestPdf {
    public static void main(String[] args) {
        Order order = new Order(24, 1, "Test Customer", 0, "DELIVERED", LocalDateTime.now(), 123.45);
        byte[] pdf = PDFInvoiceGenerator.generateInvoicePDF(order);
        if (pdf == null) {
            System.err.println("PDF generation returned null");
            return;
        }
        System.out.println("PDF generated: size=" + pdf.length);
        System.out.println("Look for files: Invoice_debug_" + order.getId() + ".pdf and .hex in project root");
    }
}
