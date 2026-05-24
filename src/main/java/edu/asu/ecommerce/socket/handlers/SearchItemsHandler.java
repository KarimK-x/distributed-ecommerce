package edu.asu.ecommerce.socket.handlers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import edu.asu.ecommerce.dataaccess.models.Item;
import edu.asu.ecommerce.services.ItemService;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class SearchItemsHandler {
    private final ItemService itemService;
    private final Connection conNorth;
    private final Connection conSouth;

    public SearchItemsHandler(ItemService itemService, Connection conNorth, Connection conSouth) {
        this.itemService = itemService;
        this.conNorth = conNorth;
        this.conSouth = conSouth;
    }

    public JsonObject handle(JsonObject request) {
        JsonObject response = new JsonObject();

        String nameQuery = request.has("name") && !request.get("name").isJsonNull()
                ? request.get("name").getAsString() : null;
        String brandQuery = request.has("brand") && !request.get("brand").isJsonNull()
                ? request.get("brand").getAsString() : null;

        if ((nameQuery == null || nameQuery.isBlank()) && (brandQuery == null || brandQuery.isBlank())) {
            response.addProperty("status", "ERR");
            response.addProperty("code", "400");
            response.addProperty("message", "At least one of name or brand is required");
            return response;
        }

        try {
            List<Item> items = itemService.searchAvailableItems(nameQuery, brandQuery, conNorth, conSouth);
            response.addProperty("status", "OK");
            JsonArray itemsArray = new JsonArray();
            for (Item item : items) {
                JsonObject itemJson = new JsonObject();
                itemJson.addProperty("itemId", item.getId());
                itemJson.addProperty("itemName", item.getItemName());
                itemJson.addProperty("description", item.getDescription());
                itemJson.addProperty("price", item.getPrice());
                itemJson.addProperty("quantity", item.getQuantity());
                itemJson.addProperty("categoryId", item.getCategoryId());
                itemJson.addProperty("brandId", item.getBrandId());
                itemsArray.add(itemJson);
            }
            response.add("items", itemsArray);
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
}
