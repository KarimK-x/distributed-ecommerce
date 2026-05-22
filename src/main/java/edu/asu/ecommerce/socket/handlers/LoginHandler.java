/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package edu.asu.ecommerce.socket.handlers;

import com.google.gson.JsonObject;
import edu.asu.ecommerce.services.UserService;

import java.sql.SQLException;

/**
 *
 * @author Dell
 */
public class LoginHandler {
    private final JsonObject response = new JsonObject();
    private final UserService userService;
    private final JsonObject request;

    public LoginHandler(UserService userService, JsonObject request){
        this.userService = userService;
        this.request = request;
    }

    public JsonObject handle(){
        String email = request.get("email").getAsString();
        String password = request.get("password").getAsString();

        try{
            boolean isValid = userService.authenticate(email, password);
            if (isValid) {
                response.addProperty("status", "OK");
                response.addProperty("message", "Login successful");
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
