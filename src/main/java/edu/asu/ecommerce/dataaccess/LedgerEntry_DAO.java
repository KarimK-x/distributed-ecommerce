package edu.asu.ecommerce.dataaccess;

import edu.asu.ecommerce.dataaccess.models.LedgerEntry;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;

public class LedgerEntry_DAO {
    private final Connection con;

    public LedgerEntry_DAO(Connection con) {
        this.con = con;
    }

    public boolean insertEntry(LedgerEntry entry) throws SQLException {
        return insertEntry(entry.getUserId(), entry.getAmount(), entry.getTransactionType(), entry.getOrderId(), entry.getTimeStamp());
    }

    public boolean insertEntry(String userId, double amount, String transactionType, Integer orderId, LocalDateTime timeStamp) throws SQLException {
        String sql = "INSERT INTO LedgerEntry (userID, amount, transactionType, orderID, timeStamp) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, userId);
            pst.setDouble(2, amount);
            pst.setString(3, transactionType);
            if (orderId == null) {
                pst.setNull(4, Types.INTEGER);
            } else {
                pst.setInt(4, orderId);
            }
            pst.setTimestamp(5, Timestamp.valueOf(timeStamp));
            return pst.executeUpdate() > 0;
        }
    }

    public double getTotalByType(String userId, String transactionType) throws SQLException {
        String sql = "SELECT COALESCE(SUM(amount), 0) AS total FROM LedgerEntry WHERE userID = ? AND transactionType = ?";
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, userId);
            pst.setString(2, transactionType);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("total");
                }
            }
        }
        return 0;
    }
}
