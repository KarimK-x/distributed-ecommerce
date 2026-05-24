package edu.asu.ecommerce.services;
//Used for
// 1. Deposit Cash
// 2. Purchasing Item (along with itemService)
// 3. Viewing Account Info/ Managing Inventory 

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;

import edu.asu.ecommerce.dataaccess.LedgerEntry_DAO;
import edu.asu.ecommerce.dataaccess.UserInventory_DAO;
import edu.asu.ecommerce.dataaccess.UserInfo_DAO;
import edu.asu.ecommerce.dataaccess.models.LedgerEntry;
import edu.asu.ecommerce.dataaccess.models.UserInventory;
import edu.asu.ecommerce.dataaccess.models.User_Info;

public class UserService{
    private Connection conSecure;
    private Connection conNorth;
    private Connection conSouth;

    private UserInfo_DAO userInfoDao;
    private UserInventory_DAO inventoryDaoNorth;
    private UserInventory_DAO inventoryDaoSouth;
    private LedgerEntry_DAO ledgerEntryDao;
    
    
    public UserService(Connection con_secure, Connection con_north, Connection con_south) throws SQLException{
        this.conSecure = con_secure;
        this.conNorth = con_north;
        this.conSouth = con_south;

        this.userInfoDao = new UserInfo_DAO(conSecure);
        this.inventoryDaoNorth = new UserInventory_DAO(conNorth);
        this.inventoryDaoSouth = new UserInventory_DAO(conSouth);
        this.ledgerEntryDao = new LedgerEntry_DAO(conSecure);

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

        LedgerEntry entry = new LedgerEntry(
                info.getId(),
                amount,
                "DEPOSIT",
                null,
                LocalDateTime.now()
        );
        boolean inserted = ledgerEntryDao.insertEntry(entry);

        if(!inserted){
            throw new SQLException("Couldn't insert ledger entry");
        }
    }

    public void createInventoryEntry(String userId, String itemId, String state) throws Exception {
        String region = getRegionFromUserId(userId);
        UserInventory inventory = new UserInventory(
                userId,
                itemId,
                state,
                LocalDateTime.now(),
                region
        );

        boolean inserted = getInventoryDao(region).insertInventory(inventory);
        if (!inserted) {
            throw new SQLException("inventory insert failed");
        }
    }
}