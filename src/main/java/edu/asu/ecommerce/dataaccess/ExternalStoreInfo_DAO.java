package edu.asu.ecommerce.dataaccess;

import edu.asu.ecommerce.dataaccess.models.ExternalStoreInfo;
import edu.asu.ecommerce.dataaccess.models.User;

import java.sql.*;

public class ExternalStoreInfo_DAO {
    private final Connection con;

    public ExternalStoreInfo_DAO(Connection con) {
        this.con = con;
    }

    public ExternalStoreInfo getStoreByApiKey(String apiKey) throws SQLException {
        String sql = "SELECT storeID, ownerID, storeName, apiKey FROM ExternalStoreInfo WHERE apiKey = ?";
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, apiKey);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return new ExternalStoreInfo(
                            rs.getString("storeID"),
                            rs.getString("ownerID"),
                            rs.getString("storeName"),
                            rs.getString("apiKey")
                    );
                }
            }
        }
        return null;
    }
    public boolean insertExternalStore(ExternalStoreInfo store) throws SQLException {
        String sql = "INSERT INTO ExternalStoreInfo (storeID,ownerID, storeName, apiKey) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, store.getStoreId());
            pst.setString(2, store.getOwnerId());
            pst.setString(3, store.getStoreName());
            pst.setString(4, store.getApiKey());

            return pst.executeUpdate() > 0;
        }
    }
}