
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

import models.Product;

/**
 * StockManager class for GreenGrocer Application.
 * Manages product stock, threshold alerts, and automatic reordering.
 * 
 * @author GroupXX
 * @version 1.0
 */
public class StockManager {
    
    private InputValidation validator;
    
    public StockManager() {
        this.validator = new InputValidation();
    }

    /**
     * Stock alert level enumeration.
     */
    public enum AlertLevel {
        CRITICAL,   // Stock = 0
        VERY_LOW,   // Stock <= threshold
        LOW,        // Stock <= threshold * 1.5
        NORMAL      // Stock > threshold * 1.5
    }

    /**
     * Stock alert class for notifications.
     */
    public static class StockAlert {
        private Product product;
        private AlertLevel level;
        private String message;
        
        public StockAlert(Product product, AlertLevel level, String message) {
            this.product = product;
            this.level = level;
            this.message = message;
        }
        
        public Product getProduct() { return product; }
        public AlertLevel getLevel() { return level; }
        public String getMessage() { return message; }
        
        @Override
        public String toString() {
            String emoji = level == AlertLevel.CRITICAL ? "🔴" : 
                          level == AlertLevel.VERY_LOW ? "🟠" : 
                          level == AlertLevel.LOW ? "🟡" : "🟢";
            return String.format("%s [%s] %s - %s", 
                emoji, level, product.getName(), message);
        }
    }

    /**
     * Reduces stock after a purchase.
     * 
     * @param product The product
     * @param amount Amount to reduce
     * @return true if successful, false if insufficient stock
     */
    public boolean reduceStock(Product product, double amount) {
        if (!validator.validateProductAmount(amount)) {
            return false;
        }
        
        if (!validator.validateStockAvailability(amount, product.getStock())) {
            return false;
        }
        
        double newStock = product.getStock() - amount;
        product.setStock(newStock);
        
        // Check for alerts
        StockAlert alert = checkStockLevel(product);
        if (alert.getLevel() != AlertLevel.NORMAL) {
            System.out.println("⚠️ " + alert);
        }
        
        return true;
    }

    /**
     * Adds stock (restocking).
     * 
     * @param product The product
     * @param amount Amount to add
     * @return true if successful, false otherwise
     */
    public boolean addStock(Product product, double amount) {
        if (!validator.validateProductAmount(amount)) {
            return false;
        }
        
        double newStock = product.getStock() + amount;
        product.setStock(newStock);
        
        System.out.println("✓ Stock added: " + product.getName() + 
            " - New stock: " + newStock + " kg");
        return true;
    }

    /**
     * Updates product threshold value.
     * 
     * @param product The product
     * @param newThreshold New threshold value
     * @return true if successful, false otherwise
     */
    public boolean updateThreshold(Product product, double newThreshold) {
        if (!validator.validateThreshold(newThreshold)) {
            return false;
        }
        
        double oldThreshold = product.getThreshold();
        product.setThreshold(newThreshold);
        
        System.out.println("✓ Threshold updated: " + product.getName() + 
            " - " + oldThreshold + " → " + newThreshold + " kg");
        
        // Check if new threshold triggers alert
        StockAlert alert = checkStockLevel(product);
        if (alert.getLevel() != AlertLevel.NORMAL) {
            System.out.println("⚠️ " + alert);
        }
        
        return true;
    }

    /**
     * Checks stock level and returns appropriate alert.
     * 
     * @param product The product to check
     * @return StockAlert object
     */
    public StockAlert checkStockLevel(Product product) {
        double stock = product.getStock();
        double threshold = product.getThreshold();
        
        if (stock == 0) {
            return new StockAlert(product, AlertLevel.CRITICAL, 
                "OUT OF STOCK! Immediate restock required.");
        } else if (stock <= threshold) {
            return new StockAlert(product, AlertLevel.VERY_LOW,
                String.format("Stock critically low (%.2f kg). Threshold pricing active!", stock));
        } else if (stock <= threshold * 1.5) {
            return new StockAlert(product, AlertLevel.LOW,
                String.format("Stock running low (%.2f kg). Consider restocking.", stock));
        } else {
            return new StockAlert(product, AlertLevel.NORMAL,
                String.format("Stock level normal (%.2f kg)", stock));
        }
    }

    /**
     * Gets all products with stock alerts.
     * 
     * @param products List of all products
     * @return List of products with alerts (CRITICAL, VERY_LOW, LOW)
     */
    public List<StockAlert> getStockAlerts(List<Product> products) {
        List<StockAlert> alerts = new ArrayList<>();
        
        for (Product product : products) {
            StockAlert alert = checkStockLevel(product);
            if (alert.getLevel() != AlertLevel.NORMAL) {
                alerts.add(alert);
            }
        }
        
        // Sort by severity (CRITICAL first)
        alerts.sort((a1, a2) -> a1.getLevel().compareTo(a2.getLevel()));
        
        return alerts;
    }

