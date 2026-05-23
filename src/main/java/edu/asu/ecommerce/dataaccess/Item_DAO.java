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

    public Item getItemById(String itemId) throws SQLException {
        String sql = "SELECT itemID, itemName, description, unitPrice, quantity, categoryID, brandID FROM Item WHERE itemID = ?";
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, itemId);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return mapItem(rs);
                }
            }
        }
        return null;
    }

    public boolean updateItem(Item item) throws SQLException {
        String sql = "UPDATE Item SET itemName = ?, description = ?, unitPrice = ?, quantity = ?, categoryID = ?, brandID = ? WHERE itemID = ?";
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, item.getItemName());
            pst.setString(2, item.getDescription());
            pst.setDouble(3, item.getPrice());
            pst.setInt(4, item.getQuantity());
            pst.setInt(5, item.getCategoryId());
            pst.setInt(6, item.getBrandId());
            pst.setString(7, item.getId());
            return pst.executeUpdate() > 0;
        }
    }

    public List<Item> searchByNameAndBrand(String nameQuery, String brandQuery) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT DISTINCT i.itemID, i.itemName, i.description, i.unitPrice, i.quantity, i.categoryID, i.brandID "
                        + "FROM Item i INNER JOIN Brand b ON i.brandID = b.brandID WHERE 1=1");
        List<String> params = new ArrayList<>();

        if (nameQuery != null && !nameQuery.isBlank()) {
            sql.append(" AND i.itemName LIKE ?");
            params.add("%" + nameQuery.trim() + "%");
        }
        if (brandQuery != null && !brandQuery.isBlank()) {
            sql.append(" AND b.brandName LIKE ?");
            params.add("%" + brandQuery.trim() + "%");
        }
        if (params.isEmpty()) {
            return List.of();
        }

        List<Item> items = new ArrayList<>();
        try (PreparedStatement pst = con.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                pst.setString(i + 1, params.get(i));
            }
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    items.add(mapItem(rs));
                }
            }
        }
        return items;
    }

    private Item mapItem(ResultSet rs) throws SQLException {
        return new Item(
                rs.getString("itemID"),
                rs.getString("itemName"),
                rs.getString("description"),
                rs.getDouble("unitPrice"),
                rs.getInt("quantity"),
                rs.getInt("categoryID"),
                rs.getInt("brandID")
        );
    }
}
