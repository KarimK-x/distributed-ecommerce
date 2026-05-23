package edu.asu.ecommerce.services;
//Used for
// 1. Deposit Cash
// 2. Purchasing Item (along with itemService)
// 3. Viewing Account Info/ Managing Inventory 

import java.sql.Connection;
import java.sql.SQLException;

import edu.asu.ecommerce.dataaccess.Profile_DAO;
import edu.asu.ecommerce.dataaccess.UserInfo_DAO;
import edu.asu.ecommerce.dataaccess.models.User_Info;

public class UserService{
    private Connection conSecure;
    private Connection conNorth;
    private Connection conSouth;

    private UserInfo_DAO userInfoDao;
    
    
    public UserService(Connection con_secure, Connection con_north, Connection con_south) throws SQLException{
        this.conSecure = con_secure;
        this.conNorth = con_north;
        this.conSouth = con_south;

        this.userInfoDao = new UserInfo_DAO(conSecure);

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