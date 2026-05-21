package com.customermanager.model;

import com.google.gson.annotations.SerializedName;

public class Customer {

    private Integer id;
    private String name;
    private String email;
    private String phone;

    @SerializedName("createdAt")
    private String createdAt;

    public Customer() {}

    public Customer(String name, String email, String phone) {
        this.name  = name;
        this.email = email;
        this.phone = phone;
    }

    // ── Getters ──────────────────────────────────────────────────────────────
    public Integer getId()        { return id; }
    public String  getName()      { return name; }
    public String  getEmail()     { return email; }
    public String  getPhone()     { return phone; }
    public String  getCreatedAt() { return createdAt; }

    // ── Setters ──────────────────────────────────────────────────────────────
    public void setId(Integer id)           { this.id = id; }
    public void setName(String name)        { this.name = name; }
    public void setEmail(String email)      { this.email = email; }
    public void setPhone(String phone)      { this.phone = phone; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return name + " <" + email + ">";
    }
}
