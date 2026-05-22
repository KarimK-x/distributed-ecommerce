package edu.asu.ecommerce.dataaccess.models;

import java.time.LocalDateTime;
import java.util.UUID;

public class User_Info {
    private final String id;
    private String email;
    private String password;
    private double balance;





    public User_Info(String id, String email, String password, double balance){
        this.id = id;
        this.email = email;
        this.password = password;
        this.balance = balance;
    }

    public String getId() {
        return id;
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
}
