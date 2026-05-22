package edu.asu.ecommerce.dataaccess;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserInfo_DAO {
    private Connection conSecure;

    public UserInfo_DAO(Connection conSecure) {
        this.conSecure = conSecure;
    }

    // Inserts the sensitive login data
    public boolean insertUserInfo(String userId, String email, String passwordHash) throws SQLException {
        String sql = "INSERT INTO UserInfo (userID, email, passwordHash, balance) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pst = conSecure.prepareStatement(sql)) {
            pst.setString(1, userId);
            pst.setString(2, email);
            pst.setString(3, passwordHash);
            pst.setDouble(4, 0.0); // Default balance is 0
            
            return pst.executeUpdate() > 0;
        }
    }

    // Used for login validation and checking if an email exists
    public String getUserIdByEmail(String email) throws SQLException {
        String sql = "SELECT userID FROM UserInfo WHERE email = ?";
        try (PreparedStatement pst = conSecure.prepareStatement(sql)) {
            pst.setString(1, email);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("userID");
                }
            }
        }
        return null;
    }
}