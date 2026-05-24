package edu.asu.ecommerce.services;

import edu.asu.ecommerce.dataaccess.ExternalStoreInfo_DAO;
import edu.asu.ecommerce.dataaccess.models.ExternalStoreInfo;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;

public class ExternalStoreService {
    private final ExternalStoreInfo_DAO daoNorth;
    private final ExternalStoreInfo_DAO daoSouth;

    public ExternalStoreService(Connection conNorth, Connection conSouth) {
        this.daoNorth = new ExternalStoreInfo_DAO(conNorth);
        this.daoSouth = new ExternalStoreInfo_DAO(conSouth);
    }

    public ExternalStoreInfo authenticateStore(String apiKey) throws SQLException {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalArgumentException("API Key cannot be null or empty");
        }


        if (apiKey.startsWith("N-")) {
            return daoNorth.getStoreByApiKey(apiKey);
        } else if (apiKey.startsWith("S-")) {
            return daoSouth.getStoreByApiKey(apiKey);
        } else {
            ExternalStoreInfo store = daoNorth.getStoreByApiKey(apiKey);
            if (store != null) return store;
            return daoSouth.getStoreByApiKey(apiKey);
        }
    }

    public ExternalStoreInfo registerStore(String ownerId, String storeName) throws SQLException {
        if (ownerId == null || storeName == null || storeName.trim().isEmpty()) {
            throw new IllegalArgumentException("Owner ID and Store Name are required");
        }



        String generatedApiKey = ownerId.charAt(0)+ "-ext_" + UUID.randomUUID().toString().replace("-", "");

        ExternalStoreInfo newStore = new ExternalStoreInfo(ownerId, storeName, generatedApiKey);

        boolean isInserted;
        if (ownerId.charAt(0)=='N') {
            isInserted = daoNorth.insertExternalStore(newStore);
        } else {
            isInserted = daoSouth.insertExternalStore(newStore);
        }

        if (!isInserted) {
            throw new SQLException("Failed to register the external store into the database.");
        }

        return newStore;
    }
}