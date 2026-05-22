package edu.asu.ecommerce.dataaccess.models;

import java.time.LocalDateTime;
import java.util.UUID;

public class Profile {
    private final String id;
    private String userName;
    private final LocalDateTime createdAt;
    private String region;

    private String email;
    private String password;
    private double balance;

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getId() {
        return id;
    }

    public Profile(String name, String reg, String email, String password){
        id = UUID.randomUUID().toString();
        userName = name;
        createdAt = LocalDateTime.now();
        region = reg;

        this.email = email;
        this.password = password;
        this.balance = 0;
    }

    public String getUserName() {
        return userName;
    }

    public Profile(String id, String name, String reg, String email, String password, double balance){
        this.id = id;
        userName = name;
        createdAt = LocalDateTime.now();
        region = reg;

        this.email = email;
        this.password = password;
        this.balance = balance;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public void setUserName(String newName){
        userName = newName;
    }
    public void setUserRegion(String newRegion){
        region = newRegion;
    }
}
