package com.sims.services;

import com.sims.models.Item;
import java.util.List;
import java.util.stream.Collectors;

public class AlertManager {
    
    public static void showDailyAlerts(List<Item> items) {
        List<Item> lowStockItems = getLowStockItems(items);
        List<Item> expiringItems = getExpiringItems(items);
        List<Item> expiredItems = getExpiredItems(items);
        
        if (!lowStockItems.isEmpty()) {
            System.out.println("⚠️ LOW STOCK ALERT: " + lowStockItems.size() + " items need restocking");
        }
        
        if (!expiringItems.isEmpty()) {
            System.out.println("⏰ EXPIRY ALERT: " + expiringItems.size() + " items expiring soon");
        }
        
        if (!expiredItems.isEmpty()) {
            System.out.println("❌ EXPIRED ALERT: " + expiredItems.size() + " items have expired");
        }
    }
    
    public static List<Item> getLowStockItems(List<Item> items) {
        return items.stream()
                   .filter(item -> item.getQuantity() <= item.getLowStockThreshold())
                   .collect(Collectors.toList());
    }
    
    public static List<Item> getExpiringItems(List<Item> items) {
        return items.stream()
                   .filter(Item::isExpiring)
                   .collect(Collectors.toList());
    }
    
    public static List<Item> getExpiredItems(List<Item> items) {
        return items.stream()
                   .filter(Item::isExpired)
                   .collect(Collectors.toList());
    }
    
    public static int getLowStockCount(List<Item> items) {
        return getLowStockItems(items).size();
    }
    
    public static int getExpiringItemsCount(List<Item> items) {
        return getExpiringItems(items).size();
    }
    
    public static String getReorderSuggestion(Item item) {
        if (item.getQuantity() <= item.getLowStockThreshold()) {
            int suggestedOrder = item.getLowStockThreshold() * 2;
            return "Suggested reorder quantity: " + suggestedOrder;
        }
        return "Stock level is adequate";
    }
}
