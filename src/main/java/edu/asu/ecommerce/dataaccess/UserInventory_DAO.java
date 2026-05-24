package edu.asu.ecommerce.dataaccess;

import edu.asu.ecommerce.dataaccess.models.UserInventory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class UserInventory_DAO {
    private final Connection con;

    public UserInventory_DAO(Connection con) {
        this.con = con;
    }

    public boolean insertInventory(UserInventory inventory) throws SQLException {
        String sql = "INSERT INTO UserInventory (userID, itemID, state, dateCreated, dateSold, region) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, inventory.getUserId());
            pst.setString(2, inventory.getItemId());
            pst.setString(3, inventory.getState());
            pst.setTimestamp(4, Timestamp.valueOf(inventory.getDateCreated()));
            if (inventory.getDateSold() == null) {
                pst.setTimestamp(5, null);
            } else {
                pst.setTimestamp(5, Timestamp.valueOf(inventory.getDateSold()));
            }
            pst.setString(6, inventory.getRegion());
            return pst.executeUpdate() > 0;
        }
    }

    public List<UserInventory> getInventoryByUserAndState(String userId, String state) throws SQLException {
        String sql = "SELECT userID, itemID, state, dateCreated, dateSold, region FROM UserInventory WHERE userID = ? AND state = ? ORDER BY COALESCE(dateSold, dateCreated)";
        List<UserInventory> results = new ArrayList<>();
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, userId);
            pst.setString(2, state);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    Timestamp createdTs = rs.getTimestamp("dateCreated");
                    Timestamp soldTs = rs.getTimestamp("dateSold");
                    results.add(new UserInventory(
                            rs.getString("userID"),
                            rs.getString("itemID"),
                            rs.getString("state"),
                            createdTs == null ? null : createdTs.toLocalDateTime(),
                            soldTs == null ? null : soldTs.toLocalDateTime(),
                            rs.getString("region")
                    ));
                }
            }
        }
        return results;
    }

    public int countByState(String userId, String state) throws SQLException {
        String sql = "SELECT COUNT(*) AS total FROM UserInventory WHERE userID = ? AND state = ?";
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, userId);
            pst.setString(2, state);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total");
                }
            }
        }
        return 0;
    }
}
