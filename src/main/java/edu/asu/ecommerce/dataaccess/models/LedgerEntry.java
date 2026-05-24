package edu.asu.ecommerce.dataaccess.models;

import java.time.LocalDateTime;

public class LedgerEntry {
    private int entryId;
    private String userId;
    private double amount;
    private String transactionType;
    private Integer orderId;
    private LocalDateTime timeStamp;

    public LedgerEntry(String userId, double amount, String transactionType, Integer orderId, LocalDateTime timeStamp) {
        this.userId = userId;
        this.amount = amount;
        this.transactionType = transactionType;
        this.orderId = orderId;
        this.timeStamp = timeStamp;
    }

    public int getEntryId() {
        return entryId;
    }

    public void setEntryId(int entryId) {
        this.entryId = entryId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    public LocalDateTime getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(LocalDateTime timeStamp) {
        this.timeStamp = timeStamp;
    }
}
