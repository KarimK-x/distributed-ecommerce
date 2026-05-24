package edu.asu.ecommerce.socket.handlers;

import com.google.gson.JsonObject;
import edu.asu.ecommerce.services.AuthenticationService;
import edu.asu.ecommerce.dataaccess.models.User; // Adjust import if your User class is named User_Info

import java.sql.SQLException;

public class VerifyLoginHandler {
    private final AuthenticationService authService;

    public VerifyLoginHandler(AuthenticationService authService) {
        this.authService = authService;
    }

    public JsonObject handle(JsonObject request) {
        JsonObject response = new JsonObject();

        String email = getString(request, "email");
        String providedOtp = getString(request, "otpCode");

        if (email == null || email.isBlank() || providedOtp == null || providedOtp.isBlank()) {
            response.addProperty("status", "ERR");
            response.addProperty("code", "400");
            response.addProperty("message", "Missing required fields: email or otpCode");
            return response;
        }

        try {
            boolean isOtpValid = authService.verifyOTP(email, providedOtp);

            if (isOtpValid) {
                var user = authService.getUserByEmail(email);

                if (user != null) {
                    response.addProperty("status", "OK");
                    response.addProperty("message", "Login fully authenticated.");
                    response.addProperty("userId", user.getId());
                } else {
                    response.addProperty("status", "ERR");
                    response.addProperty("code", "404");
                    response.addProperty("message", "User profile not found.");
                }
            } else {
                response.addProperty("status", "ERR");
                response.addProperty("code", "401");
                response.addProperty("message", "Invalid or expired OTP.");
            }

        } catch (SQLException se) {
            response.addProperty("status", "ERR");
            response.addProperty("code", "777");
            response.addProperty("message", "SQL ERROR: " + se.getMessage());
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