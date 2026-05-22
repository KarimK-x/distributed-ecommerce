
package edu.asu.ecommerce;

import com.google.gson.JsonObject;
import edu.asu.ecommerce.client.Client;
import java.io.IOException;
import java.net.Socket;

public class Main {
    public static void main(String[] args) throws Exception {
        
        Client c1 = new Client(new Socket("localhost", 1234));
        Client c2 = new Client(new Socket("localhost", 1234));

        // --- USER 1 THREAD ---
        Thread user1 = new Thread(() -> runRegistration(c1, "karim", "3333", "karim@gmail.com","North"));


        // --- USER 2 THREAD ---
        Thread user2 = new Thread(() -> runRegistration(c2, "bebo", "1234", "bebo@gmail.com","South"));

        // START BOTH THREADS AT THE EXACT SAME TIME
        
        user1.start();
        user2.start();
    }
    
    public static void runRegistration(Client c, String username, String password, String email, String region){
        try {
                
                JsonObject reqC1 = new JsonObject();
                reqC1.addProperty("action", "REGISTER");
                reqC1.addProperty("username", username);
                reqC1.addProperty("password", password);
                reqC1.addProperty("email", email);
                reqC1.addProperty("region", region);

                System.out.println("[User " + username +"]: Sending request...");
                c.sendRequest(reqC1);
                System.out.println("[User " + username +"]: Server replied: " + c.receiveResponse());
                
                // Clean exit
                JsonObject exitReq = new JsonObject();
                exitReq.addProperty("action", "EXIT");
                c.sendRequest(exitReq);
                
            } catch (IOException e) {
                System.out.println("User 1 Error: " + e);
            }
    }
    
}