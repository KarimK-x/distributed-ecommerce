package edu.asu.ecommerce.services;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import edu.asu.ecommerce.dataaccess.Item_DAO;
import edu.asu.ecommerce.dataaccess.LedgerEntry_DAO;
import edu.asu.ecommerce.dataaccess.UserInfo_DAO;
import edu.asu.ecommerce.dataaccess.UserInventory_DAO;
import edu.asu.ecommerce.dataaccess.models.Item;
import edu.asu.ecommerce.dataaccess.models.UserInventory;
import edu.asu.ecommerce.dataaccess.models.User_Info;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class ReportService {
    private final UserInfo_DAO userInfoDao;
    private final LedgerEntry_DAO ledgerEntryDao;
    private final UserInventory_DAO inventoryDaoNorth;
    private final UserInventory_DAO inventoryDaoSouth;
    private final Item_DAO itemDao;

    public ReportService(Connection conSecure, Connection conGlobal, Connection conNorth, Connection conSouth) {
        this.userInfoDao = new UserInfo_DAO(conSecure);
        this.ledgerEntryDao = new LedgerEntry_DAO(conSecure);
        this.inventoryDaoNorth = new UserInventory_DAO(conNorth);
        this.inventoryDaoSouth = new UserInventory_DAO(conSouth);
        this.itemDao = new Item_DAO(conGlobal);
    }

    public JsonObject generateReport(String email) throws Exception {
        if (email == null || email.isBlank()) {
            throw new Exception("email is required");
        }

        User_Info info = userInfoDao.getUserByEmail(email);
        if (info == null) {
            throw new Exception("user not found");
        }

        String userId = info.getId();
        UserInventory_DAO inventoryDao = getInventoryDaoByUserId(userId);

        double totalSpent = ledgerEntryDao.getTotalByType(userId, "Buy");
        double totalEarned = ledgerEntryDao.getTotalByType(userId, "Sell");

        List<UserInventory> boughtItems = inventoryDao.getInventoryByUserAndState(userId, "Bought");
        List<UserInventory> soldItems = inventoryDao.getInventoryByUserAndState(userId, "Sold");

        JsonArray purchaseHistory = buildHistory(boughtItems);
        JsonArray salesHistory = buildHistory(soldItems);

        int availableCount = inventoryDao.countByState(userId, "Available");
        int soldCount = inventoryDao.countByState(userId, "Sold");
        int boughtCount = inventoryDao.countByState(userId, "Bought");

        JsonObject transactionSummary = new JsonObject();
        transactionSummary.addProperty("totalSpent", totalSpent);
        transactionSummary.addProperty("totalEarned", totalEarned);

        JsonObject inventorySnapshot = new JsonObject();
        inventorySnapshot.addProperty("availableCount", availableCount);
        inventorySnapshot.addProperty("soldCount", soldCount);
        inventorySnapshot.addProperty("boughtCount", boughtCount);

        JsonObject response = new JsonObject();
        response.addProperty("status", "OK");
        response.add("transactionSummary", transactionSummary);
        response.add("purchaseHistory", purchaseHistory);
        response.add("salesHistory", salesHistory);
        response.add("inventorySnapshot", inventorySnapshot);
        return response;
    }

    private UserInventory_DAO getInventoryDaoByUserId(String userId) throws Exception {
        if (userId == null || userId.isEmpty()) {
            throw new Exception("userId is required");
        }
        if (userId.startsWith("S-") || userId.startsWith("s-")) {
            return inventoryDaoSouth;
        }
        if (userId.startsWith("N-") || userId.startsWith("n-")) {
            return inventoryDaoNorth;
        }
        throw new Exception("invalid userId region");
    }

    private JsonArray buildHistory(List<UserInventory> entries) throws SQLException {
        JsonArray history = new JsonArray();
        for (UserInventory entry : entries) {
            Item itemInfo = itemDao.getItemById(entry.getItemId());
            JsonObject obj = new JsonObject();
            obj.addProperty("itemId", entry.getItemId());
            if (itemInfo != null) {
                obj.addProperty("itemName", itemInfo.getItemName());
                obj.addProperty("price", itemInfo.getPrice());
            }
            obj.addProperty("date", formatDate(getEntryDate(entry)));
            history.add(obj);
        }
        return history;
    }

    private LocalDateTime getEntryDate(UserInventory entry) {
        return entry.getDateCreated();
    }

    private String formatDate(LocalDateTime date) {
        return date == null ? "" : date.toString();
    }
}
