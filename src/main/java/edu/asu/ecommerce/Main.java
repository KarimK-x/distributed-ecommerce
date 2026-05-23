
package edu.asu.ecommerce;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import edu.asu.ecommerce.client.Client;
import java.io.IOException;
import java.net.Socket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class Main {
    public static void main(String[] args) throws Exception {
        
        Client c1 = new Client(new Socket("localhost", 1234));
        Client c2 = new Client(new Socket("localhost", 1234));

        // --- USER 1 THREAD ---
        Thread user1 = new Thread(() -> runRegistration(c1, "karim", "3333", "karim@gmail.com","North"));


        // --- USER 2 THREAD ---
        Thread user2 = new Thread(() -> runRegistration(c2, "bebo", "1234", "bebo@gmail.com","South"));

        
        user1.start();
        user2.start();

        user1.join();
        user2.join();

        runLogin(c1, "karim@gmail.com", "3333", "karim");

        sendExit(c1, "karim");
        sendExit(c2, "bebo");
        runRestDepositTest();
        runRestAddItemTest("karim@gmail.com");
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
                
            } catch (IOException e) {
                System.out.println("User 1 Error: " + e);
            }
    }

    public static void runLogin(Client c, String email, String password, String username){
        try {
            JsonObject loginReq = new JsonObject();
            loginReq.addProperty("action", "LOGIN");
            loginReq.addProperty("email", email);
            loginReq.addProperty("password", password);

            System.out.println("[User " + username + "]: Sending login request...");
            c.sendRequest(loginReq);
            System.out.println("[User " + username + "]: Server replied: " + c.receiveResponse());

        } catch (IOException e) {
            System.out.println("Login Error: " + e);
        }
    }

    public static void sendExit(Client c, String username){
        try {
            JsonObject exitReq = new JsonObject();
            exitReq.addProperty("action", "EXIT");
            c.sendRequest(exitReq);
            System.out.println("[User " + username + "]: Connection closing.");
        } catch (IOException e) {
            System.out.println("Exit Error: " + e);
        }
    }

    public static void runRestDepositTest() throws Exception {
        HttpClient client = HttpClient.newHttpClient();

        String json = "{\"email\":\"bebo@gmail.com\",\"amount\":50}";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:7000/deposit"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("REST status: " + response.statusCode());
        System.out.println("REST body: " + response.body());
    }

    public static void runRestAddItemTest(String email) throws Exception {
        HttpClient client = HttpClient.newHttpClient();

        String json = "{\"itemName\":\"Laptop\",\"description\":\"Gaming laptop\",\"price\":1200,\"quantity\":2,\"categoryId\":1,\"brandId\":1,\"email\":\"" + email + "\"}";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:7000/items"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("REST /items status: " + response.statusCode());
        System.out.println("REST /items body: " + response.body());
    }
    
}