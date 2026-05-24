package edu.asu.ecommerce.dataaccess.models;

import java.time.LocalDate;

public class LedgerEntry {
    private int entryId;
    private String userId;
    private double amount;
    private String transactionType;
    private Integer orderId = null;
    private LocalDate timeStamp;

    // Constructor for creating NEW entries (without entryId)
    public LedgerEntry(String userId, double amount, String transactionType, Integer orderId, LocalDate timeStamp) {
        this.userId = userId;
        this.amount = amount;
        this.transactionType = transactionType;
        this.orderId = orderId;
        this.timeStamp = timeStamp;
    }

    // Constructor for reading EXISTING entries from DB
    public LedgerEntry(int entryId, String userId, double amount, String transactionType, Integer orderId, LocalDate timeStamp) {
        this.entryId = entryId;
        this.userId = userId;
        this.amount = amount;
        this.transactionType = transactionType;
        this.orderId = orderId;
        this.timeStamp = timeStamp;
    }

    public int getEntryId() { return entryId; }
    public void setEntryId(int entryId) { this.entryId = entryId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }
    public Integer getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }
    public LocalDate getTimeStamp() { return timeStamp; }
    public void setTimeStamp(LocalDate timeStamp) { this.timeStamp = timeStamp; }
}