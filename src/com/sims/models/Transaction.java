package com.sims.models;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Transaction implements Serializable {
    private static final long serialVersionUID = 1L;
    final private String id;
    final private String userId;
    final private String action;
    final private String details;
    final private LocalDateTime timestamp;
    
    public Transaction(String userId, String action, String details) {
        this.id = "TXN" + System.currentTimeMillis();
        this.userId = userId;
        this.action = action;
        this.details = details;
        this.timestamp = LocalDateTime.now();
    }
    
    // Getters
    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getAction() { return action; }
    public String getDetails() { return details; }
    public LocalDateTime getTimestamp() { return timestamp; }
    
    @Override
    public String toString() {
        return String.format("[%s] %s - %s: %s", timestamp, userId, action, details);
    }
}
