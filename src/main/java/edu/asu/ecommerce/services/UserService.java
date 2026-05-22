package edu.asu.ecommerce.services;

import edu.asu.ecommerce.dataaccess.models.Profile;
import edu.asu.ecommerce.dataaccess.Profile_DAO;

import java.sql.*;

public class UserService {
    private Connection conSecure;
    private Connection conGlobal;
    private Connection conNorth;
    private Connection conSouth;
    private Profile_DAO profileDao;


    public UserService(Connection con_secure, Connection con_global, Connection con_north, Connection con_south) throws SQLException {
        this.conSecure = con_secure;
        this.conGlobal = con_global;
        this.conNorth = con_north;
        this.conSouth = con_south;
        profileDao = new Profile_DAO(conSecure);
    }


    public void createUser(String userName, String email, String password, String region) throws Exception {
        if(isExist(email)){
            throw new Exception("email is already registered");
        }
        else {
            profileDao.insertProfile(new Profile(userName, region, email, password));
        }
    }


    public Profile getUser(String id) throws SQLException {

        return profileDao.getProfileById(id);
    }

    public Profile getUserByEmail(String email) throws SQLException {

        String sql = "select userID from UserInfo where email = ?";
        PreparedStatement pst = conSecure.prepareStatement(sql);
        pst.setString(1,email);

        ResultSet rs = pst.executeQuery();
        if(rs.next()){
            String id = rs.getString(1);
            return profileDao.getProfileById(id);
        }
        return null;
    }
    public boolean isExist(String email) throws SQLException {
        return getUserByEmail(email) != null;
    }



}