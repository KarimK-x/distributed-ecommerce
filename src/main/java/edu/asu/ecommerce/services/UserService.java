package edu.asu.ecommerce.services;
//Used for
// 1. Deposit Cash
// 2. Purchasing Item (along with itemService)
// 3. Viewing Account Info/ Managing Inventory 

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;

import edu.asu.ecommerce.dataaccess.UserInventory_DAO;
import edu.asu.ecommerce.dataaccess.UserInfo_DAO;
import edu.asu.ecommerce.dataaccess.models.Item;
import edu.asu.ecommerce.dataaccess.models.UserInventory;
import edu.asu.ecommerce.dataaccess.models.User_Info;

public class UserService{
    private Connection conSecure;
    private Connection conNorth;
    private Connection conSouth;

    private UserInfo_DAO userInfoDao;
    private UserInventory_DAO inventoryDaoNorth;
    private UserInventory_DAO inventoryDaoSouth;
    
    
    public UserService(Connection con_secure, Connection con_north, Connection con_south) throws SQLException{
        this.conSecure = con_secure;
        this.conNorth = con_north;
        this.conSouth = con_south;

        this.userInfoDao = new UserInfo_DAO(conSecure);
        this.inventoryDaoNorth = new UserInventory_DAO(conNorth);
        this.inventoryDaoSouth = new UserInventory_DAO(conSouth);

    }

    private UserInventory_DAO getInventoryDao(String region) throws Exception {
        if (region == null || region.isEmpty()) {
            throw new Exception("region is required");
        }
        if (region.equalsIgnoreCase("south")) {
            return inventoryDaoSouth;
        }
        return inventoryDaoNorth;
    }

    private String getRegionFromUserId(String userId) throws Exception {
        if (userId == null || userId.isEmpty()) {
            throw new Exception("userId is required");
        }
        if (userId.startsWith("N-") || userId.startsWith("n-")) {
            return "North";
        }
        if (userId.startsWith("S-") || userId.startsWith("s-")) {
            return "South";
        }
        throw new Exception("invalid userId region");
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

    public void createInventoryEntry(String userId, String itemId, String state) throws Exception {
        String region = getRegionFromUserId(userId);
        UserInventory inventory = new UserInventory(
                userId,
                itemId,
                state,
                region
        );

        boolean inserted = getInventoryDao(region).insertInventory(inventory);
        if (!inserted) {
            throw new SQLException("inventory insert failed");
        }
    }



    public void purchase(String buyerId, Item item) throws Exception{
        User_Info buyer = userInfoDao.getUserById(buyerId);
        System.out.println(buyerId);
        String sellerId = item.getSellerId();
        UserInventory_DAO userInventoryDao;
        if(sellerId.charAt(0)=='N')
            userInventoryDao = inventoryDaoNorth;
        else
            userInventoryDao = inventoryDaoSouth;

        if(buyer.getBalance()>=item.getPrice()){

            if(!userInventoryDao.editSellingItemById(item.getId()))
                throw new Exception("Item is not available");

            userInfoDao.decrementBalance(buyerId,item.getPrice());
            userInfoDao.incrementBalance(sellerId,item.getPrice());


            if(buyerId.charAt(0)=='N') {
                System.out.println("north");
                inventoryDaoNorth.insertInventory(new UserInventory(buyerId, item.getId(), "Bought", "North"));
            }
            else {
                System.out.println("south");
                inventoryDaoSouth.insertInventory(new UserInventory(buyerId, item.getId(), "Bought", "South"));
            }
        }
        else{
            throw new Exception("Insufficient Balance!!!");
        }

    }
}