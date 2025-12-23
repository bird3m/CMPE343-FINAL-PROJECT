
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DeliveryManager class for GreenGrocer Application.
 * Manages carrier deliveries and order assignments.
 * 
 * @author Group04
 * @version 1.0
 */
public class DeliveryManager {

    /**
     * Gets available orders for carriers (PENDING status).
     * 
     * @param allOrders List of all orders
     * @return List of available orders
     */
    public List<Order> getAvailableOrders(List<Order> allOrders) {
        return allOrders.stream()
            .filter(o -> o.getStatus() == Order.OrderStatus.PENDING)
            .sorted((o1, o2) -> o1.getRequestedDeliveryTime().compareTo(o2.getRequestedDeliveryTime()))
            .collect(Collectors.toList());
    }

    /**
     * Gets current orders for a specific carrier.
     * 
     * @param allOrders List of all orders
     * @param carrierId The carrier ID
     * @return List of carrier's current orders
     */
    public List<Order> getCurrentOrders(List<Order> allOrders, int carrierId) {
        return allOrders.stream()
            .filter(o -> o.getCarrierId() != null && o.getCarrierId() == carrierId)
            .filter(o -> o.getStatus() == Order.OrderStatus.ASSIGNED || 
                        o.getStatus() == Order.OrderStatus.IN_DELIVERY)
            .sorted((o1, o2) -> o1.getRequestedDeliveryTime().compareTo(o2.getRequestedDeliveryTime()))
            .collect(Collectors.toList());
    }

    /**
     * Gets completed orders for a specific carrier.
     * 
     * @param allOrders List of all orders
     * @param carrierId The carrier ID
     * @return List of carrier's completed orders
     */
    public List<Order> getCompletedOrders(List<Order> allOrders, int carrierId) {
        return allOrders.stream()
            .filter(o -> o.getCarrierId() != null && o.getCarrierId() == carrierId)
            .filter(o -> o.getStatus() == Order.OrderStatus.DELIVERED)
            .sorted((o1, o2) -> o2.getActualDeliveryTime().compareTo(o1.getActualDeliveryTime()))
            .collect(Collectors.toList());
    }

    /**
     * Assigns an order to a carrier.
     * Validates that order is available and not already assigned.
     * 
     * @param order The order to assign
     * @param carrierId The carrier ID
     * @return true if assigned successfully, false otherwise
     */
    public boolean assignOrderToCarrier(Order order, int carrierId) {
        if (order.getStatus() != Order.OrderStatus.PENDING) {
            System.err.println("Order is not available for assignment! Status: " + order.getStatus());
            return false;
        }
        
        if (order.getCarrierId() != null) {
            System.err.println("Order already assigned to carrier " + order.getCarrierId());
            return false;
        }
        
        return order.assignCarrier(carrierId);
    }

    /**
     * Carrier picks up order and starts delivery.
     * 
     * @param order The order to start delivering
     * @param carrierId The carrier ID
     * @return true if started successfully, false otherwise
     */
    public boolean startDelivery(Order order, int carrierId) {
        if (order.getCarrierId() == null || order.getCarrierId() != carrierId) {
            System.err.println("This order is not assigned to carrier " + carrierId);
            return false;
        }
        
        return order.startDelivery();
    }

    /**
     * Carrier completes delivery.
     * 
     * @param order The order to complete
     * @param carrierId The carrier ID
     * @return true if completed successfully, false otherwise
     */
    public boolean completeDelivery(Order order, int carrierId) {
        if (order.getCarrierId() == null || order.getCarrierId() != carrierId) {
            System.err.println("This order is not assigned to carrier " + carrierId);
            return false;
        }
        
        return order.completeDelivery(LocalDateTime.now());
    }

    /**
     * Checks if carrier can select more orders.
     * Limits carrier to maximum number of concurrent deliveries.
     * 
     * @param allOrders List of all orders
     * @param carrierId The carrier ID
     * @param maxConcurrent Maximum concurrent deliveries allowed
     * @return true if carrier can select more, false otherwise
     */
    public boolean canSelectMoreOrders(List<Order> allOrders, int carrierId, int maxConcurrent) {
        long currentCount = allOrders.stream()
            .filter(o -> o.getCarrierId() != null && o.getCarrierId() == carrierId)
            .filter(o -> o.getStatus() == Order.OrderStatus.ASSIGNED || 
                        o.getStatus() == Order.OrderStatus.IN_DELIVERY)
            .count();
        
        return currentCount < maxConcurrent;
    }

    /**
     * Gets urgent orders (delivery time within next 2 hours).
     * 
     * @param allOrders List of all orders
     * @return List of urgent orders
     */
    public List<Order> getUrgentOrders(List<Order> allOrders) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime twoHoursLater = now.plusHours(2);
        
