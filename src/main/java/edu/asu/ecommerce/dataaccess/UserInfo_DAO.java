package edu.asu.ecommerce.dataaccess;

import edu.asu.ecommerce.dataaccess.models.User;
import edu.asu.ecommerce.dataaccess.models.User_Info;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserInfo_DAO {
    private Connection conSecure;

    public UserInfo_DAO(Connection conSecure) {
        this.conSecure = conSecure;
    }


    public boolean insertUserInfo(User user) throws SQLException {
        String sql = "INSERT INTO UserInfo (userID, email, passwordHash, balance) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pst = conSecure.prepareStatement(sql)) {
            pst.setString(1, user.getId());
            pst.setString(2, user.getEmail());
            pst.setString(3, user.getPassword());
            pst.setDouble(4, user.getBalance());
            
            return pst.executeUpdate() > 0;
        }
    }

    public User_Info getUserByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM UserInfo WHERE email = ?";
        try (PreparedStatement pst = conSecure.prepareStatement(sql)) {
            pst.setString(1, email);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return new User_Info(
                            rs.getString("userID"),
                            rs.getString("email"),
                            rs.getString("passwordHash"),
                            rs.getDouble("balance")
                    );
                }
            }
        }
        return null;
    }

    public User_Info getUserById(String id) throws SQLException {
        String sql = "SELECT * FROM UserInfo WHERE userID = ?";
        try (PreparedStatement pst = conSecure.prepareStatement(sql)) {
            pst.setString(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return new User_Info(
                            rs.getString("userID"),
                            rs.getString("email"),
                            rs.getString("passwordHash"),
                            rs.getDouble("balance")
                    );
                }
            }
        }
        return null;
    }

    public boolean incrementBalance(String userId, double amount) throws SQLException {
        String sql = "UPDATE UserInfo SET balance = balance + ? WHERE userID = ?";
        try (PreparedStatement pst = conSecure.prepareStatement(sql)) {
            pst.setDouble(1, amount);
            pst.setString(2, userId);
            return pst.executeUpdate() > 0;
        }
    }
}


