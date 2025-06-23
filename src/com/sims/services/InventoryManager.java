package com.sims.services;

import com.sims.models.Item;
import com.sims.utils.FileManager;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class InventoryManager {
    private List<Item> items;
    private static final String ITEMS_FILE = "data/items.dat";
    
    public InventoryManager() {
        this.items = new ArrayList<>();
        loadItems();
    }
    
    public boolean addItem(Item item) {
        if (item != null && !items.contains(item)) {
            items.add(item);
            saveItems();
            return true;
        }
        return false;
    }
    
    public Item getItem(String id) {
        return items.stream()
                   .filter(item -> item.getId().equals(id))
                   .findFirst()
                   .orElse(null);
    }
    
    public Item getItemByBarcode(String barcode) {
        return items.stream()
                   .filter(item -> item.getBarcode().equals(barcode))
                   .findFirst()
                   .orElse(null);
    }
    
    public List<Item> getAllItems() {
        return new ArrayList<>(items);
    }
    
    public List<Item> getItemsByCategory(String category) {
        return items.stream()
                   .filter(item -> item.getCategory().equalsIgnoreCase(category))
                   .collect(Collectors.toList());
    }
    
    public List<Item> searchItems(String searchTerm) {
        return items.stream()
                   .filter(item -> item.getName().toLowerCase().contains(searchTerm.toLowerCase()) ||
                                 item.getDescription().toLowerCase().contains(searchTerm.toLowerCase()))
                   .collect(Collectors.toList());
    }
    
    public boolean updateItem(Item updatedItem) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getId().equals(updatedItem.getId())) {
                items.set(i, updatedItem);
                saveItems();
                return true;
            }
        }
        return false;
    }
    
    public boolean deleteItem(String id) {
        boolean removed = items.removeIf(item -> item.getId().equals(id));
        if (removed) {
            saveItems();
        }
        return removed;
    }
    
    public Set<String> getCategories() {
        return items.stream()
                   .map(Item::getCategory)
                   .collect(Collectors.toSet());
    }
    
    public Map<String, Integer> getCategorySummary() {
        return items.stream()
                   .collect(Collectors.groupingBy(
                       Item::getCategory,
                       Collectors.summingInt(item -> 1)
                   ));
    }
    
    public int getTotalItems() {
        return items.size();
    }
    
    private void loadItems() {
        try {
            Object data = FileManager.loadData(ITEMS_FILE);
            if (data instanceof List<?>) {
                this.items = (List<Item>) data;
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("No existing inventory data found. Starting fresh.");
            this.items = new ArrayList<>();
        }
    }
    
    private void saveItems() {
        try {
            FileManager.saveData(items, ITEMS_FILE);
        } catch (IOException e) {
            System.err.println("Error saving inventory data: " + e.getMessage());
        }
    }
}
