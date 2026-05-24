package edu.asu.ecommerce.socket.handlers;

import com.google.gson.JsonObject;
import edu.asu.ecommerce.services.ItemService;
import edu.asu.ecommerce.services.UserService;

import java.sql.SQLException;

public class EditItemHandler {
    private final UserService userService;
    private final ItemService itemService;

    public EditItemHandler(UserService userService, ItemService itemService) {
        this.userService = userService;
        this.itemService = itemService;
    }

    public JsonObject handle(JsonObject request) {
        JsonObject response = new JsonObject();

        String email = request.get("email").getAsString();
        String itemId = request.get("itemId").getAsString();
        String itemName = request.get("itemName").getAsString();
        String description = request.get("description").getAsString();
        double price = request.get("price").getAsDouble();
        int quantity = request.get("quantity").getAsInt();
        int categoryId = request.get("categoryId").getAsInt();
        int brandId = request.get("brandId").getAsInt();

        try {
            userService.editAvailableItem(email, itemId, itemName, description, price, quantity,
                    categoryId, brandId, itemService);
            response.addProperty("status", "OK");
            response.addProperty("message", "Item updated");
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
}
