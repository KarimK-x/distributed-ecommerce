package edu.asu.ecommerce.dataaccess.models;

import java.time.LocalDateTime;

public class UserInventory {
    private final String userId;
    private final String itemId;
    private final String state;
    private final LocalDateTime dateCreated;
    private final LocalDateTime dateSold;
    private final String region;

    public UserInventory(String userId, String itemId, String state, LocalDateTime dateCreated, LocalDateTime dateSold, String region) {
        this.userId = userId;
        this.itemId = itemId;
        this.state = state;
        this.dateCreated = dateCreated;
        this.dateSold = dateSold;
        this.region = region;
    }

    public UserInventory(String userId, String itemId, String state, LocalDateTime dateCreated, String region) {
        this(userId, itemId, state, dateCreated, null, region);
    }

    public String getUserId() {
        return userId;
    }

    public String getItemId() {
        return itemId;
    }

    public String getState() {
        return state;
    }

    public LocalDateTime getDateCreated() {
        return dateCreated;
    }

    public LocalDateTime getDateSold() {
        return dateSold;
    }

    public String getRegion() {
        return region;
    }
}
