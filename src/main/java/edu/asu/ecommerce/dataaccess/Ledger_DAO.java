package edu.asu.ecommerce.dataaccess;

import edu.asu.ecommerce.dataaccess.models.LedgerEntry;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Ledger_DAO {
    private final Connection con;

    public Ledger_DAO(Connection con) {
        this.con = con;
    }

    public int insertLedgerEntry(LedgerEntry entry) throws SQLException {
        String sql = "INSERT INTO [LedgerEntry] (userID, amount, transactionType, orderID, timeStamp) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement pst = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, entry.getUserId());
            pst.setDouble(2, entry.getAmount());
            pst.setString(3, entry.getTransactionType());
            pst.setInt(4, entry.getOrderId());
            pst.setDate(5, Date.valueOf(entry.getTimeStamp()));

            pst.executeUpdate();

            try (ResultSet generatedKeys = pst.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Creating ledger entry failed, no ID obtained.");
                }
            }
        }
    }

    // public List<LedgerEntry> getLedgerEntriesByUserId(String userId) throws SQLException {
    //     String sql = "SELECT * FROM [LedgerEntry] WHERE userID = ? ORDER BY timeStamp DESC";
    //     List<LedgerEntry> entries = new ArrayList<>();

    //     try (PreparedStatement pst = con.prepareStatement(sql)) {
    //         pst.setString(1, userId);
    //         try (ResultSet rs = pst.executeQuery()) {
    //             while (rs.next()) {
    //                 entries.add(new LedgerEntry(
    //                         rs.getInt("entryID"),
    //                         rs.getString("userID"),
    //                         rs.getDouble("amount"),
    //                         rs.getString("transactionType"),
    //                         rs.getInt("orderID"),
    //                         rs.getDate("timeStamp").toLocalDate()
    //                 ));
    //             }
    //         }
    //     }
    //     return entries;
    // }
}