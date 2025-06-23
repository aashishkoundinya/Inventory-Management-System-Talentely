package com.sims.services;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.zip.*;

public class BackupService {
    private static final String BACKUP_DIR = "data/backups/";
    
    public static boolean createCompressedBackup() {
        try {
            String timestamp = LocalDateTime.now()
                                          .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String backupFileName = BACKUP_DIR + "backup_" + timestamp + ".zip";
            
            File backupDir = new File(BACKUP_DIR);
            backupDir.mkdirs();
            
            try (FileOutputStream fos = new FileOutputStream(backupFileName);
                 ZipOutputStream zos = new ZipOutputStream(fos)) {
                
                File dataDir = new File("data");
                addDirectoryToZip(dataDir, dataDir.getName(), zos);
            }
            
            System.out.println("Compressed backup created: " + backupFileName);
            return true;
            
        } catch (IOException e) {
            System.err.println("Backup failed: " + e.getMessage());
            return false;
        }
    }
    
    private static void addDirectoryToZip(File dir, String dirName, ZipOutputStream zos) throws IOException {
        File[] files = dir.listFiles();
        if (files == null) return;
        
        for (File file : files) {
            if (file.isDirectory()) {
                if (!file.getName().equals("backups")) { // Skip backup directory
                    addDirectoryToZip(file, dirName + "/" + file.getName(), zos);
                }
            } else {
                addFileToZip(file, dirName + "/" + file.getName(), zos);
            }
        }
    }
    
    private static void addFileToZip(File file, String fileName, ZipOutputStream zos) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            ZipEntry zipEntry = new ZipEntry(fileName);
            zos.putNextEntry(zipEntry);
            
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                zos.write(buffer, 0, length);
            }
            
            zos.closeEntry();
        }
    }
    
    public static List<String> getAvailableBackups() {
        File backupDir = new File(BACKUP_DIR);
        if (!backupDir.exists()) {
            return new ArrayList<>();
        }
        
        String[] backupFiles = backupDir.list((dir, name) -> name.endsWith(".zip"));
        return Arrays.asList(backupFiles != null ? backupFiles : new String[0]);
    }
    
    // Auto-cleanup old backups (keep last 10)
    public static void cleanupOldBackups() {
        List<String> backups = getAvailableBackups();
        if (backups.size() > 10) {
            Collections.sort(backups);
            for (int i = 0; i < backups.size() - 10; i++) {
                File oldBackup = new File(BACKUP_DIR + backups.get(i));
                if (oldBackup.delete()) {
                    System.out.println("Deleted old backup: " + backups.get(i));
                }
            }
        }
    }
}
