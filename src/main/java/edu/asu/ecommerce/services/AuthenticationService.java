package edu.asu.ecommerce.services;

import edu.asu.ecommerce.dataaccess.UserInfo_DAO;
import edu.asu.ecommerce.dataaccess.models.Profile;
import edu.asu.ecommerce.dataaccess.models.User;
import edu.asu.ecommerce.dataaccess.Profile_DAO;
import edu.asu.ecommerce.dataaccess.models.User_Info;

import java.sql.*;

public class AuthenticationService {
    private Connection conSecure;
    private Connection conNorth;
    private Connection conSouth;

    private Profile_DAO profileDaoNorth;
    private Profile_DAO profileDaoSouth;
    private UserInfo_DAO userInfoDao;

    public AuthenticationService(Connection con_secure, Connection con_north, Connection con_south) throws SQLException {
        this.conSecure = con_secure;
        this.conNorth = con_north;
        this.conSouth = con_south;

        this.userInfoDao = new UserInfo_DAO(conSecure);
        this.profileDaoNorth = new Profile_DAO(conNorth);
        this.profileDaoSouth = new Profile_DAO(conSouth);
    }

    private Profile_DAO getProfileDao(String region) {
        if (region != null && region.equalsIgnoreCase("south")) {
            return this.profileDaoSouth;
        }
        return this.profileDaoNorth;
    }

    public void createUser(String userName, String email, String password, String region) throws Exception {
        if(isExist(email)){
            throw new Exception("email is already registered");
        }
        else {
            User user = new User(userName, region, email, password);
            getProfileDao(region).insertProfile(user);
            userInfoDao.insertUserInfo(user);
        }
    }

    public User getUser(String id) throws SQLException {
        if (id == null || id.isEmpty()) {
            return null;
        }

        User_Info info = userInfoDao.getUserById(id);
        if (info == null) {
            return null;
        }

        Profile prof = null;
        if(id.charAt(0) == 'N') {
            prof = profileDaoNorth.getProfileById(id);
        }
        else if(id.charAt(0) == 'S') {
            prof = profileDaoSouth.getProfileById(id);
        }

        if (prof == null) {
            return null;
        }

        return new User(prof.getId(), prof.getUserName(), prof.getRegion(), prof.getCreatedAt(),
                info.getEmail(), info.getPassword(), info.getBalance());
    }

    public User getUserByEmail(String email) throws SQLException {
        User_Info info = userInfoDao.getUserByEmail(email);

        if (info != null) {
            return getUser(info.getId());
        }
        return null;
    }

    public boolean isExist(String email) throws SQLException {
        
        return getUserByEmail(email) != null;
    }

    public boolean authenticate(String email, String password) throws SQLException {
        if (email == null || email.isEmpty() || password == null) {
            return false;
        }

        User_Info info = userInfoDao.getUserByEmail(email);
        if (info == null) {
            return false;
        }

        return password.equals(info.getPassword());
    }

    public void depositCash(String email, double amount) throws Exception {
        if (email == null || email.isEmpty()) {
            throw new Exception("email is required");
        }
        if (amount <= 0) {
            throw new Exception("amount must be greater than zero");
        }

        User_Info info = userInfoDao.getUserByEmail(email);
        if (info == null) {
            throw new Exception("user not found");
        }

        boolean updated = userInfoDao.incrementBalance(info.getId(), amount);
        if (!updated) {
            throw new SQLException("balance update failed");
        }
    }
}