package edu.asu.ecommerce.dataaccess;

import edu.asu.ecommerce.dataaccess.models.Profile;
import edu.asu.ecommerce.dataaccess.models.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class Profile_DAO {
    private Connection con;

    public Profile_DAO(Connection con) {
        this.con = con;
    }

    public boolean insertProfile(User profile) throws SQLException {
        String sql = "INSERT INTO Profile (userID, userName, createdAt, region) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, profile.getId());
            pst.setString(2, profile.getUserName());
            pst.setTimestamp(3, Timestamp.valueOf(profile.getCreatedAt()));
            pst.setString(4, profile.getRegion());

            return pst.executeUpdate() > 0;
        }
    }

    public Profile getProfileById(String id) throws SQLException {
        String sql = "SELECT * FROM Profile WHERE userID = ?";
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return new Profile(
                            rs.getString("userID"),
                            rs.getTimestamp("createdAt").toLocalDateTime(),
                            rs.getString("userName"),
                            rs.getString("region")
                    );
                }
            }
        }
        return null;
    }
}