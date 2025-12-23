
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ReportGenerator class for GreenGrocer Application.
 * Generates various reports and statistics for the owner.
 * Reports include: sales by product, time-based sales, revenue analysis.
 * 
 * @author GroupXX
 * @version 1.0
 */
public class ReportGenerator {

    /**
     * Sales data for a single product.
     */
    public static class ProductSalesData {
        private String productName;
        private double totalQuantity;
        private double totalRevenue;
        private int orderCount;
        
        public ProductSalesData(String productName) {
            this.productName = productName;
            this.totalQuantity = 0;
            this.totalRevenue = 0;
            this.orderCount = 0;
        }
        
        public void addSale(double quantity, double revenue) {
            this.totalQuantity += quantity;
            this.totalRevenue += revenue;
            this.orderCount++;
        }
        
        public String getProductName() { return productName; }
        public double getTotalQuantity() { return totalQuantity; }
        public double getTotalRevenue() { return totalRevenue; }
        public int getOrderCount() { return orderCount; }
        public double getAveragePrice() { 
            return totalQuantity > 0 ? totalRevenue / totalQuantity : 0; 
        }
    }

    /**
     * Time-based sales data.
     */
    public static class TimePeriodSales {
        private String period; // "2024-12", "2024-W50", etc.
        private double revenue;
        private int orderCount;
        private int customerCount;
        
        public TimePeriodSales(String period) {
            this.period = period;
            this.revenue = 0;
            this.orderCount = 0;
            this.customerCount = 0;
        }
        
        public void addOrder(double orderTotal) {
            this.revenue += orderTotal;
            this.orderCount++;
        }
        
        public String getPeriod() { return period; }
        public double getRevenue() { return revenue; }
        public int getOrderCount() { return orderCount; }
        public int getCustomerCount() { return customerCount; }
        public void setCustomerCount(int count) { this.customerCount = count; }
    }

    /**
     * Generates product-based sales report.
     * 
     * @param orders List of orders
     * @return Map of product name to sales data
     */
    public static Map<String, ProductSalesData> generateProductSalesReport(List<Order> orders) {
        Map<String, ProductSalesData> salesMap = new HashMap<>();
        
        for (Order order : orders) {
            if (order.getStatus() != Order.OrderStatus.DELIVERED) {
                continue; // Only count delivered orders
            }
            
            for (Order.OrderItem item : order.getItems()) {
                String productName = item.getProductName();
                
                salesMap.putIfAbsent(productName, new ProductSalesData(productName));
                salesMap.get(productName).addSale(item.getAmount(), item.getTotalPrice());
            }
        }
        
        return salesMap;
    }

    /**
     * Generates monthly sales report.
     * 
     * @param orders List of orders
     * @return Map of month to sales data
     */
    public static Map<String, TimePeriodSales> generateMonthlySalesReport(List<Order> orders) {
        Map<String, TimePeriodSales> monthlySales = new HashMap<>();
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("yyyy-MM");
        
        for (Order order : orders) {
            if (order.getStatus() != Order.OrderStatus.DELIVERED) {
                continue;
            }
            
            String month = order.getOrderTime().format(monthFormatter);
            monthlySales.putIfAbsent(month, new TimePeriodSales(month));
            monthlySales.get(month).addOrder(order.getTotalCost());
        }
        
        return monthlySales;
    }

    /**
     * Generates daily sales report for a specific month.
     * 
     * @param orders List of orders
     * @param year Year
     * @param month Month (1-12)
     * @return Map of day to sales data
     */
    public static Map<String, TimePeriodSales> generateDailySalesReport(
            List<Order> orders, int year, int month) {
        Map<String, TimePeriodSales> dailySales = new HashMap<>();
        DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        for (Order order : orders) {
            if (order.getStatus() != Order.OrderStatus.DELIVERED) {
                continue;
            }
            
            LocalDateTime orderTime = order.getOrderTime();
            if (orderTime.getYear() == year && orderTime.getMonthValue() == month) {
                String day = orderTime.format(dayFormatter);
                dailySales.putIfAbsent(day, new TimePeriodSales(day));
                dailySales.get(day).addOrder(order.getTotalCost());
            }
        }
        
        return dailySales;
    }

