package com.sims.models;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

public class Supplier implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id;
    private String name;
    private String contactPerson;
    private String email;
    private String phone;
    private String address;
    private LocalDateTime createdDate;
    
    public Supplier(String name, String contactPerson, String email, String phone, String address) {
        this.id = "SUP" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.name = name;
        this.contactPerson = contactPerson;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.createdDate = LocalDateTime.now();
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getContactPerson() { return contactPerson; }
    public void setContactPerson(String contactPerson) { this.contactPerson = contactPerson; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public LocalDateTime getCreatedDate() { return createdDate; }
    
    @Override
    public String toString() {
        return String.format("Supplier{id='%s', name='%s', contact='%s'}", id, name, contactPerson);
    }
}
