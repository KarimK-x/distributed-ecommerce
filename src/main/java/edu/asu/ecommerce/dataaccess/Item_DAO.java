package edu.asu.ecommerce.dataaccess;

import edu.asu.ecommerce.dataaccess.models.Item;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Item_DAO {
    private final Connection con;

    public Item_DAO(Connection con) {
        this.con = con;
    }

    public boolean insertItem(Item item) throws SQLException {
        String sql = "INSERT INTO Item (itemID, itemName, description, unitPrice, quantity, categoryID, brandID) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, item.getId());
            pst.setString(2, item.getItemName());
            pst.setString(3, item.getDescription());
            pst.setDouble(4, item.getPrice());
            pst.setInt(5, item.getQuantity());
            pst.setInt(6, item.getCategoryId());
            pst.setInt(7, item.getBrandId());
            return pst.executeUpdate() > 0;
        }
    }

    public boolean deleteItem(String itemId) throws SQLException {
        String sql = "DELETE FROM Item WHERE itemID = ?";
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, itemId);
            return pst.executeUpdate() > 0;
        }
    }
}
