package com.sims.services;

import com.sims.models.User;
import com.sims.utils.FileManager;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

public class UserManager {
    private List<User> users;
    private static final String USERS_FILE = "data/users.dat";
    
    public UserManager() {
        this.users = new ArrayList<>();
        loadUsers();
    }
    
    public boolean registerUser(String username, String password, String role) {
        if (getUserByUsername(username) != null) {
            return false; // User already exists
        }
        
        String hashedPassword = SecurityUtils.hashPassword(password);
        User newUser = new User(username, hashedPassword, role);
        users.add(newUser);
        saveUsers();
        return true;
    }
    
    public User authenticateUser(String username, String password) {
        User user = getUserByUsername(username);
        if (user != null && SecurityUtils.verifyPassword(password, user.getPasswordHash())) {
            user.setLastLogin(LocalDateTime.now());
            saveUsers();
            return user;
        }
        return null;
    }
    
    public User getUserByUsername(String username) {
        return users.stream()
                   .filter(user -> user.getUsername().equals(username))
                   .findFirst()
                   .orElse(null);
    }
    
    public List<User> getAllUsers() {
        return new ArrayList<>(users);
    }
    
    public boolean updateUser(User updatedUser) {
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getUsername().equals(updatedUser.getUsername())) {
                users.set(i, updatedUser);
                saveUsers();
                return true;
            }
        }
        return false;
    }
    
    public boolean deleteUser(String username) {
        boolean removed = users.removeIf(user -> user.getUsername().equals(username));
        if (removed) {
            saveUsers();
        }
        return removed;
    }
    
    private void loadUsers() {
        try {
            Object data = FileManager.loadData(USERS_FILE);
            if (data instanceof List<?>) {
                this.users = (List<User>) data;
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("No existing user data found. Starting fresh.");
            this.users = new ArrayList<>();
        }
    }
    
    private void saveUsers() {
        try {
            FileManager.saveData(users, USERS_FILE);
        } catch (IOException e) {
            System.err.println("Error saving user data: " + e.getMessage());
        }
    }
}
