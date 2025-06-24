package com.sims.services;

import com.sims.models.Item;
import java.io.*;
import java.util.*;

public class ExportUtils {
    private static final String TRANSACTIONS_FILE = "data/transactions.log";
    private static final String BACKUP_DIR = "data/backups/";
    private static final String EXPORTS_DIR = "exports/";
    
    public static boolean exportToCSV(List<Item> items, String filename) {
        try {
            File exportsDir = new File(EXPORTS_DIR);
            if (!exportsDir.exists()) {
                exportsDir.mkdirs();
            }

            File file = new File(EXPORTS_DIR + filename);
            
            try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                writer.println("ID,Name,Category,Quantity,Price,Description,LowStockThreshold,Barcode,ExpiryDate");
                
                for (Item item : items) {
                    writer.printf("%s,%s,%s,%d,%.2f,%s,%d,%s,%s%n",
                        escapeCSV(item.getId()),
                        escapeCSV(item.getName()),
                        escapeCSV(item.getCategory()),
                        item.getQuantity(),
                        item.getPrice(),
                        escapeCSV(item.getDescription()),
                        item.getLowStockThreshold(),
                        escapeCSV(item.getBarcode()),
                        item.getExpiryDate() != null ? item.getExpiryDate().toString() : ""
                    );
                }
            }
            
            System.out.println("File saved at: " + file.getAbsolutePath());
            return true;
            
        } catch (IOException e) {
            System.err.println("Error exporting to CSV: " + e.getMessage());
            return false;
        }
    }
    
    private static String escapeCSV(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
    
    public static boolean createBackup(String backupName) {
        try {
            File backupDir = new File(BACKUP_DIR);
            backupDir.mkdirs();
            
            // Copy data files to backup directory
            File sourceDir = new File("data");
            File targetDir = new File(BACKUP_DIR + backupName);
            targetDir.mkdirs();
            
            copyDirectory(sourceDir, targetDir);
            return true;
        } catch (IOException e) {
            System.err.println("Error creating backup: " + e.getMessage());
            return false;
        }
    }
    
    private static void copyDirectory(File source, File target) throws IOException {
        if (source.isDirectory()) {
            if (!target.exists()) {
                target.mkdirs();
            }
            
            String[] children = source.list();
            if (children != null) {
                for (String child : children) {
                    if (!child.equals("backups")) {
                        copyDirectory(new File(source, child), new File(target, child));
                    }
                }
            }
        } else {
            try (FileInputStream fis = new FileInputStream(source);
                 FileOutputStream fos = new FileOutputStream(target)) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = fis.read(buffer)) > 0) {
                    fos.write(buffer, 0, length);
                }
            }
        }
    }
    
    public static void logTransaction(String logEntry) {
        try {
            File logFile = new File(TRANSACTIONS_FILE);
            
            File parentDir = logFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            
            try (PrintWriter writer = new PrintWriter(new FileWriter(logFile, true))) {
                writer.println(logEntry);
            }
        } catch (IOException e) {
            System.err.println("Error logging transaction: " + e.getMessage());
        }
    }
    
    public static List<String> getTransactionLogs() {
        List<String> logs = new ArrayList<>();
        try {
            File logFile = new File(TRANSACTIONS_FILE);
            if (logFile.exists()) {
                try (BufferedReader reader = new BufferedReader(new FileReader(logFile))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        logs.add(line);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading transaction logs: " + e.getMessage());
        }
        return logs;
    }
}
