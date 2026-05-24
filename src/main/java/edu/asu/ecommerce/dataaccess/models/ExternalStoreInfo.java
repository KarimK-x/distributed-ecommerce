package edu.asu.ecommerce.dataaccess.models;

public class ExternalStoreInfo {
    private final String storeId;
    private final String ownerId;
    private final String storeName;
    private final String apiKey;

    public ExternalStoreInfo(String storeId, String ownerId, String storeName, String apiKey) {
        this.storeId = storeId;
        this.ownerId = ownerId;
        this.storeName = storeName;
        this.apiKey = apiKey;
    }

    public String getStoreId() { return storeId; }
    public String getOwnerId() { return ownerId; }
    public String getStoreName() { return storeName; }
    public String getApiKey() { return apiKey; }
}