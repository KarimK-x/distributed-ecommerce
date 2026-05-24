package edu.asu.ecommerce.socket.handlers;

import com.google.gson.JsonObject;
import edu.asu.ecommerce.services.AuthenticationService;
import edu.asu.ecommerce.services.EmailService;

import java.sql.SQLException;

public class LoginHandler {
    private final AuthenticationService authService;
    private final EmailService emailService;
    private final JsonObject request;

    // Required: Inject the EmailService here
    public LoginHandler(AuthenticationService authService, EmailService emailService, JsonObject request){
        this.authService = authService;
        this.emailService = emailService;
        this.request = request;
    }

    public JsonObject handle(){
        JsonObject response = new JsonObject();
        String email = request.get("email").getAsString();
        String password = request.get("password").getAsString();

        try {
            // 1. Verify standard credentials
            boolean isValid = authService.authenticate(email, password);

            if (isValid) {
                // 2. 2FA LOGIC: Generate code, email it, and tell client to wait
                String otp = authService.generateAndSaveOTP(email);
                emailService.sendLoginOtp(email, otp);

                response.addProperty("status", "PENDING_OTP");
                response.addProperty("message", "Credentials verified. Please enter the OTP sent to your email.");
                // CRITICAL: Do NOT return the userId here! Let VerifyLoginHandler do it.
            } else {
                response.addProperty("status", "ERR");
                response.addProperty("code", "401");
                response.addProperty("message", "Invalid email or password");
            }
        }
        catch(SQLException se){
            response.addProperty("status", "ERR");
            response.addProperty("message", "SQL ERROR");
        }
        catch (Exception e){
            response.addProperty("status", "ERR");
            response.addProperty("code", "505");
            response.addProperty("message", e.getMessage());
        }

        return response;
    }
}