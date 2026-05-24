package edu.asu.ecommerce.socket.handlers;

import com.google.gson.JsonObject;
import edu.asu.ecommerce.dataaccess.models.User;
import edu.asu.ecommerce.services.AuthenticationService;
import edu.asu.ecommerce.services.ItemService;
import edu.asu.ecommerce.services.UserService;

import java.sql.SQLException;

public class AddItemHandler {
    private final AuthenticationService authService;
    private final ItemService itemService;
    private final UserService userService;

    public AddItemHandler(AuthenticationService authService, ItemService itemService, UserService userService) {
        this.authService = authService;
        this.itemService = itemService;
        this.userService = userService;
    }

    public JsonObject handle(JsonObject request) {
        JsonObject response = new JsonObject();

        String itemName = getString(request, "itemName");
        String description = getString(request, "description");
        Double price = getDouble(request, "price");
        Integer quantity = getInteger(request, "quantity");
        Integer categoryId = getInteger(request, "categoryId");
        Integer brandId = getInteger(request, "brandId");
        String email = getString(request, "email");

        if (itemName == null || description == null || price == null || quantity == null
                || categoryId == null || brandId == null || email == null || email.isBlank()) {
            response.addProperty("status", "ERR");
            response.addProperty("code", "400");
            response.addProperty("message", "Missing required fields");
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

            String itemId = itemService.addItem(itemName, description, price, quantity,
                    categoryId, brandId, user.getId());

            try {
                userService.createInventoryEntry(user.getId(), itemId, "Available");
            } catch (Exception e) {
                itemService.deleteItem(itemId);
                throw e;
            }

            response.addProperty("status", "OK");
            response.addProperty("message", "Item created");
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

    private static String getString(JsonObject obj, String key) {
        if (obj == null || !obj.has(key) || obj.get(key).isJsonNull()) {
            return null;
        }
        return obj.get(key).getAsString();
    }

    private static Double getDouble(JsonObject obj, String key) {
        if (obj == null || !obj.has(key) || obj.get(key).isJsonNull()) {
            return null;
        }
        return obj.get(key).getAsDouble();
    }

    private static Integer getInteger(JsonObject obj, String key) {
        if (obj == null || !obj.has(key) || obj.get(key).isJsonNull()) {
            return null;
        }
        return obj.get(key).getAsInt();
    }
}
