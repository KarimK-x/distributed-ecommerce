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
}