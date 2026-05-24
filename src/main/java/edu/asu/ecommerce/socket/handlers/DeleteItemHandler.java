package edu.asu.ecommerce.socket.handlers;

import com.google.gson.JsonObject;
import edu.asu.ecommerce.dataaccess.UserInventory_DAO;
import edu.asu.ecommerce.dataaccess.models.Item;
import edu.asu.ecommerce.dataaccess.models.User;
import edu.asu.ecommerce.services.AuthenticationService;
import edu.asu.ecommerce.services.ItemService;

import java.sql.Connection;
import java.sql.SQLException;

public class DeleteItemHandler {
    private final AuthenticationService authService;
    private final ItemService itemService;
    private final Connection conNorth;
    private final Connection conSouth;

    public DeleteItemHandler(AuthenticationService authService, ItemService itemService,
                             Connection conNorth, Connection conSouth) {
        this.authService = authService;
        this.itemService = itemService;
        this.conNorth = conNorth;
        this.conSouth = conSouth;
    }

    public JsonObject handle(JsonObject request) {
        JsonObject response = new JsonObject();

        String itemId = getString(request, "itemId");
        String email = getString(request, "email");

        if (itemId == null || itemId.isBlank() || email == null || email.isBlank()) {
            response.addProperty("status", "ERR");
            response.addProperty("code", "400");
            response.addProperty("message", "itemId and email are required");
            return response;
        }

        try {
            User user = authService.getUserByEmail(email);
            if (user == null) {
                response.addProperty("status", "ERR");
                response.addProperty("code", "404");
                response.addProperty("message", "User not found");
                return response;
            }

            Item item = itemService.getItemById(itemId);
            if (item == null) {
                response.addProperty("status", "ERR");
                response.addProperty("code", "404");
                response.addProperty("message", "Item not found");
                return response;
            }

            if (!user.getId().equalsIgnoreCase(item.getSellerId())) {
                response.addProperty("status", "ERR");
                response.addProperty("code", "403");
                response.addProperty("message", "Not authorized to delete this item");
                return response;
            }

            UserInventory_DAO inventoryDao = isSouth(user.getId())
                    ? new UserInventory_DAO(conSouth)
                    : new UserInventory_DAO(conNorth);

            if (!inventoryDao.hasInventoryEntry(user.getId(), itemId, "Available")) {
                response.addProperty("status", "ERR");
                response.addProperty("code", "409");
                response.addProperty("message", "Item is not available for delete");
                return response;
            }

            if (!inventoryDao.deleteInventoryEntry(user.getId(), itemId, "Available")) {
                response.addProperty("status", "ERR");
                response.addProperty("code", "777");
                response.addProperty("message", "Inventory delete failed");
                return response;
            }

            itemService.deleteItem(itemId);
            if (itemService.getItemById(itemId) != null) {
                response.addProperty("status", "ERR");
                response.addProperty("code", "777");
                response.addProperty("message", "Item delete failed");
                return response;
            }

            response.addProperty("status", "OK");
            response.addProperty("message", "Item deleted");
            response.addProperty("itemId", itemId);
        } catch (SQLException se) {
            response.addProperty("status", "ERR");
            response.addProperty("code", "777");
            response.addProperty("message", "SQL ERROR");
        } catch (Exception e) {
            response.addProperty("status", "ERR");
            response.addProperty("code", "505");
            response.addProperty("message", e.getMessage());
        }

        return response;
    }

    private static boolean isSouth(String userId) {
        return userId != null && (userId.startsWith("S-") || userId.startsWith("s-"));
    }

    private static String getString(JsonObject obj, String key) {
        if (obj == null || !obj.has(key) || obj.get(key).isJsonNull()) {
            return null;
        }
        return obj.get(key).getAsString();
    }
}
