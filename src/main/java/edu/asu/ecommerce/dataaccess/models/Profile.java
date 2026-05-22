package edu.asu.ecommerce.dataaccess.models;

import java.time.LocalDateTime;
import java.util.UUID;

public class Profile {
    private final String id;
    private String userName;
    private final LocalDateTime createdAt;
    private String region;

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getId() {
        return id;
    }


    public Profile(String id, LocalDateTime createdAt, String userName, String region) {
        this.id = id;
        this.createdAt = createdAt;
        this.userName = userName;
        this.region = region;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }
}
