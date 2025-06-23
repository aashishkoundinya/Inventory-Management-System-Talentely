package com.sims.services;

import com.sims.models.Item;
import java.util.*;
import java.util.stream.Collectors;

public class AnalyticsService {
    
    public static Map<String, Object> getInventoryAnalytics(List<Item> items) {
        Map<String, Object> analytics = new HashMap<>();
        
        // Basic metrics
        analytics.put("totalItems", items.size());
        analytics.put("totalValue", calculateTotalValue(items));
        analytics.put("averagePrice", calculateAveragePrice(items));
        
        // Category analysis
        analytics.put("categoryDistribution", getCategoryDistribution(items));
        analytics.put("topValueCategories", getTopValueCategories(items));
        
        // Stock analysis
        analytics.put("stockDistribution", getStockDistribution(items));
        analytics.put("lowStockPercentage", getLowStockPercentage(items));
        
        return analytics;
    }
    
    private static double calculateTotalValue(List<Item> items) {
        return items.stream()
                   .mapToDouble(item -> item.getPrice() * item.getQuantity())
                   .sum();
    }
    
    private static double calculateAveragePrice(List<Item> items) {
        return items.stream()
                   .mapToDouble(Item::getPrice)
                   .average()
                   .orElse(0.0);
    }
    
    private static Map<String, Integer> getCategoryDistribution(List<Item> items) {
        return items.stream()
                   .collect(Collectors.groupingBy(
                       Item::getCategory,
                       Collectors.summingInt(item -> 1)
                   ));
    }
    
    private static List<Map.Entry<String, Double>> getTopValueCategories(List<Item> items) {
        Map<String, Double> categoryValues = items.stream()
            .collect(Collectors.groupingBy(
                Item::getCategory,
                Collectors.summingDouble(item -> item.getPrice() * item.getQuantity())
            ));
        
        return categoryValues.entrySet().stream()
                           .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                           .limit(5)
                           .collect(Collectors.toList());
    }
    
    private static Map<String, Integer> getStockDistribution(List<Item> items) {
        Map<String, Integer> distribution = new HashMap<>();
        distribution.put("outOfStock", 0);
        distribution.put("lowStock", 0);
        distribution.put("normalStock", 0);
        distribution.put("overStock", 0);
        
        for (Item item : items) {
            if (item.getQuantity() == 0) {
                distribution.put("outOfStock", distribution.get("outOfStock") + 1);
            } else if (item.getQuantity() <= item.getLowStockThreshold()) {
                distribution.put("lowStock", distribution.get("lowStock") + 1);
            } else if (item.getQuantity() > item.getLowStockThreshold() * 3) {
                distribution.put("overStock", distribution.get("overStock") + 1);
            } else {
                distribution.put("normalStock", distribution.get("normalStock") + 1);
            }
        }
        
        return distribution;
    }
    
    private static double getLowStockPercentage(List<Item> items) {
        if (items.isEmpty()) return 0.0;
        
        long lowStockCount = items.stream()
                                 .filter(item -> item.getQuantity() <= item.getLowStockThreshold())
                                 .count();
        
        return (double) lowStockCount / items.size() * 100;
    }
    
    // Generate purchase recommendations
    public static List<String> generatePurchaseRecommendations(List<Item> items) {
        List<String> recommendations = new ArrayList<>();
        
        for (Item item : items) {
            if (item.getQuantity() <= item.getLowStockThreshold()) {
                int suggestedQuantity = Math.max(
                    item.getLowStockThreshold() * 2, 
                    item.getLowStockThreshold() - item.getQuantity() + 10
                );
                
                recommendations.add(String.format(
                    "Reorder %s: Current=%d, Threshold=%d, Suggested=%d",
                    item.getName(), item.getQuantity(), 
                    item.getLowStockThreshold(), suggestedQuantity
                ));
            }
        }
        
        return recommendations;
    }
}
