package com.sims.main;

import com.sims.models.*;
import com.sims.services.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;


public class InventoryManagementSystem {
    private static InventoryManager inventoryManager;
    private static UserManager userManager;
    private static User currentUser;
    final private static Scanner scanner = new Scanner(System.in);
    
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
        
        showEnhancedDashboard();
        
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
            case 1 -> handleLogin();
            case 2 -> handleRegistration();
            case 3 -> System.exit(0);
            default -> System.out.println("Invalid choice!");
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
    
    private static void showMainMenu() {
        System.out.println("\n=== Main Menu ===");
        System.out.println("1. Item Management");
        System.out.println("2. Category Management");
        System.out.println("3. Search & Barcode");
        System.out.println("4. Reports & Export");
        System.out.println("5. Supplier Management");
        System.out.println("6. Analytics Dashboard"); 
        System.out.println("7. System Notifications");
        System.out.println("8. User Management");
        System.out.println("9. System Settings");
        System.out.println("10. Logout");
        System.out.print("Choice: ");
        
        int choice = getIntInput();
        switch (choice) {
            case 1 -> showItemMenu();
            case 2 -> showCategoryMenu();
            case 3 -> showSearchMenu();
            case 4 -> showReportsMenu();
            case 5 -> showSupplierMenu();
            case 6 -> showAnalyticsDashboard();
            case 7 -> showNotifications();
            case 8 -> {
                if (currentUser.getRole().equals("Admin")) {
                    showUserMenu();
                } else {
                    System.out.println("Access denied! Admin only.");
                }
            }
            case 9 -> showSettingsMenu();
            case 10 -> logout();
            default -> System.out.println("Invalid choice!");
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
            case 1 -> addItem();
            case 2 -> viewAllItems();
            case 3 -> updateItem();
            case 4 -> deleteItem();
            case 5 -> {
            }

            default -> System.out.println("Invalid choice!");
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
        String description = scanner.nextLine();
        System.out.print("Low Stock Threshold: ");
        int threshold = getIntInput();
        
        System.out.print("Expiry Date (YYYY-MM-DD, or press Enter to skip): ");
        String expiryStr = scanner.nextLine().trim();
        
        Item item = new Item(name, category, quantity, price, description, threshold);
        
        if (!expiryStr.isEmpty()) {
            if (item.setExpiryDate(expiryStr)) {
                System.out.println("Expiry date successfully set to: " + item.getExpiryDate());
            } else {
                System.out.println("Item will be created without expiry date due to invalid format.");
            }
        } else {
            System.out.println("No expiry date provided - item will not expire.");
        }
        
        if (inventoryManager.addItem(item)) {
            System.out.println("\nItem added successfully!");
            System.out.println("ID: " + item.getId());
            System.out.println("Barcode: " + item.getBarcode());
            
            if (item.getExpiryDate() != null) {
                System.out.println("Expiry Date: " + item.getExpiryDate() + " (" + item.getExpiryStatus() + ")");
            } else {
                System.out.println("Expiry Date: Not set");
            }
            
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
            
            System.out.printf("%-5s %-20s %-15s %-10d Rs %-9.2f %-15s%n",
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
            case 1 -> viewCategories();
            case 2 -> viewItemsByCategory();
            case 3 -> showCategorySummary();
            case 4 -> {
            }
            default -> System.out.println("Invalid choice!");
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
            System.out.printf("%s - %s (Qty: %d, Price: Rs%.2f)%n",
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
            case 1 -> searchByName();
            case 2 -> searchByCategory();
            case 3 -> searchById();
            case 4 -> barcodeSearch();
            case 5 -> {
            }

            default -> System.out.println("Invalid choice!");
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
            System.out.println("Price: Rs" + item.getPrice());
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
            System.out.printf("%s - %s [%s] (Qty: %d, Price: Rs%.2f)%n",
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
            case 1 -> exportToCsv();
            case 2 -> createBackup();
            case 3 -> viewTransactionLog();
            case 4 -> showLowStockReport();
            case 5 -> showExpiryReport();
            case 6 -> {
            }

            default -> System.out.println("Invalid choice!");
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
            case 1 -> viewAllUsers();
            case 2 -> addUser();
            case 3 -> deleteUser();
            case 4 -> {
            }

            default -> System.out.println("Invalid choice!");
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
            case 1 -> changePassword();
            case 2 -> showSystemInfo();
            case 3 -> System.out.println("Auto backup is enabled daily at system startup.");
            case 4 -> {
            }

            default -> System.out.println("Invalid choice!");
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
        showEnhancedDashboard();
    }
    
    private static void logTransaction(String action, String details) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String username = currentUser != null ? currentUser.getUsername() : "SYSTEM";
        String logEntry = String.format("[%s] %s - %s: %s", timestamp, username, action, details);
        ExportUtils.logTransaction(logEntry);
    }
    
    private static int getIntInput() {
        try {
            String input = scanner.nextLine().trim();
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
    
    private static double getDoubleInput() {
        try {
            String input = scanner.nextLine().trim();
            return Double.parseDouble(input);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    final private static SupplierManager supplierManager = new SupplierManager();

    private static void showSupplierMenu() {
        System.out.println("\n=== Supplier Management ===");
        System.out.println("1. Add Supplier");
        System.out.println("2. View All Suppliers");
        System.out.println("3. Search Supplier");
        System.out.println("4. Back");
        System.out.print("Choice: ");
        
        int choice = getIntInput();
        switch (choice) {
            case 1 -> addSupplier();
            case 2 -> viewAllSuppliers();
            case 3 -> searchSupplier();
            case 4 -> {
            }

            default -> System.out.println("Invalid choice!");
        }
    }

    private static void addSupplier() {
        System.out.println("\n=== Add New Supplier ===");
        System.out.print("Supplier Name: ");
        String name = scanner.nextLine();
        System.out.print("Contact Person: ");
        String contact = scanner.nextLine();
        System.out.print("Email: ");
        String email = scanner.nextLine();
        System.out.print("Phone: ");
        String phone = scanner.nextLine();
        System.out.print("Address: ");
        String address = scanner.nextLine();
        
        Supplier supplier = new Supplier(name, contact, email, phone, address);
        if (supplierManager.addSupplier(supplier)) {
            System.out.println("Supplier added successfully! ID: " + supplier.getId());
            logTransaction("ADD_SUPPLIER", "Added supplier: " + name);
        } else {
            System.out.println("Failed to add supplier!");
        }
    }

    private static void viewAllSuppliers() {
        List<Supplier> suppliers = supplierManager.getAllSuppliers();
        if (suppliers.isEmpty()) {
            System.out.println("No suppliers found.");
            return;
        }
        
        System.out.println("\n=== Suppliers ===");
        System.out.printf("%-10s %-20s %-20s %-15s%n", "ID", "Name", "Contact", "Phone");
        System.out.println("-".repeat(70));
        
        for (Supplier supplier : suppliers) {
            System.out.printf("%-10s %-20s %-20s %-15s%n",
                supplier.getId(),
                supplier.getName().length() > 20 ? supplier.getName().substring(0, 17) + "..." : supplier.getName(),
                supplier.getContactPerson().length() > 20 ? supplier.getContactPerson().substring(0, 17) + "..." : supplier.getContactPerson(),
                supplier.getPhone());
        }
    }

    private static void searchSupplier() {
        System.out.print("Enter supplier name to search: ");
        String searchTerm = scanner.nextLine();
        
        List<Supplier> allSuppliers = supplierManager.getAllSuppliers();
        List<Supplier> results = allSuppliers.stream()
            .filter(s -> s.getName().toLowerCase().contains(searchTerm.toLowerCase()) ||
                        s.getContactPerson().toLowerCase().contains(searchTerm.toLowerCase()))
            .collect(Collectors.toList());
        
        if (results.isEmpty()) {
            System.out.println("No suppliers found matching: " + searchTerm);
        } else {
            System.out.println("\n=== Search Results ===");
            for (Supplier supplier : results) {
                System.out.printf("%s - %s (Contact: %s)%n",
                    supplier.getId(), supplier.getName(), supplier.getContactPerson());
            }
        }
    }

    private static void showAnalyticsDashboard() {
        System.out.println("\n=== Analytics Dashboard ===");
        
        List<Item> items = inventoryManager.getAllItems();
        Map<String, Object> analytics = AnalyticsService.getInventoryAnalytics(items);
        
        System.out.printf("Total Items: %d%n", analytics.get("totalItems"));
        System.out.printf("Total Value: Rs %.2f%n", analytics.get("totalValue"));
        System.out.printf("Average Price: Rs %.2f%n", analytics.get("averagePrice"));
        System.out.printf("Low Stock Percentage: %.1f%%%n", analytics.get("lowStockPercentage"));
        
        List<String> recommendations = AnalyticsService.generatePurchaseRecommendations(items);
        if (!recommendations.isEmpty()) {
            System.out.println("\n=== Purchase Recommendations ===");
            for (String rec : recommendations) {
                System.out.println("• " + rec);
            }
        }
        
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }

    private static void showNotifications() {
        System.out.println("\n=== System Notifications ===");
        
        NotificationService.generateDailyNotifications(inventoryManager.getAllItems());
        
        List<String> notifications = NotificationService.getRecentNotifications(10);
        if (notifications.isEmpty()) {
            System.out.println("No notifications to display.");
        } else {
            for (String notification : notifications) {
                System.out.println(notification);
            }
        }
        
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }

    private static void showEnhancedDashboard() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("=== SMART INVENTORY MANAGEMENT SYSTEM DASHBOARD ===");
        System.out.println("=".repeat(60));
        
        // Welcome message
        System.out.printf("Welcome back, %s (%s)%n", 
            currentUser.getUsername(), currentUser.getRole());
        System.out.println("Login Time: " + 
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy - HH:mm:ss")));
        
        // Get all items for calculations
        List<Item> allItems = inventoryManager.getAllItems();
        
        // Calculate summary statistics
        InventorySummary summary = calculateInventorySummary(allItems);
        
        System.out.println("\n" + "─".repeat(60));
        System.out.println("INVENTORY OVERVIEW");
        System.out.println("─".repeat(60));
        
        // Basic inventory stats
        System.out.printf("Total Items in System: %d%n", summary.totalItems);
        System.out.printf("Total Categories: %d%n", summary.totalCategories);
        System.out.printf("Total Inventory Value: Rs %.2f%n", summary.totalValue);
        System.out.printf("Average Item Value: Rs %.2f%n", summary.averageItemValue);
        
        System.out.println("\n" + "─".repeat(60));
        System.out.println("ALERTS & ATTENTION REQUIRED");
        System.out.println("─".repeat(60));
        
        // Critical alerts
        if (summary.expiredItems > 0) {
            System.out.printf("EXPIRED ITEMS: %d items have expired and need immediate attention!%n", 
                summary.expiredItems);
        }
        
        if (summary.expiringItems > 0) {
            System.out.printf("EXPIRING SOON: %d items will expire within 7 days%n", 
                summary.expiringItems);
        }
        
        if (summary.lowStockItems > 0) {
            System.out.printf("LOW STOCK: %d items need restocking%n", summary.lowStockItems);
        }
        
        if (summary.outOfStockItems > 0) {
            System.out.printf("OUT OF STOCK: %d items have zero quantity%n", summary.outOfStockItems);
        }
        
        // If no alerts
        if (summary.expiredItems == 0 && summary.expiringItems == 0 && 
            summary.lowStockItems == 0 && summary.outOfStockItems == 0) {
            System.out.println("All items are in good condition - no immediate alerts!");
        }
        
        // Top categories by value
        if (!summary.topCategories.isEmpty()) {
            System.out.println("\n" + "─".repeat(60));
            System.out.println("TOP CATEGORIES BY VALUE");
            System.out.println("─".repeat(60));
            
            int rank = 1;
            for (Map.Entry<String, Double> entry : summary.topCategories.entrySet()) {
                System.out.printf("%d. %s: Rs %.2f%n", rank++, entry.getKey(), entry.getValue());
                if (rank > 5) break; // Show top 5
            }
        }
        
        // Recent activity (if you want to show last few transactions)
        showRecentActivity();
        
        // Action recommendations
        System.out.println("\n" + "─".repeat(60));
        System.out.println("RECOMMENDED ACTIONS");
        System.out.println("─".repeat(60));
        
        if (summary.lowStockItems > 0) {
            System.out.println("Review low stock items and create purchase orders");
        }
        if (summary.expiredItems > 0) {
            System.out.println("Remove expired items from inventory");
        }
        if (summary.expiringItems > 0) {
            System.out.println("Plan promotions for items expiring soon");
        }
        if (summary.totalItems == 0) {
            System.out.println("Add items to start managing your inventory");
        }
        
        System.out.println("\n" + "=".repeat(60));
        System.out.println("Press Enter to continue to main menu...");
        scanner.nextLine();
    }

    // Helper class to store summary data
    private static class InventorySummary {
        int totalItems;
        int totalCategories;
        double totalValue;
        double averageItemValue;
        int lowStockItems;
        int outOfStockItems;
        int expiringItems;
        int expiredItems;
        Map<String, Double> topCategories;
        
        InventorySummary() {
            this.topCategories = new LinkedHashMap<>();
        }
    }

    private static InventorySummary calculateInventorySummary(List<Item> items) {
        InventorySummary summary = new InventorySummary();
        
        // Basic counts
        summary.totalItems = items.size();
        summary.totalCategories = inventoryManager.getCategories().size();
        
        // Value calculations
        summary.totalValue = items.stream()
            .mapToDouble(item -> item.getPrice() * item.getQuantity())
            .sum();
        
        summary.averageItemValue = summary.totalItems > 0 ? 
            summary.totalValue / summary.totalItems : 0;
        
        // Alert counts
        summary.lowStockItems = (int) items.stream()
            .filter(item -> item.getQuantity() <= item.getLowStockThreshold() && item.getQuantity() > 0)
            .count();
        
        summary.outOfStockItems = (int) items.stream()
            .filter(item -> item.getQuantity() == 0)
            .count();
        
        summary.expiringItems = (int) items.stream()
            .filter(Item::isExpiring)
            .count();
        
        summary.expiredItems = (int) items.stream()
            .filter(Item::isExpired)
            .count();
        
        // Top categories by total value
        Map<String, Double> categoryValues = items.stream()
            .collect(Collectors.groupingBy(
                Item::getCategory,
                Collectors.summingDouble(item -> item.getPrice() * item.getQuantity())
            ));
        
        summary.topCategories = categoryValues.entrySet().stream()
            .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (e1, e2) -> e1,
                LinkedHashMap::new
            ));
        
        return summary;
    }

    private static void showRecentActivity() {
        List<String> recentLogs = ExportUtils.getTransactionLogs();
        if (!recentLogs.isEmpty()) {
            System.out.println("\n" + "─".repeat(60));
            System.out.println("RECENT ACTIVITY (Last 5 transactions)");
            System.out.println(" ".repeat(60));
            
            int count = Math.min(5, recentLogs.size());
            for (int i = recentLogs.size() - count; i < recentLogs.size(); i++) {
                String log = recentLogs.get(i);
                // Format the log entry for better readability
                if (log.length() > 80) {
                    log = log.substring(0, 77) + "...";
                }
                System.out.println(" " + log);
            }
        }
    }
}
