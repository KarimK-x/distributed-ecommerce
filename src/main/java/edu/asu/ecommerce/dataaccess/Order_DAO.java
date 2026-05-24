package edu.asu.ecommerce.dataaccess;

import edu.asu.ecommerce.dataaccess.models.Order; // Updated import
import java.sql.*;

public class Order_DAO {
    private final Connection con;

    public Order_DAO(Connection con) {
        this.con = con;
    }

    public int insertOrder(Order order) throws SQLException { // Updated parameter
        String sql = "INSERT INTO [orderInfo] (buyerID, sellerID, itemID, totalPrice, timeStamp, buyerType) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pst = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, order.getBuyerId());
            pst.setString(2, order.getSellerId());
            pst.setString(3, order.getItemId());
            pst.setDouble(4, order.getTotalPrice());
            pst.setDate(5, Date.valueOf(order.getTimeStamp()));
            pst.setString(6, order.getBuyerType());

            int affectedRows = pst.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating order failed, no rows affected.");
            }

            try (ResultSet generatedKeys = pst.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Creating order failed, no ID obtained.");
                }
            }
        }
    }
}