/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package edu.asu.ecommerce.socket.handlers;

import com.google.gson.JsonObject;
import edu.asu.ecommerce.services.AuthenticationService;

import java.sql.SQLException;

/**
 *
 * @author Dell
 */
public class LoginHandler {
    private JsonObject response = new JsonObject();
    private AuthenticationService authService;
    private JsonObject request;

    public LoginHandler(AuthenticationService userService, JsonObject request){
        this.authService = userService;
        this.request = request;
    }

    public JsonObject handle(){
        String email = request.get("email").getAsString();
        String password = request.get("password").getAsString();

        try{
            boolean isValid = authService.authenticate(email, password);
            if (isValid) {
                response.addProperty("status", "OK");
                response.addProperty("message", "Login successful");
                String userId = authService.getUserByEmail(email).getId();
                response.addProperty("userId", userId);
            } else {
                response.addProperty("status", "ERR");
                response.addProperty("code", "401");
                response.addProperty("message", "Invalid email or password");
            }
        }
        catch(SQLException se){
            response.addProperty("status", "ERR");
            response.addProperty("code", "777");
            response.addProperty("message", "SQL ERROR");
        }
        catch (Exception e){
            response.addProperty("status", "ERR");
            response.addProperty("code", "505");
            response.addProperty("message", e.getMessage());
        }

        return this.response;
    }
}
