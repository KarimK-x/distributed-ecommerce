package edu.asu.ecommerce.services;

import edu.asu.ecommerce.dataaccess.ExternalStoreInfo_DAO;
import edu.asu.ecommerce.dataaccess.models.ExternalStoreInfo;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;

public class ExternalStoreService {
    private final ExternalStoreInfo_DAO storeDaoNorth;
    private final ExternalStoreInfo_DAO storeDaoSouth;

    public ExternalStoreService(Connection conNorth, Connection conSouth) {
        this.storeDaoNorth = new ExternalStoreInfo_DAO(conNorth);
        this.storeDaoSouth = new ExternalStoreInfo_DAO(conSouth);
    }

    public ExternalStoreInfo createStore(String ownerId, String storeName, String apiEndpoint) throws Exception {
        String region = getRegionFromOwnerId(ownerId);
        ExternalStoreInfo store = new ExternalStoreInfo(
                UUID.randomUUID().toString(),
                ownerId,
                storeName,
                apiEndpoint,
                UUID.randomUUID().toString()
        );

        boolean inserted = getStoreDao(region).insertStore(store);
        if (!inserted) {
            throw new SQLException("store insert failed");
        }

        return store;
    }

    public ExternalStoreInfo getStoreByApiKey(String apiKey) throws SQLException {
        ExternalStoreInfo store = storeDaoNorth.getStoreByApiKey(apiKey);
        if (store != null) {
            return store;
        }
        return storeDaoSouth.getStoreByApiKey(apiKey);
    }

    private ExternalStoreInfo_DAO getStoreDao(String region) throws Exception {
        if (region == null || region.isEmpty()) {
            throw new Exception("region is required");
        }
        if (region.equalsIgnoreCase("south")) {
            return storeDaoSouth;
        }
        return storeDaoNorth;
    }

    private String getRegionFromOwnerId(String ownerId) throws Exception {
        if (ownerId == null || ownerId.isEmpty()) {
            throw new Exception("ownerId is required");
        }
        if (ownerId.startsWith("N-") || ownerId.startsWith("n-")) {
            return "North";
        }
        if (ownerId.startsWith("S-") || ownerId.startsWith("s-")) {
            return "South";
        }
        throw new Exception("invalid ownerId region");
    }
}
