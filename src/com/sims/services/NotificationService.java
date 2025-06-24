package com.sims.services;

import com.sims.models.Item;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class NotificationService {
    final private static List<String> notifications = new ArrayList<>();
    
    public static void addNotification(String type, String message) {
        String timestamp = LocalDateTime.now()
                                      .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        String notification = String.format("[%s] %s: %s", timestamp, type, message);
        notifications.add(notification);
        
        if (notifications.size() > 50) {
            notifications.remove(0);
        }
    }
    
    public static List<String> getRecentNotifications(int count) {
        int size = notifications.size();
        int start = Math.max(0, size - count);
        return new ArrayList<>(notifications.subList(start, size));
    }
    
    public static void generateDailyNotifications(List<Item> items) {
        String today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        notifications.removeIf(notif -> notif.contains(today));
        
        for (Item item : items) {
            if (item.getQuantity() == 0) {
                addNotification("CRITICAL", item.getName() + " is out of stock");
            } else if (item.getQuantity() <= item.getLowStockThreshold()) {
                addNotification("WARNING", item.getName() + " is running low (Qty: " + item.getQuantity() + ")");
            }
            
            if (item.isExpired()) {
                addNotification("CRITICAL", item.getName() + " has expired");
            } else if (item.isExpiring()) {
                addNotification("WARNING", item.getName() + " expires soon");
            }
        }
    }
    
    public static void showAllNotifications() {
        if (notifications.isEmpty()) {
            System.out.println("No notifications to display.");
            return;
        }
        
        System.out.println("\n=== System Notifications ===");
        for (String notification : notifications) {
            System.out.println(notification);
        }
    }
}
