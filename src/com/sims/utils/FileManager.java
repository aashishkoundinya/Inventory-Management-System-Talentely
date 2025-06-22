package com.sims.utils;

import java.io.*;

public class FileManager {
    
    public static void saveData(Object data, String filename) throws IOException {
        File file = new File(filename);
        file.getParentFile().mkdirs();
        
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(data);
        }
    }
    
    public static Object loadData(String filename) throws IOException, ClassNotFoundException {
        File file = new File(filename);
        if (!file.exists()) {
            throw new FileNotFoundException("Data file not found: " + filename);
        }
        
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return ois.readObject();
        }
    }
    
    public static boolean fileExists(String filename) {
        return new File(filename).exists();
    }
    
    public static void createDirectories(String path) {
        new File(path).mkdirs();
    }
}
