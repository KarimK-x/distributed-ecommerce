package edu.asu.ecommerce.dataaccess;

import edu.asu.ecommerce.dataaccess.models.Item;
import edu.asu.ecommerce.dataaccess.models.UserInventory;
import edu.asu.ecommerce.dataaccess.models.User_Info;

import java.sql.*;

public class UserInventory_DAO {
    private final Connection con;

    public UserInventory_DAO(Connection con) {
        this.con = con;
    }

    public boolean insertInventory(UserInventory inventory) throws SQLException {
        String sql = "INSERT INTO UserInventory (userID, itemID, state, dateCreated, region) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            System.out.println(inventory.getUserId());
            pst.setString(1, inventory.getUserId());
            pst.setString(2, inventory.getItemId());
            pst.setString(3, inventory.getState());
            pst.setTimestamp(4, Timestamp.valueOf(inventory.getDateCreated()));
            pst.setString(5, inventory.getRegion());
            return pst.executeUpdate() > 0;
        }
    }

    public boolean editSellingItemById(String id) throws SQLException {
        String sql = "SELECT * FROM UserInventory WHERE itemID = ? AND state = 'Available'";
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                if (!rs.next()) {
                    return false;
                }
                else{
                    String sql2 = "Update UserInventory Set state = 'Sold' where itemId = ? AND state = 'Available'";

                    try(PreparedStatement pst2 = con.prepareStatement(sql2)) {
                        pst2.setString(1,id);
                        return pst2.executeUpdate()>0;
                    }
                }
            }
        }
    }
}
