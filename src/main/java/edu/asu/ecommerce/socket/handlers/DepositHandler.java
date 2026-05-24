package edu.asu.ecommerce.socket.handlers;

import com.google.gson.JsonObject;
import edu.asu.ecommerce.dataaccess.models.User;
import edu.asu.ecommerce.services.AuthenticationService;
import edu.asu.ecommerce.services.UserService;

import java.sql.SQLException;

public class DepositHandler {
    private final UserService userService;
    private final AuthenticationService authService;

    public DepositHandler(UserService userService, AuthenticationService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    public JsonObject handle(JsonObject request) {
        JsonObject response = new JsonObject();

        String email = getString(request, "email");
        Double amount = getDouble(request, "amount");

        if (email == null || email.isBlank() || amount == null) {
            response.addProperty("status", "ERR");
            response.addProperty("code", "400");
            response.addProperty("message", "email and amount are required");
            return response;
        }
        if (amount <= 0) {
            response.addProperty("status", "ERR");
            response.addProperty("code", "400");
            response.addProperty("message", "amount must be greater than zero");
            return response;
        }

        try {
            userService.depositCash(email, amount);
            User user = authService.getUserByEmail(email);
            if (user == null) {
                response.addProperty("status", "ERR");
                response.addProperty("code", "404");
                response.addProperty("message", "User not found");
                return response;
            }

            response.addProperty("status", "OK");
            response.addProperty("message", "Deposit successful");
            response.addProperty("balance", user.getBalance());
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
}
