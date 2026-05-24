package edu.asu.ecommerce.dataaccess;

import edu.asu.ecommerce.dataaccess.models.ExternalStoreInfo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ExternalStoreInfo_DAO {
    private final Connection con;

    public ExternalStoreInfo_DAO(Connection con) {
        this.con = con;
    }

    public boolean insertStore(ExternalStoreInfo store) throws SQLException {
        String sql = "INSERT INTO ExternalStoreInfo (storeID, ownerID, storeName, apiEndpoint, apiKey) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, store.getStoreId());
            pst.setString(2, store.getOwnerId());
            pst.setString(3, store.getStoreName());
            pst.setString(4, store.getApiEndpoint());
            pst.setString(5, store.getApiKey());
            return pst.executeUpdate() > 0;
        }
    }

    public ExternalStoreInfo getStoreByApiKey(String apiKey) throws SQLException {
        String sql = "SELECT storeID, ownerID, storeName, apiEndpoint, apiKey FROM ExternalStoreInfo WHERE apiKey = ?";
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, apiKey);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return new ExternalStoreInfo(
                            rs.getString("storeID"),
                            rs.getString("ownerID"),
                            rs.getString("storeName"),
                            rs.getString("apiEndpoint"),
                            rs.getString("apiKey")
                    );
                }
            }
        }
        return null;
    }
}
