package edu.asu.ecommerce.socket.handlers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import edu.asu.ecommerce.services.ItemService;
import edu.asu.ecommerce.services.UserService;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class ManageInventoryHandler {
    private final UserService userService;
    private final ItemService itemService;

    public ManageInventoryHandler(UserService userService, ItemService itemService) {
        this.userService = userService;
        this.itemService = itemService;
    }

    public JsonObject handle(JsonObject request) {
        JsonObject response = new JsonObject();
        String userId = request.get("userId").getAsString();

        try {
            List<Map<String, Object>> listings = userService.getInventoryListing(userId, itemService);
            response.addProperty("status", "OK");
            response.add("items", toJsonArray(listings));
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

    private JsonArray toJsonArray(List<Map<String, Object>> items) {
        JsonArray array = new JsonArray();
        for (Map<String, Object> item : items) {
            JsonObject obj = new JsonObject();
            obj.addProperty("itemId", (String) item.get("itemId"));
            obj.addProperty("itemName", (String) item.get("itemName"));
            obj.addProperty("description", (String) item.get("description"));
            obj.addProperty("price", (Double) item.get("price"));
            obj.addProperty("quantity", (Integer) item.get("quantity"));
            obj.addProperty("categoryId", (Integer) item.get("categoryId"));
            obj.addProperty("brandId", (Integer) item.get("brandId"));
            obj.addProperty("dateCreated", (String) item.get("dateCreated"));
            array.add(obj);
        }
        return array;
    }
}
