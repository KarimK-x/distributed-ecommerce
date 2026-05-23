package edu.asu.ecommerce.dataaccess.models;

import java.util.UUID;

public class Item {
    private final String id = UUID.randomUUID().toString();
    private String itemName;
    private String description;
    private double price;
    private int quantity;
    private int categoryId;
    private int brandId;

    public Item(String itemName, String description, double price, int quantity, int categoryId, int brandId) {
        this.itemName = itemName;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
        this.categoryId = categoryId;
        this.brandId = brandId;
    }

    public String getId() {
        return id;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setPrice(double price) {
        this.price = price;
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

    public double getPrice() {
        return price;
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
