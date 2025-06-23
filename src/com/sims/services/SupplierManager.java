package com.sims.services;

import com.sims.models.Supplier;
import com.sims.utils.FileManager;
import java.util.*;

public class SupplierManager {
    private List<Supplier> suppliers;
    private static final String SUPPLIERS_FILE = "data/suppliers.dat";
    
    public SupplierManager() {
        this.suppliers = new ArrayList<>();
        loadSuppliers();
    }
    
    public boolean addSupplier(Supplier supplier) {
        if (supplier != null && !suppliers.contains(supplier)) {
            suppliers.add(supplier);
            saveSuppliers();
            return true;
        }
        return false;
    }
    
    public List<Supplier> getAllSuppliers() {
        return new ArrayList<>(suppliers);
    }
    
    public Supplier getSupplierById(String id) {
        return suppliers.stream()
                       .filter(s -> s.getId().equals(id))
                       .findFirst()
                       .orElse(null);
    }
    
    private void loadSuppliers() {
        try {
            Object data = FileManager.loadData(SUPPLIERS_FILE);
            if (data instanceof List<?>) {
                this.suppliers = (List<Supplier>) data;
            }
        } catch (Exception e) {
            this.suppliers = new ArrayList<>();
        }
    }
    
    private void saveSuppliers() {
        try {
            FileManager.saveData(suppliers, SUPPLIERS_FILE);
        } catch (Exception e) {
            System.err.println("Error saving supplier data: " + e.getMessage());
        }
    }
}