    /**
     * Gets top selling products (by revenue).
     * 
     * @param orders List of orders
     * @param limit Number of top products to return
     * @return List of top selling products
     */
    public static List<ProductSalesData> getTopSellingProducts(List<Order> orders, int limit) {
        Map<String, ProductSalesData> salesMap = generateProductSalesReport(orders);
        
        return salesMap.values().stream()
            .sorted((p1, p2) -> Double.compare(p2.getTotalRevenue(), p1.getTotalRevenue()))
            .limit(limit)
            .collect(Collectors.toList());
    }

    /**
     * Gets top selling products by quantity.
     * 
     * @param orders List of orders
     * @param limit Number of top products to return
     * @return List of top selling products by quantity
     */
    public static List<ProductSalesData> getTopSellingProductsByQuantity(
            List<Order> orders, int limit) {
        Map<String, ProductSalesData> salesMap = generateProductSalesReport(orders);
        
        return salesMap.values().stream()
            .sorted((p1, p2) -> Double.compare(p2.getTotalQuantity(), p1.getTotalQuantity()))
            .limit(limit)
            .collect(Collectors.toList());
    }

    /**
     * Calculates total revenue for a time period.
     * 
     * @param orders List of orders
     * @param startDate Start date (inclusive)
     * @param endDate End date (inclusive)
     * @return Total revenue
     */
    public static double calculateTotalRevenue(List<Order> orders, 
                                               LocalDateTime startDate, 
                                               LocalDateTime endDate) {
        return orders.stream()
            .filter(o -> o.getStatus() == Order.OrderStatus.DELIVERED)
            .filter(o -> !o.getOrderTime().isBefore(startDate))
            .filter(o -> !o.getOrderTime().isAfter(endDate))
            .mapToDouble(Order::getTotalCost)
            .sum();
    }

    /**
     * Calculates average order value.
     * 
     * @param orders List of orders
     * @return Average order value
     */
    public static double calculateAverageOrderValue(List<Order> orders) {
        List<Order> deliveredOrders = orders.stream()
            .filter(o -> o.getStatus() == Order.OrderStatus.DELIVERED)
            .collect(Collectors.toList());
        
        if (deliveredOrders.isEmpty()) {
            return 0.0;
        }
        
        double totalRevenue = deliveredOrders.stream()
            .mapToDouble(Order::getTotalCost)
            .sum();
        
        return totalRevenue / deliveredOrders.size();
    }

