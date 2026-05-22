package edu.asu.ecommerce.services;

import edu.asu.ecommerce.dataaccess.models.Profile;
import edu.asu.ecommerce.dataaccess.Profile_DAO;

import java.sql.*;

public class UserService {
    private Connection con;
    private Profile_DAO profileDao;


    public UserService(Connection conn) throws SQLException {
        con = conn;
        profileDao = new Profile_DAO(con);
    }


    public void createUser(String userName, String email, String password, String region) throws SQLException {
        profileDao.insertProfile(new Profile(userName,region,email,password));
    }


    public Profile getUser(String id) throws SQLException {

        return profileDao.getProfileById(id);
    }

    public Profile getUserByEmail(String email) throws SQLException {

        String sql = "select id from UserInfo where email = ?";
        PreparedStatement pst = con.prepareStatement(sql);
        pst.setString(1,email);

        ResultSet rs = pst.executeQuery();
        if(rs.next()){
            String id = rs.getString(1);
            return profileDao.getProfileById(id);
        }
        return null;
    }



}
