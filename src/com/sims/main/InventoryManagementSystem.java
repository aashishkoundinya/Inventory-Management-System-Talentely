package com.sims.main;

import com.sims.services.*;
import com.sims.models.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class InventoryManagementSystem {
    private static InventoryManager inventoryManager;
    private static UserManager userManager;
    private static User currentUser;
    private static Scanner scanner = new Scanner(System.in);
    
    public static void main(String[] args) {
        System.out.println("=== Smart Inventory Management System (SIMS) ===");
        
        // Initialize managers
        inventoryManager = new InventoryManager();
        userManager = new UserManager();
        
        // Create default admin user if no users exist
        if (userManager.getAllUsers().isEmpty()) {
            userManager.registerUser("admin", "admin123", "Admin");
            System.out.println("Default admin user created: admin/admin123");
        }
        
        // Authentication loop
        while (currentUser == null) {
            showLoginMenu();
        }
        
        // Main application loop
        showDashboard();
        while (true) {
            showMainMenu();
        }
    }
    
    private static void showLoginMenu() {
        System.out.println("\n=== Login / Register ===");
        System.out.println("1. Login");
        System.out.println("2. Register");
        System.out.println("3. Exit");
        System.out.print("Choice: ");
        
        int choice = getIntInput();
        switch (choice) {
            case 1:
                handleLogin();
                break;
            case 2:
                handleRegistration();
                break;
            case 3:
                System.exit(0);
                break;
            default:
                System.out.println("Invalid choice!");
        }
    }
    
    private static void handleLogin() {
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();
        
        currentUser = userManager.authenticateUser(username, password);
        if (currentUser != null) {
            System.out.println("Login successful! Welcome, " + currentUser.getUsername());
            logTransaction("LOGIN", "User logged in: " + username);
        } else {
            System.out.println("Invalid credentials!");
        }
    }
    
    private static void handleRegistration() {
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();
        System.out.println("Roles: Admin, Manager, Employee");
        System.out.print("Role: ");
        String role = scanner.nextLine();
        
        if (userManager.registerUser(username, password, role)) {
            System.out.println("Registration successful!");
        } else {
            System.out.println("Registration failed! Username might already exist.");
        }
    }
    
    private static void showDashboard() {
        System.out.println("\n=== Dashboard ===");
        System.out.println("Total Items: " + inventoryManager.getTotalItems());
        System.out.println("Low Stock Alerts: " + AlertManager.getLowStockCount(inventoryManager.getAllItems()));
        System.out.println("Expiring Soon: " + AlertManager.getExpiringItemsCount(inventoryManager.getAllItems()));
        AlertManager.showDailyAlerts(inventoryManager.getAllItems());
    }
    
    private static void showMainMenu() {
        System.out.println("\n=== Main Menu ===");
        System.out.println("1. Item Management");
        System.out.println("2. Category Management");
        System.out.println("3. Search & Barcode");
        System.out.println("4. Reports & Export");
        System.out.println("5. User Management");
        System.out.println("6. System Settings");
        System.out.println("7. Logout");
        System.out.print("Choice: ");
        
        int choice = getIntInput();
        switch (choice) {
            case 1:
                showItemMenu();
                break;
            case 2:
                showCategoryMenu();
                break;
            case 3:
                showSearchMenu();
                break;
            case 4:
                showReportsMenu();
                break;
            case 5:
                if (currentUser.getRole().equals("Admin")) {
                    showUserMenu();
                } else {
                    System.out.println("Access denied! Admin only.");
                }
                break;
            case 6:
                showSettingsMenu();
                break;
            case 7:
                logout();
                break;
            default:
                System.out.println("Invalid choice!");
        }
    }
    
    private static void showItemMenu() {
        System.out.println("\n=== Item Management ===");
        System.out.println("1. Add Item");
        System.out.println("2. View All Items");
        System.out.println("3. Update Item");
        System.out.println("4. Delete Item");
        System.out.println("5. Back");
        System.out.print("Choice: ");
        
        int choice = getIntInput();
        switch (choice) {
            case 1:
                addItem();
                break;
            case 2:
                viewAllItems();
                break;
            case 3:
                updateItem();
                break;
            case 4:
                deleteItem();
                break;
            case 5:
                return;
            default:
                System.out.println("Invalid choice!");
        }
    }
    
    private static void addItem() {
        System.out.println("\n=== Add New Item ===");
        System.out.print("Name: ");
        String name = scanner.nextLine();
        System.out.print("Category: ");
        String category = scanner.nextLine();
        System.out.print("Quantity: ");
        int quantity = getIntInput();
        System.out.print("Price: ");
        double price = getDoubleInput();
        System.out.print("Description: ");
        scanner.nextLine(); // consume newline
        String description = scanner.nextLine();
        System.out.print("Low Stock Threshold: ");
        int threshold = getIntInput();
        System.out.print("Expiry Date (YYYY-MM-DD, or press Enter to skip): ");
        scanner.nextLine(); // consume newline
        String expiryStr = scanner.nextLine();
        
        Item item = new Item(name, category, quantity, price, description, threshold);
        if (!expiryStr.isEmpty()) {
            item.setExpiryDate(expiryStr);
        }
        
        if (inventoryManager.addItem(item)) {
            System.out.println("Item added successfully! ID: " + item.getId());
            System.out.println("Generated Barcode: " + item.getBarcode());
            logTransaction("ADD_ITEM", "Added item: " + name + " (ID: " + item.getId() + ")");
        } else {
            System.out.println("Failed to add item!");
        }
    }
    
    private static void viewAllItems() {
        List<Item> items = inventoryManager.getAllItems();
        if (items.isEmpty()) {
            System.out.println("No items in inventory.");
            return;
        }
        
        System.out.println("\n=== Inventory ===");
        System.out.printf("%-5s %-20s %-15s %-10s %-10s %-15s%n", 
            "ID", "Name", "Category", "Quantity", "Price", "Status");
        System.out.println("-".repeat(80));
        
        for (Item item : items) {
            String status = "";
            if (item.getQuantity() <= item.getLowStockThreshold()) {
                status = "LOW STOCK";
            }
            if (item.isExpiring()) {
                status += " EXPIRING";
            }
            
            System.out.printf("%-5s %-20s %-15s %-10d $%-9.2f %-15s%n",
                item.getId(), 
                item.getName().length() > 20 ? item.getName().substring(0, 17) + "..." : item.getName(),
                item.getCategory(), 
                item.getQuantity(), 
                item.getPrice(),
                status);
        }
    }
    
    private static void updateItem() {
        System.out.print("Enter Item ID to update: ");
        String id = scanner.nextLine();
        
        Item item = inventoryManager.getItem(id);
        if (item == null) {
            System.out.println("Item not found!");
            return;
        }
        
        System.out.println("Current item: " + item.getName());
        System.out.println("Leave blank to keep current value");
        
        System.out.print("New quantity (" + item.getQuantity() + "): ");
        String qtyStr = scanner.nextLine();
        if (!qtyStr.isEmpty()) {
            item.setQuantity(Integer.parseInt(qtyStr));
        }
        
        System.out.print("New price (" + item.getPrice() + "): ");
        String priceStr = scanner.nextLine();
        if (!priceStr.isEmpty()) {
            item.setPrice(Double.parseDouble(priceStr));
        }
        
        System.out.print("New description (" + item.getDescription() + "): ");
        String desc = scanner.nextLine();
        if (!desc.isEmpty()) {
            item.setDescription(desc);
        }
        
        if (inventoryManager.updateItem(item)) {
            System.out.println("Item updated successfully!");
            logTransaction("UPDATE_ITEM", "Updated item: " + id);
        } else {
            System.out.println("Failed to update item!");
        }
    }
    
    private static void deleteItem() {
        System.out.print("Enter Item ID to delete: ");
        String id = scanner.nextLine();
        
        Item item = inventoryManager.getItem(id);
        if (item == null) {
            System.out.println("Item not found!");
            return;
        }
        
        System.out.println("Are you sure you want to delete: " + item.getName() + "? (y/N)");
        String confirm = scanner.nextLine();
        
        if (confirm.equalsIgnoreCase("y")) {
            if (inventoryManager.deleteItem(id)) {
                System.out.println("Item deleted successfully!");
                logTransaction("DELETE_ITEM", "Deleted item: " + id);
            } else {
                System.out.println("Failed to delete item!");
            }
        }
    }
    
    private static void showCategoryMenu() {
        System.out.println("\n=== Category Management ===");
        System.out.println("1. View Categories");
        System.out.println("2. View Items by Category");
        System.out.println("3. Category Summary");
        System.out.println("4. Back");
        System.out.print("Choice: ");
        
        int choice = getIntInput();
        switch (choice) {
            case 1:
                viewCategories();
                break;
            case 2:
                viewItemsByCategory();
                break;
            case 3:
                showCategorySummary();
                break;
            case 4:
                return;
            default:
                System.out.println("Invalid choice!");
        }
    }
    
    private static void viewCategories() {
        Set<String> categories = inventoryManager.getCategories();
        if (categories.isEmpty()) {
            System.out.println("No categories found.");
            return;
        }
        
        System.out.println("\n=== Categories ===");
        for (String category : categories) {
            System.out.println("- " + category);
        }
    }
    
    private static void viewItemsByCategory() {
        System.out.print("Enter category name: ");
        String category = scanner.nextLine();
        
        List<Item> items = inventoryManager.getItemsByCategory(category);
        if (items.isEmpty()) {
            System.out.println("No items found in category: " + category);
            return;
        }
        
        System.out.println("\n=== Items in " + category + " ===");
        for (Item item : items) {
            System.out.printf("%s - %s (Qty: %d, Price: $%.2f)%n",
                item.getId(), item.getName(), item.getQuantity(), item.getPrice());
        }
    }
    
    private static void showCategorySummary() {
        Map<String, Integer> summary = inventoryManager.getCategorySummary();
        if (summary.isEmpty()) {
            System.out.println("No items in inventory.");
            return;
        }
        
        System.out.println("\n=== Category Summary ===");
        System.out.printf("%-20s %-10s%n", "Category", "Items");
        System.out.println("-".repeat(30));
        
        for (Map.Entry<String, Integer> entry : summary.entrySet()) {
            System.out.printf("%-20s %-10d%n", entry.getKey(), entry.getValue());
        }
    }
    
    private static void showSearchMenu() {
        System.out.println("\n=== Search & Barcode ===");
        System.out.println("1. Search by Name");
        System.out.println("2. Search by Category");
        System.out.println("3. Search by ID");
        System.out.println("4. Barcode Lookup");
        System.out.println("5. Back");
        System.out.print("Choice: ");
        
        int choice = getIntInput();
        switch (choice) {
            case 1:
                searchByName();
                break;
            case 2:
                searchByCategory();
                break;
            case 3:
                searchById();
                break;
            case 4:
                barcodeSearch();
                break;
            case 5:
                return;
            default:
                System.out.println("Invalid choice!");
        }
    }
    
    private static void searchByName() {
        System.out.print("Enter search term: ");
        String term = scanner.nextLine();
        
        List<Item> results = inventoryManager.searchItems(term);
        displaySearchResults(results, "name containing '" + term + "'");
    }
    
    private static void searchByCategory() {
        System.out.print("Enter category: ");
        String category = scanner.nextLine();
        
        List<Item> results = inventoryManager.getItemsByCategory(category);
        displaySearchResults(results, "category '" + category + "'");
    }
    
    private static void searchById() {
        System.out.print("Enter Item ID: ");
        String id = scanner.nextLine();
        
        Item item = inventoryManager.getItem(id);
        if (item != null) {
            List<Item> results = Arrays.asList(item);
            displaySearchResults(results, "ID '" + id + "'");
        } else {
            System.out.println("No item found with ID: " + id);
        }
    }
    
    private static void barcodeSearch() {
        System.out.print("Scan/Enter barcode: ");
        String barcode = scanner.nextLine();
        
        Item item = inventoryManager.getItemByBarcode(barcode);
        if (item != null) {
            System.out.println("\n=== Barcode Match Found ===");
            System.out.println("ID: " + item.getId());
            System.out.println("Name: " + item.getName());
            System.out.println("Category: " + item.getCategory());
            System.out.println("Quantity: " + item.getQuantity());
            System.out.println("Price: $" + item.getPrice());
            System.out.println("Barcode: " + item.getBarcode());
        } else {
            System.out.println("No item found with barcode: " + barcode);
        }
    }
    
    private static void displaySearchResults(List<Item> results, String searchCriteria) {
        if (results.isEmpty()) {
            System.out.println("No items found for " + searchCriteria);
            return;
        }
        
        System.out.println("\n=== Search Results for " + searchCriteria + " ===");
        for (Item item : results) {
            System.out.printf("%s - %s [%s] (Qty: %d, Price: $%.2f)%n",
                item.getId(), item.getName(), item.getCategory(), 
                item.getQuantity(), item.getPrice());
        }
    }
    
    private static void showReportsMenu() {
        System.out.println("\n=== Reports & Export ===");
        System.out.println("1. Export to CSV");
        System.out.println("2. Create Backup");
        System.out.println("3. View Transaction Log");
        System.out.println("4. Low Stock Report");
        System.out.println("5. Expiry Report");
        System.out.println("6. Back");
        System.out.print("Choice: ");
        
        int choice = getIntInput();
        switch (choice) {
            case 1:
                exportToCsv();
                break;
            case 2:
                createBackup();
                break;
            case 3:
                viewTransactionLog();
                break;
            case 4:
                showLowStockReport();
                break;
            case 5:
                showExpiryReport();
                break;
            case 6:
                return;
            default:
                System.out.println("Invalid choice!");
        }
    }
    
    private static void exportToCsv() {
        String filename = "inventory_export_" + 
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv";
        
        if (ExportUtils.exportToCSV(inventoryManager.getAllItems(), filename)) {
            System.out.println("Inventory exported to: " + filename);
            logTransaction("EXPORT", "Exported inventory to CSV: " + filename);
        } else {
            System.out.println("Export failed!");
        }
    }
    
    private static void createBackup() {
        String backupName = "backup_" + 
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        
        if (ExportUtils.createBackup(backupName)) {
            System.out.println("Backup created: " + backupName);
            logTransaction("BACKUP", "Created backup: " + backupName);
        } else {
            System.out.println("Backup failed!");
        }
    }
    
    private static void viewTransactionLog() {
        List<String> logs = ExportUtils.getTransactionLogs();
        if (logs.isEmpty()) {
            System.out.println("No transaction logs found.");
            return;
        }
        
        System.out.println("\n=== Recent Transactions ===");
        int count = Math.min(20, logs.size()); // Show last 20 transactions
        for (int i = logs.size() - count; i < logs.size(); i++) {
            System.out.println(logs.get(i));
        }
    }
    
    private static void showLowStockReport() {
        List<Item> lowStockItems = AlertManager.getLowStockItems(inventoryManager.getAllItems());
        if (lowStockItems.isEmpty()) {
            System.out.println("No low stock items found.");
            return;
        }
        
        System.out.println("\n=== Low Stock Report ===");
        System.out.printf("%-20s %-10s %-10s%n", "Item", "Current", "Threshold");
        System.out.println("-".repeat(40));
        
        for (Item item : lowStockItems) {
            System.out.printf("%-20s %-10d %-10d%n",
                item.getName().length() > 20 ? item.getName().substring(0, 17) + "..." : item.getName(),
                item.getQuantity(),
                item.getLowStockThreshold());
        }
    }
    
    private static void showExpiryReport() {
        List<Item> expiringItems = AlertManager.getExpiringItems(inventoryManager.getAllItems());
        if (expiringItems.isEmpty()) {
            System.out.println("No items expiring soon.");
            return;
        }
        
        System.out.println("\n=== Expiry Report ===");
        for (Item item : expiringItems) {
            System.out.printf("%s - %s (Expires: %s)%n",
                item.getId(), item.getName(), 
                item.getExpiryDate() != null ? item.getExpiryDate().toString() : "No expiry");
        }
    }
    
    private static void showUserMenu() {
        System.out.println("\n=== User Management ===");
        System.out.println("1. View All Users");
        System.out.println("2. Add User");
        System.out.println("3. Delete User");
        System.out.println("4. Back");
        System.out.print("Choice: ");
        
        int choice = getIntInput();
        switch (choice) {
            case 1:
                viewAllUsers();
                break;
            case 2:
                addUser();
                break;
            case 3:
                deleteUser();
                break;
            case 4:
                return;
            default:
                System.out.println("Invalid choice!");
        }
    }
    
    private static void viewAllUsers() {
        List<User> users = userManager.getAllUsers();
        if (users.isEmpty()) {
            System.out.println("No users found.");
            return;
        }
        
        System.out.println("\n=== Users ===");
        System.out.printf("%-20s %-15s %-20s%n", "Username", "Role", "Last Login");
        System.out.println("-".repeat(55));
        
        for (User user : users) {
            System.out.printf("%-20s %-15s %-20s%n",
                user.getUsername(),
                user.getRole(),
                user.getLastLogin() != null ? user.getLastLogin().toString() : "Never");
        }
    }
    
    private static void addUser() {
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();
        System.out.println("Available roles: Admin, Manager, Employee");
        System.out.print("Role: ");
        String role = scanner.nextLine();
        
        if (userManager.registerUser(username, password, role)) {
            System.out.println("User added successfully!");
            logTransaction("ADD_USER", "Added user: " + username + " (Role: " + role + ")");
        } else {
            System.out.println("Failed to add user! Username might already exist.");
        }
    }
    
    private static void deleteUser() {
        System.out.print("Username to delete: ");
        String username = scanner.nextLine();
        
        if (username.equals(currentUser.getUsername())) {
            System.out.println("Cannot delete currently logged in user!");
            return;
        }
        
        System.out.println("Are you sure you want to delete user: " + username + "? (y/N)");
        String confirm = scanner.nextLine();
        
        if (confirm.equalsIgnoreCase("y")) {
            if (userManager.deleteUser(username)) {
                System.out.println("User deleted successfully!");
                logTransaction("DELETE_USER", "Deleted user: " + username);
            } else {
                System.out.println("Failed to delete user!");
            }
        }
    }
    
    private static void showSettingsMenu() {
        System.out.println("\n=== System Settings ===");
        System.out.println("1. Change Password");
        System.out.println("2. View System Info");
        System.out.println("3. Auto Backup Settings");
        System.out.println("4. Back");
        System.out.print("Choice: ");
        
        int choice = getIntInput();
        switch (choice) {
            case 1:
                changePassword();
                break;
            case 2:
                showSystemInfo();
                break;
            case 3:
                System.out.println("Auto backup is enabled daily at system startup.");
                break;
            case 4:
                return;
            default:
                System.out.println("Invalid choice!");
        }
    }
    
    private static void changePassword() {
        System.out.print("Current password: ");
        String currentPwd = scanner.nextLine();
        
        if (!SecurityUtils.verifyPassword(currentPwd, currentUser.getPasswordHash())) {
            System.out.println("Invalid current password!");
            return;
        }
        
        System.out.print("New password: ");
        String newPwd = scanner.nextLine();
        System.out.print("Confirm new password: ");
        String confirmPwd = scanner.nextLine();
        
        if (!newPwd.equals(confirmPwd)) {
            System.out.println("Passwords don't match!");
            return;
        }
        
        currentUser.setPasswordHash(SecurityUtils.hashPassword(newPwd));
        if (userManager.updateUser(currentUser)) {
            System.out.println("Password changed successfully!");
            logTransaction("CHANGE_PASSWORD", "Password changed for user: " + currentUser.getUsername());
        } else {
            System.out.println("Failed to change password!");
        }
    }
    
    private static void showSystemInfo() {
        System.out.println("\n=== System Information ===");
        System.out.println("SIMS Version: 1.0");
        System.out.println("Total Items: " + inventoryManager.getTotalItems());
        System.out.println("Total Users: " + userManager.getAllUsers().size());
        System.out.println("Current User: " + currentUser.getUsername() + " (" + currentUser.getRole() + ")");
        System.out.println("System Time: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }
    
    private static void logout() {
        logTransaction("LOGOUT", "User logged out: " + currentUser.getUsername());
        currentUser = null;
        System.out.println("Logged out successfully!");
        
        // Return to login menu
        while (currentUser == null) {
            showLoginMenu();
        }
        showDashboard();
    }
    
    private static void logTransaction(String action, String details) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String username = currentUser != null ? currentUser.getUsername() : "SYSTEM";
        String logEntry = String.format("[%s] %s - %s: %s", timestamp, username, action, details);
        ExportUtils.logTransaction(logEntry);
    }
    
    private static int getIntInput() {
        try {
            return Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
    
    private static double getDoubleInput() {
        try {
            return Double.parseDouble(scanner.nextLine());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}