        return allOrders.stream()
            .filter(o -> o.getStatus() == Order.OrderStatus.PENDING ||
                        o.getStatus() == Order.OrderStatus.ASSIGNED ||
                        o.getStatus() == Order.OrderStatus.IN_DELIVERY)
            .filter(o -> o.getRequestedDeliveryTime().isBefore(twoHoursLater))
            .sorted((o1, o2) -> o1.getRequestedDeliveryTime().compareTo(o2.getRequestedDeliveryTime()))
            .collect(Collectors.toList());
    }

    /**
     * Calculates carrier performance statistics.
     * 
     * @param allOrders List of all orders
     * @param carrierId The carrier ID
     * @return Performance statistics as string
     */
    public String getCarrierStatistics(List<Order> allOrders, int carrierId) {
        List<Order> completedOrders = getCompletedOrders(allOrders, carrierId);
        
        if (completedOrders.isEmpty()) {
            return "No completed deliveries yet.";
        }
        
        int totalDeliveries = completedOrders.size();
        
        // Calculate on-time delivery percentage
        long onTimeDeliveries = completedOrders.stream()
            .filter(o -> o.getActualDeliveryTime() != null &&
                        !o.getActualDeliveryTime().isAfter(o.getRequestedDeliveryTime()))
            .count();
        
        double onTimePercentage = (onTimeDeliveries * 100.0) / totalDeliveries;
        
        // Calculate total revenue delivered
        double totalRevenue = completedOrders.stream()
            .mapToDouble(Order::getTotalCost)
            .sum();
        
        StringBuilder stats = new StringBuilder();
        stats.append("=== CARRIER STATISTICS ===\n\n");
        stats.append("Total Deliveries: ").append(totalDeliveries).append("\n");
        stats.append("On-Time Deliveries: ").append(onTimeDeliveries).append(" (")
             .append(String.format("%.1f%%", onTimePercentage)).append(")\n");
        stats.append(String.format("Total Revenue Delivered: %.2f TL\n", totalRevenue));
        stats.append(String.format("Average Order Value: %.2f TL\n", totalRevenue / totalDeliveries));
        
        return stats.toString();
    }

    /**
     * Generates delivery summary for carrier interface.
     * 
     * @param allOrders List of all orders
     * @param carrierId The carrier ID
     * @return Delivery summary as string
     */
    public String generateDeliverySummary(List<Order> allOrders, int carrierId) {
        StringBuilder summary = new StringBuilder();
        
        List<Order> available = getAvailableOrders(allOrders);
        List<Order> current = getCurrentOrders(allOrders, carrierId);
        List<Order> completed = getCompletedOrders(allOrders, carrierId);
        
        summary.append("=== DELIVERY SUMMARY ===\n\n");
        summary.append("Available Orders: ").append(available.size()).append("\n");
        summary.append("Current Orders: ").append(current.size()).append("\n");
        summary.append("Completed Orders: ").append(completed.size()).append("\n\n");
        
        // Urgent orders warning
        List<Order> urgent = getUrgentOrders(allOrders).stream()
            .filter(o -> o.getCarrierId() == null || o.getCarrierId() == carrierId)
            .collect(Collectors.toList());
        
        if (!urgent.isEmpty()) {
            summary.append("⚠️ URGENT: ").append(urgent.size())
                   .append(" order(s) need delivery within 2 hours!\n");
        }
        
        return summary.toString();
    }

    /**
     * Validates if order can be selected by carrier.
     * Checks for conflicts with already selected orders.
     * 
     * @param order The order to validate
     * @param currentOrders Carrier's current orders
     * @return true if can be selected, false otherwise
     */
    public boolean validateOrderSelection(Order order, List<Order> currentOrders) {
        // Check if delivery time conflicts with existing orders
        for (Order existing : currentOrders) {
            // Simple conflict check: if delivery times are within 30 minutes
            long minutesDiff = Math.abs(
                java.time.Duration.between(
                    order.getRequestedDeliveryTime(),
                    existing.getRequestedDeliveryTime()
                ).toMinutes()
            );
            
            if (minutesDiff < 30) {
                System.err.println("Delivery time conflicts with order #" + existing.getOrderId());
                return false;
            }
        }
        
        return true;
    }

    /**
     * Gets order details formatted for carrier display.
     * 
     * @param order The order
     * @return Formatted order details
     */
    public String getOrderDetailsForCarrier(Order order) {
        StringBuilder details = new StringBuilder();
        
        details.append("=== ORDER #").append(order.getOrderId()).append(" ===\n\n");
        details.append("Customer: ").append(order.getCustomerName()).append("\n");
        details.append("Phone: ").append(order.getCustomerPhone()).append("\n");
        details.append("Address: ").append(order.getCustomerAddress()).append("\n\n");
        
        details.append("Delivery Time: ")
               .append(order.getRequestedDeliveryTime()
                      .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))
               .append("\n\n");
        
        details.append("Items:\n");
        for (Order.OrderItem item : order.getItems()) {
            details.append("  - ").append(item.getProductName())
                   .append(": ").append(item.getAmount()).append(" kg\n");
        }
        
        details.append("\nTotal: ").append(String.format("%.2f TL", order.getTotalCost())).append("\n");
        details.append("Status: ").append(order.getStatus()).append("\n");
        
        return details.toString();
    }
}
