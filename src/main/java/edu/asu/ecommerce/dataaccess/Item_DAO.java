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
        String sql = "INSERT INTO Item (itemID, itemName, description, price, quantity, categoryID, brandID, sellerID) VALUES (?, ?, ?, ?, ?, ?, ?,?)";
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, item.getId());
            pst.setString(2, item.getItemName());
            pst.setString(3, item.getDescription());
            pst.setDouble(4, item.getPrice());
            pst.setInt(5, item.getQuantity());
            pst.setInt(6, item.getCategoryId());
            pst.setInt(7, item.getBrandId());
            pst.setString(8,item.getSellerId());
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

    public Item getItemById(String itemId) throws SQLException {
        String sql = "SELECT * FROM Item WHERE itemID = ?";
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, itemId);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return new Item(
                            rs.getString("itemID"),
                            rs.getString("itemName"),
                            rs.getString("description"),
                            rs.getDouble("price"),
                            rs.getInt("quantity"),
                            rs.getInt("categoryID"),
                            rs.getInt("brandID"),
                            rs.getString("sellerID")
                    );
                }
            }
        }
        return null;
    }

    public List<Item> getItemsByIds(List<String> itemIds) throws SQLException {
        List<Item> results = new ArrayList<>();
        if (itemIds == null || itemIds.isEmpty()) {
            return results;
        }

        StringBuilder sql = new StringBuilder("SELECT itemID, itemName, description, price, quantity, categoryID, brandID FROM Item WHERE itemID IN (");
        for (int i = 0; i < itemIds.size(); i++) {
            sql.append("?");
            if (i < itemIds.size() - 1) {
                sql.append(",");
            }
        }
        sql.append(")");


        try (PreparedStatement pst = con.prepareStatement(sql.toString())) {
            for (int i = 0; i < itemIds.size(); i++) {
                pst.setString(i + 1, itemIds.get(i));
            }
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    results.add(new Item(
                            rs.getString("itemID"),
                            rs.getString("itemName"),
                            rs.getString("description"),
                            rs.getDouble("price"),
                            rs.getInt("quantity"),
                            rs.getInt("categoryID"),
                            rs.getInt("brandID"),
                            rs.getString("sellerID")
                    ));
                }
            }
        }

        return results;
    }
}