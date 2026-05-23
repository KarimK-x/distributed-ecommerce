package edu.asu.ecommerce.dataaccess;

import edu.asu.ecommerce.dataaccess.models.UserInventory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

public class UserInventory_DAO {
    private final Connection con;

    public UserInventory_DAO(Connection con) {
        this.con = con;
    }

    public boolean insertInventory(UserInventory inventory) throws SQLException {
        String sql = "INSERT INTO UserInventory (userID, itemID, state, dateCreated, region) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, inventory.getUserId());
            pst.setString(2, inventory.getItemId());
            pst.setString(3, inventory.getState());
            pst.setTimestamp(4, Timestamp.valueOf(inventory.getDateCreated()));
            pst.setString(5, inventory.getRegion());
            return pst.executeUpdate() > 0;
        }
    }
}
