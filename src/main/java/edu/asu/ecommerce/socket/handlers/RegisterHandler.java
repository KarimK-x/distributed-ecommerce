/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package edu.asu.ecommerce.socket.handlers;

import com.google.gson.JsonObject;
import edu.asu.ecommerce.services.AuthenticationService;

import java.sql.SQLException;


public class RegisterHandler {
    private AuthenticationService authService;    

    public RegisterHandler(AuthenticationService us){
        this.authService = us;
    }
    
    public JsonObject handle(JsonObject request){
        
        JsonObject response = new JsonObject();

        String username = request.get("username").getAsString();
        String password = request.get("password").getAsString();
        String email = request.get("email").getAsString();
        String region = request.get("region").getAsString();
        
        try{
            authService.createUser(username,email,password,region);
            response.addProperty("status","OK");
            response.addProperty("message", "Account Created");
        }
        catch(SQLException se){
            response.addProperty("status", "ERR");
            response.addProperty("code", "777");
            response.addProperty("message", "SQL ERROR");
        }
        catch (Exception e){
            response.addProperty("status","ERR");
            response.addProperty("code", "505");
            response.addProperty("message", e.getMessage());
        }

        
        return response;
    }
}