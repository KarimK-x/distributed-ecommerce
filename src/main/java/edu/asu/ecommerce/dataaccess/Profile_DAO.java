package edu.asu.ecommerce.dataaccess;

import edu.asu.ecommerce.dataaccess.models.Profile;


import java.sql.*;

public class Profile_DAO {
    private Connection con;

    public Profile_DAO(Connection conn){
        con = conn;
    }


    public Profile getProfileById(String id) throws SQLException {
        String sql = "select * from Profile where userID = ?";
        String sql2 = "select * from UserInfo where userID = ?";
        PreparedStatement pst = con.prepareStatement(sql);
        PreparedStatement pst2 = con.prepareStatement(sql2);
        pst.setString(1,id);
        pst2.setString(1,id);
        ResultSet rs = pst.executeQuery();
        ResultSet rs2 = pst2.executeQuery();

        if(rs.next() && rs2.next()){
            return new Profile(
                rs.getString("userID"), rs.getString("userName"), rs.getString("region"),
                rs.getTimestamp("createdAt").toLocalDateTime(),
                rs2.getString("email"),rs2.getString("passwordHash"),rs2.getDouble("balance")
            );
        }

        return null;
    }

    public void insertProfile(Profile user) throws SQLException {
        String sql = "Insert into Profile values(?,  ?,  ?,  ?)";
        String sql2 = "Insert into UserInfo values( ?, ?,  ?, ?)";
        PreparedStatement pst = con.prepareStatement(sql);
        PreparedStatement pst2 = con.prepareStatement(sql2);

        pst2.setString(1,user.getId());
        pst2.setString(2,user.getEmail());
        pst2.setString(3,user.getPassword());
        pst2.setDouble(4,user.getBalance());

        pst.setString(1,user.getId());
        pst.setString(2,user.getUserName());
        pst.setTimestamp(3,Timestamp.valueOf(user.getCreatedAt()));
        pst.setString(4,user.getRegion());


        pst2.executeUpdate();
        pst.executeUpdate();
    }



}