    /**
     * Gets products that are out of stock.
     * 
     * @param products List of all products
     * @return List of out-of-stock products
     */
    public List<Product> getOutOfStockProducts(List<Product> products) {
        return products.stream()
            .filter(p -> p.getStock() == 0)
            .collect(Collectors.toList());
    }

    /**
     * Gets products with threshold pricing active.
     * 
     * @param products List of all products
     * @return List of products with threshold active
     */
    public List<Product> getThresholdActiveProducts(List<Product> products) {
        return products.stream()
            .filter(p -> p.getStock() > 0 && p.getStock() <= p.getThreshold())
            .collect(Collectors.toList());
    }

    /**
     * Gets products with low stock (below threshold * 1.5).
     * 
     * @param products List of all products
     * @return List of low stock products
     */
    public List<Product> getLowStockProducts(List<Product> products) {
        return products.stream()
            .filter(p -> p.getStock() > 0 && p.getStock() <= p.getThreshold() * 1.5)
            .sorted((p1, p2) -> Double.compare(p1.getStock(), p2.getStock()))
            .collect(Collectors.toList());
    }

    /**
     * Calculates suggested restock amount based on sales patterns.
     * Simple formula: max(threshold * 3, 50)
     * 
     * @param product The product
     * @return Suggested restock amount
     */
    public double calculateSuggestedRestock(Product product) {
        double suggested = Math.max(product.getThreshold() * 3, 50);
        return Math.round(suggested * 100.0) / 100.0; // Round to 2 decimals
    }

    /**
     * Generates a stock report for owner.
     * 
     * @param products List of all products
     * @return Stock report as string
     */
    public String generateStockReport(List<Product> products) {
        StringBuilder report = new StringBuilder();
        
        report.append("========================================\n");
        report.append("       STOCK STATUS REPORT              \n");
        report.append("========================================\n\n");
        
        // Summary
        int totalProducts = products.size();
        int outOfStock = (int) products.stream().filter(p -> p.getStock() == 0).count();
        int lowStock = (int) products.stream()
            .filter(p -> p.getStock() > 0 && p.getStock() <= p.getThreshold()).count();
        int thresholdActive = (int) products.stream().filter(p -> p.getStock() > 0 && p.getStock() <= p.getThreshold()).count();
        
        report.append("SUMMARY:\n");
        report.append("----------------------------------------\n");
        report.append("Total Products: ").append(totalProducts).append("\n");
        report.append("Out of Stock: ").append(outOfStock).append("\n");
        report.append("Low Stock: ").append(lowStock).append("\n");
        report.append("Threshold Pricing Active: ").append(thresholdActive).append("\n\n");
        
        // Alerts
        List<StockAlert> alerts = getStockAlerts(products);
        if (!alerts.isEmpty()) {
            report.append("ALERTS:\n");
            report.append("----------------------------------------\n");
            for (StockAlert alert : alerts) {
                report.append(alert).append("\n");
            }
            report.append("\n");
        }
        
        // Detailed product list
        report.append("DETAILED STOCK LEVELS:\n");
        report.append("----------------------------------------\n");
        report.append(String.format("%-20s %10s %10s %10s\n", 
            "Product", "Stock", "Threshold", "Status"));
        report.append("----------------------------------------\n");
        
        for (Product product : products) {
            String status = product.getStock() == 0 ? "OUT" :
                           product.getStock() <= product.getThreshold() ? "LOW" : "OK";
            
            report.append(String.format("%-20s %8.2f kg %8.2f kg %10s\n",
                truncate(product.getName(), 20),
                product.getStock(),
                product.getThreshold(),
                status));
        }
        
        report.append("========================================\n");
        
        return report.toString();
    }

    /**
     * Generates restock suggestions for owner.
     * 
     * @param products List of all products
     * @return Restock suggestions as string
     */
    public String generateRestockSuggestions(List<Product> products) {
        StringBuilder suggestions = new StringBuilder();
        
        suggestions.append("========================================\n");
        suggestions.append("     RESTOCK SUGGESTIONS                \n");
        suggestions.append("========================================\n\n");
        
        List<Product> needsRestock = products.stream()
            .filter(p -> p.getStock() <= p.getThreshold())
            .sorted((p1, p2) -> Double.compare(p1.getStock(), p2.getStock()))
            .collect(Collectors.toList());
        
        if (needsRestock.isEmpty()) {
            suggestions.append("✓ All products have sufficient stock!\n");
        } else {
            suggestions.append(String.format("%-20s %12s %15s\n", 
                "Product", "Current", "Suggested"));
            suggestions.append("----------------------------------------\n");
            
            for (Product product : needsRestock) {
                double suggested = calculateSuggestedRestock(product);
                suggestions.append(String.format("%-20s %10.2f kg %13.2f kg\n",
                    truncate(product.getName(), 20),
                    product.getStock(),
                    suggested));
            }
        }
        
        suggestions.append("========================================\n");
        
        return suggestions.toString();
    }

    /**
     * Helper method to truncate long strings.
     */
    private String truncate(String str, int maxLength) {
        if (str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength - 3) + "...";
    }
}