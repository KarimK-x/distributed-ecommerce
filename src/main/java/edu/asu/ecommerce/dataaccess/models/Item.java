package edu.asu.ecommerce.dataaccess.models;

import java.util.UUID;

public class Item {
    private final String id = UUID.randomUUID().toString();
    private String itemName;
    private String description;
    private double unitPrice;
    private int quantity;
    private int categoryId;
    private int brandId;

    public Item(String itemName, String description, double unitPrice, int quantity, int categoryId, int brandId) {
        this.itemName = itemName;
        this.description = description;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.categoryId = categoryId;
        this.brandId = brandId;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getItemName() {
        return itemName;
    }

    public String getDescription() {
        return description;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public int getBrandId() {
        return brandId;
    }
}
