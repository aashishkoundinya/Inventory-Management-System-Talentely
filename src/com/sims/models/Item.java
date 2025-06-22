package com.sims.models;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public class Item implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id;
    private String name;
    private String category;
    private int quantity;
    private double price;
    private String description;
    private int lowStockThreshold;
    private LocalDate expiryDate;
    private String barcode;
    private LocalDate dateAdded;
    
    public Item(String name, String category, int quantity, double price, String description, int lowStockThreshold) {
        this.id = "ITM" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.name = name;
        this.category = category;
        this.quantity = quantity;
        this.price = price;
        this.description = description;
        this.lowStockThreshold = lowStockThreshold;
        this.barcode = generateBarcode();
        this.dateAdded = LocalDate.now();
    }
    
    private String generateBarcode() {
        return "BC" + System.currentTimeMillis() + (int)(Math.random() * 1000);
    }
    
    public boolean isExpiring() {
        if (expiryDate == null) return false;
        return ChronoUnit.DAYS.between(LocalDate.now(), expiryDate) <= 7;
    }
    
    public boolean isExpired() {
        if (expiryDate == null) return false;
        return expiryDate.isBefore(LocalDate.now());
    }
    
    public void setExpiryDate(String dateStr) {
        try {
            this.expiryDate = LocalDate.parse(dateStr);
        } catch (Exception e) {
            System.out.println("Invalid date format. Use YYYY-MM-DD");
        }
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public int getLowStockThreshold() { return lowStockThreshold; }
    public void setLowStockThreshold(int lowStockThreshold) { this.lowStockThreshold = lowStockThreshold; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
    public String getBarcode() { return barcode; }
    public LocalDate getDateAdded() { return dateAdded; }
    
    @Override
    public String toString() {
        return String.format("Item{id='%s', name='%s', category='%s', quantity=%d, price=%.2f}", 
                           id, name, category, quantity, price);
    }
}
