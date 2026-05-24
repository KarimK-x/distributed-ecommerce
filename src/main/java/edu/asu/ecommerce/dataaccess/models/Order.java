package edu.asu.ecommerce.dataaccess.models;

import java.time.LocalDate;

public class Order {
    private int orderId;
    private String buyerId;
    private String sellerId;
    private String itemId;
    private double totalPrice;
    private LocalDate timeStamp;
    private String buyerType;

    // Constructor containing all fields (useful for reading from the database)
    public Order(int orderId, String buyerId, String sellerId, String itemId, double totalPrice, LocalDate timeStamp, String buyerType) {
        this.orderId = orderId;
        this.buyerId = buyerId;
        this.sellerId = sellerId;
        this.itemId = itemId;
        this.totalPrice = totalPrice;
        this.timeStamp = timeStamp;
        this.buyerType = buyerType;
    }

    public Order(String buyerId, String sellerId, String itemId, double totalPrice, String buyerType) {
        this.buyerId = buyerId;
        this.sellerId = sellerId;
        this.itemId = itemId;
        this.totalPrice = totalPrice;
        this.timeStamp = LocalDate.now();
        this.buyerType = buyerType;
    }

    // Getters and Setters
    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public String getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(String buyerId) {
        this.buyerId = buyerId;
    }

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public LocalDate getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(LocalDate timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getBuyerType() {
        return buyerType;
    }

    public void setBuyerType(String buyerType) {
        this.buyerType = buyerType;
    }
}