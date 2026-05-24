package edu.asu.ecommerce.socket.handlers;

import com.google.gson.JsonObject;
import edu.asu.ecommerce.dataaccess.models.ExternalStoreInfo;
import edu.asu.ecommerce.dataaccess.models.User;
import edu.asu.ecommerce.services.AuthenticationService;
import edu.asu.ecommerce.services.ExternalStoreService;

import java.sql.SQLException;

public class ExternalStoreHandler {
    private final ExternalStoreService extStoreService;
    private final AuthenticationService authService;

    public ExternalStoreHandler(ExternalStoreService extStoreService, AuthenticationService authService) {
        this.extStoreService = extStoreService;
        this.authService = authService;
    }

    public JsonObject handle(JsonObject request) {
        JsonObject response = new JsonObject();

        String email = getString(request, "email");
        String storeName = getString(request, "storeName");

        if (email == null || email.isBlank() || storeName == null || storeName.isBlank()) {
            response.addProperty("status", "ERR");
            response.addProperty("code", "400");
            response.addProperty("message", "Missing required fields: email or storeName");
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

            ExternalStoreInfo newStore = extStoreService.registerStore(user.getId(), storeName);

            response.addProperty("status", "OK");
            response.addProperty("message", "External store registered successfully");
            response.addProperty("storeId", newStore.getStoreId());
            response.addProperty("apiKey", newStore.getApiKey());

        } catch (SQLException se) {
            response.addProperty("status", "ERR");
            response.addProperty("code", "777");
            response.addProperty("message", se.getMessage());
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
}