    /**
     * Generates a comprehensive sales report.
     * 
     * @param orders List of all orders
     * @return Formatted report as string
     */
    public static String generateComprehensiveReport(List<Order> orders) {
        StringBuilder report = new StringBuilder();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        
        report.append("================================================================\n");
        report.append("                   COMPREHENSIVE SALES REPORT                   \n");
        report.append("================================================================\n\n");
        
        // Overall statistics
        int totalOrders = orders.size();
        long deliveredOrders = orders.stream()
            .filter(o -> o.getStatus() == Order.OrderStatus.DELIVERED)
            .count();
        long pendingOrders = orders.stream()
            .filter(o -> o.getStatus() == Order.OrderStatus.PENDING || 
                        o.getStatus() == Order.OrderStatus.ASSIGNED ||
                        o.getStatus() == Order.OrderStatus.IN_DELIVERY)
            .count();
        long cancelledOrders = orders.stream()
            .filter(o -> o.getStatus() == Order.OrderStatus.CANCELLED)
            .count();
        
        double totalRevenue = orders.stream()
            .filter(o -> o.getStatus() == Order.OrderStatus.DELIVERED)
            .mapToDouble(Order::getTotalCost)
            .sum();
        
        double averageOrderValue = calculateAverageOrderValue(orders);
        
        report.append("OVERALL STATISTICS:\n");
        report.append("----------------------------------------------------------------\n");
        report.append("Total Orders: ").append(totalOrders).append("\n");
        report.append("  - Delivered: ").append(deliveredOrders).append("\n");
        report.append("  - Pending/In Progress: ").append(pendingOrders).append("\n");
        report.append("  - Cancelled: ").append(cancelledOrders).append("\n");
        report.append(String.format("Total Revenue: %.2f TL\n", totalRevenue));
        report.append(String.format("Average Order Value: %.2f TL\n", averageOrderValue));
        report.append("\n");
        
        // Top 10 products by revenue
        report.append("TOP 10 PRODUCTS (BY REVENUE):\n");
        report.append("----------------------------------------------------------------\n");
        report.append(String.format("%-25s %12s %12s %10s\n", 
            "Product", "Quantity", "Revenue", "Orders"));
        report.append("----------------------------------------------------------------\n");
        
        List<ProductSalesData> topProducts = getTopSellingProducts(orders, 10);
        for (ProductSalesData data : topProducts) {
            report.append(String.format("%-25s %10.2f kg %10.2f TL %10d\n",
                truncate(data.getProductName(), 25),
                data.getTotalQuantity(),
                data.getTotalRevenue(),
                data.getOrderCount()));
        }
        report.append("\n");
        
        // Monthly sales trend
        report.append("MONTHLY SALES TREND:\n");
        report.append("----------------------------------------------------------------\n");
        report.append(String.format("%-10s %15s %10s\n", "Month", "Revenue", "Orders"));
        report.append("----------------------------------------------------------------\n");
        
        Map<String, TimePeriodSales> monthlySales = generateMonthlySalesReport(orders);
        List<String> sortedMonths = new ArrayList<>(monthlySales.keySet());
        Collections.sort(sortedMonths);
        
        for (String month : sortedMonths) {
            TimePeriodSales data = monthlySales.get(month);
            report.append(String.format("%-10s %13.2f TL %10d\n",
                month, data.getRevenue(), data.getOrderCount()));
        }
        
        report.append("================================================================\n");
        report.append("Report Generated: ").append(LocalDateTime.now().format(formatter)).append("\n");
        
        return report.toString();
    }

    /**
     * Generates a simple summary report for dashboard.
     * 
     * @param orders List of orders
     * @return Summary report as string
     */
    public static String generateDashboardSummary(List<Order> orders) {
        StringBuilder summary = new StringBuilder();
        
        summary.append("=== DASHBOARD SUMMARY ===\n\n");
        
        // Today's stats
        LocalDateTime today = LocalDateTime.now();
        LocalDateTime startOfDay = today.withHour(0).withMinute(0).withSecond(0);
        
        long todayOrders = orders.stream()
            .filter(o -> o.getOrderTime().isAfter(startOfDay))
            .filter(o -> o.getStatus() == Order.OrderStatus.DELIVERED)
            .count();
        
        double todayRevenue = orders.stream()
            .filter(o -> o.getOrderTime().isAfter(startOfDay))
            .filter(o -> o.getStatus() == Order.OrderStatus.DELIVERED)
            .mapToDouble(Order::getTotalCost)
            .sum();
        
        long pendingCount = orders.stream()
            .filter(o -> o.getStatus() == Order.OrderStatus.PENDING ||
                        o.getStatus() == Order.OrderStatus.ASSIGNED)
            .count();
        
        summary.append("TODAY:\n");
        summary.append("  Orders: ").append(todayOrders).append("\n");
        summary.append(String.format("  Revenue: %.2f TL\n", todayRevenue));
        summary.append("\nPENDING ORDERS: ").append(pendingCount).append("\n");
        
        return summary.toString();
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
}