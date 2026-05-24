package edu.asu.ecommerce;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import edu.asu.ecommerce.client.Client;
import edu.asu.ecommerce.dataaccess.Brand_DAO;
import edu.asu.ecommerce.dataaccess.Category_DAO;
import java.io.IOException;
import java.net.Socket;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Main {
    private static final String TEST_EMAIL = "ziad@gmail.com";
    private static final String TEST_PASSWORD = "1111";
    private static final String TEST_USERNAME = "ziad";
    private static final String TEST_EMAIL_2 = "bebo@gmail.com";
    private static final String TEST_PASSWORD_2 = "1234";
    private static final String TEST_USERNAME_2 = "bebo";
    private static final String TEST_REGION_1 = "North";
    private static final String TEST_REGION_2 = "North";
    private static final String DB_BASE_URL = "jdbc:sqlserver://localhost:1433;encrypt=true;trustServerCertificate=true;";
    private static final String DB_USER = "sa";
    private static final String DB_PASS = "123456";

    private static int testCategoryId;
    private static int testBrandId;

    public static void main(String[] args) throws Exception {
        ensureTestCatalogData();

        Client sellerClient = new Client(new Socket("localhost", 1234));
        Client buyerClient = new Client(new Socket("localhost", 1234));

        Thread demo = new Thread(() -> runDemoFlow(
                sellerClient,
                buyerClient,
                TEST_USERNAME,
                TEST_PASSWORD,
                TEST_EMAIL,
                TEST_REGION_1,
                TEST_USERNAME_2,
                TEST_PASSWORD_2,
                TEST_EMAIL_2,
                TEST_REGION_2
        ));

        demo.start();
        demo.join();
    }

    private static void runDemoFlow(Client sellerClient, Client buyerClient,
                                    String sellerUsername, String sellerPassword, String sellerEmail, String sellerRegion,
                                    String buyerUsername, String buyerPassword, String buyerEmail, String buyerRegion) {
        try {
            System.out.println("=== [" + sellerUsername + "] Setup: register & login ===");
            runRegistration(sellerClient, sellerUsername, sellerPassword, sellerEmail, sellerRegion);
            String sellerId = runLogin(sellerClient, sellerEmail, sellerPassword, sellerUsername);
            if (sellerId == null) {
                System.out.println("[" + sellerUsername + "] Login failed; stopping tests.");
                sendExit(sellerClient, sellerUsername);
                return;
            }
            System.out.println("[" + sellerUsername + "] Logged in userId: " + sellerId);

            System.out.println("=== [" + buyerUsername + "] Setup: register & login ===");
            runRegistration(buyerClient, buyerUsername, buyerPassword, buyerEmail, buyerRegion);
            String buyerId = runLogin(buyerClient, buyerEmail, buyerPassword, buyerUsername);
            if (buyerId == null) {
                System.out.println("[" + buyerUsername + "] Login failed; stopping purchase step.");
            } else {
                System.out.println("[" + buyerUsername + "] Logged in userId: " + buyerId);
                System.out.println("\n=== [" + buyerUsername + "] REST: deposit ===");
                runRestDepositTest(buyerEmail, 10000);

                System.out.println("\n=== [" + buyerUsername + "] Socket: deposit ===");
                runSocketDeposit(buyerClient, buyerUsername, buyerEmail, 50);
            }

            System.out.println("\n=== [" + sellerUsername + "] REST: add item (purchase) ===");
            String purchasedItemId = runRestAddItem(sellerEmail, "Gaming Laptop", 10);
            System.out.println("[" + sellerUsername + "] Created itemId (purchase): " + purchasedItemId);

            System.out.println("\n=== [" + sellerUsername + "] Socket: add item (available) ===");
            String availableItemId = runSocketAddItem(sellerClient, sellerUsername, sellerEmail, "Office Laptop", 12);
            System.out.println("[" + sellerUsername + "] Created itemId (available): " + availableItemId);

            if (buyerId != null) {
                runPurchase(buyerClient, buyerUsername, purchasedItemId);
            }

            System.out.println("\n=== [" + sellerUsername + "] Socket: MANAGE_INVENTORY ===");
            runManageInventory(sellerClient, sellerId);

            System.out.println("\n=== [" + sellerUsername + "] Socket: SEARCH_ITEMS ===");
            runSocketSearch(sellerClient, null, "Dell");

            System.out.println("\n=== [" + sellerUsername + "] REST: search items ===");
            runRestSearch(null, "Dell");

            System.out.println("\n=== [" + sellerUsername + "] Socket: EDIT_ITEM ===");
            runEditItem(sellerClient, sellerEmail, availableItemId, "Office Laptop Pro", "Updated description", 60, 1);

            System.out.println("\n=== [" + sellerUsername + "] REST: edit item ===");
            runRestEditItem(availableItemId, sellerEmail, "Office Laptop Pro REST", "Updated via REST", 60, 1);

            System.out.println("\n=== [" + sellerUsername + "] REST: delete item ===");
            runRestDeleteItem(availableItemId, sellerEmail);

            System.out.println("\n=== [" + sellerUsername + "] Socket: delete item ===");
            runSocketDeleteItem(sellerClient, sellerUsername, purchasedItemId, sellerEmail);

            System.out.println("\n=== [" + sellerUsername + "] Socket: VIEW_ACCOUNT ===");
            runViewAccount(sellerClient, sellerEmail);

            System.out.println("\n=== [" + sellerUsername + "] Socket: SEARCH_ITEMS (after edit) ===");
            runSocketSearch(sellerClient, "Gaming", null);

            System.out.println("\n=== [" + sellerUsername + "] Socket: GET_REPORT ===");
            runReportTest(sellerClient, sellerEmail);

            if(buyerId!=null){
                System.out.println("\n=== [" + buyerUsername + "] Socket: GET_REPORT ===");
                runReportTest(buyerClient, buyerEmail);
            }

            sendExit(sellerClient, sellerUsername);
            sendExit(buyerClient, buyerUsername);
        } catch (Exception e) {
            System.out.println("[" + sellerUsername + "] Demo flow error: " + e.getMessage());
            sendExit(sellerClient, sellerUsername);
            sendExit(buyerClient, buyerUsername);
        }
    }

    public static void runRegistration(Client c, String username, String password, String email, String region){
        try {
            JsonObject req = new JsonObject();
            req.addProperty("action", "REGISTER");
            req.addProperty("username", username);
            req.addProperty("password", password);
            req.addProperty("email", email);
            req.addProperty("region", region);

            System.out.println("[User " + username + "]: Sending REGISTER...");
            c.sendRequest(req);
            System.out.println("[User " + username + "]: " + c.receiveResponse());
        } catch (IOException e) {
            System.out.println("Registration error: " + e);
        }
    }

    public static String runLogin(Client c, String email, String password, String username) {
        try {
            JsonObject loginReq = new JsonObject();
            loginReq.addProperty("action", "LOGIN");
            loginReq.addProperty("email", email);
            loginReq.addProperty("password", password);

            System.out.println("[User " + username + "]: Sending LOGIN...");
            c.sendRequest(loginReq);
            String responseStr = c.receiveResponse();
            System.out.println("[User " + username + "]: " + responseStr);

            JsonObject response = JsonParser.parseString(responseStr).getAsJsonObject();
            if ("OK".equals(response.get("status").getAsString()) && response.has("userId")) {
                return response.get("userId").getAsString();
            }
        } catch (IOException e) {
            System.out.println("Login error: " + e);
        }
        return null;
    }

    public static void runViewAccount(Client c, String email) {
        try {
            JsonObject req = new JsonObject();
            req.addProperty("action", "VIEW_ACCOUNT");
            req.addProperty("email", email);

            System.out.println("Sending VIEW_ACCOUNT for " + email + "...");
            c.sendRequest(req);
            System.out.println("Response: " + c.receiveResponse());
        } catch (IOException e) {
            System.out.println("VIEW_ACCOUNT error: " + e);
        }
    }

    public static void runManageInventory(Client c, String userId) {
        try {
            JsonObject req = new JsonObject();
            req.addProperty("action", "MANAGE_INVENTORY");
            req.addProperty("userId", userId);

            System.out.println("Sending MANAGE_INVENTORY for userId " + userId + "...");
            c.sendRequest(req);
            System.out.println("Response: " + c.receiveResponse());
        } catch (IOException e) {
            System.out.println("MANAGE_INVENTORY error: " + e);
        }
    }

    public static void runSocketSearch(Client c, String name, String brand) {
        try {
            JsonObject req = new JsonObject();
            req.addProperty("action", "SEARCH_ITEMS");
            if (name != null && !name.isBlank()) {
                req.addProperty("name", name);
            }
            if (brand != null && !brand.isBlank()) {
                req.addProperty("brand", brand);
            }

            System.out.println("Sending SEARCH_ITEMS...");
            c.sendRequest(req);
            System.out.println("Response: " + c.receiveResponse());
        } catch (IOException e) {
            System.out.println("SEARCH_ITEMS error: " + e);
        }
    }

    public static void runEditItem(Client c, String email, String itemId, String itemName,
                                   String description, double price, int quantity) {
        try {
            JsonObject req = new JsonObject();
            req.addProperty("action", "EDIT_ITEM");
            req.addProperty("email", email);
            req.addProperty("itemId", itemId);
            req.addProperty("itemName", itemName);
            req.addProperty("description", description);
            req.addProperty("price", price);
            req.addProperty("quantity", quantity);
            req.addProperty("categoryId", testCategoryId);
            req.addProperty("brandId", testBrandId);

            System.out.println("Sending EDIT_ITEM for itemId " + itemId + "...");
            c.sendRequest(req);
            System.out.println("Response: " + c.receiveResponse());
        } catch (IOException e) {
            System.out.println("EDIT_ITEM error: " + e);
        }
    }

    public static void runRestSearch(String name, String brand) throws Exception {
        StringBuilder uri = new StringBuilder("http://localhost:7000/items/search?");
        boolean hasParam = false;
        if (name != null && !name.isBlank()) {
            uri.append("name=").append(URLEncoder.encode(name, StandardCharsets.UTF_8));
            hasParam = true;
        }
        if (brand != null && !brand.isBlank()) {
            if (hasParam) {
                uri.append("&");
            }
            uri.append("brand=").append(URLEncoder.encode(brand, StandardCharsets.UTF_8));
            hasParam = true;
        }
        if (!hasParam) {
            System.out.println("REST search skipped: provide at least name or brand");
            return;
        }

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri.toString()))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("REST /items/search status: " + response.statusCode());
        System.out.println("REST /items/search body: " + response.body());
    }

    public static void runRestEditItem(String itemId, String email, String itemName,
                                       String description, double price, int quantity) throws Exception {
        String json = "{\"email\":\"" + email + "\",\"itemName\":\"" + itemName
                + "\",\"description\":\"" + description + "\",\"price\":" + price
                + ",\"quantity\":" + quantity + ",\"categoryId\":" + testCategoryId + ",\"brandId\":" + testBrandId + "}";

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:7000/items/" + itemId))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("REST PUT /items/" + itemId + " status: " + response.statusCode());
        System.out.println("REST PUT /items/" + itemId + " body: " + response.body());
    }

    public static String runSocketAddItem(Client c, String username, String email, String itemName, double price) throws Exception {
        JsonObject req = new JsonObject();
        req.addProperty("action", "ADD_ITEM");
        req.addProperty("itemName", itemName);
        req.addProperty("description", itemName);
        req.addProperty("price", price);
        req.addProperty("quantity", 2);
        req.addProperty("categoryId", testCategoryId);
        req.addProperty("brandId", testBrandId);
        req.addProperty("email", email);

        System.out.println("[User " + username + "]: Sending ADD_ITEM for " + itemName + "...");
        c.sendRequest(req);
        String responseStr = c.receiveResponse();
        System.out.println("[User " + username + "]: " + responseStr);

        JsonObject response = JsonParser.parseString(responseStr).getAsJsonObject();
        if (!"OK".equals(response.get("status").getAsString())) {
            throw new RuntimeException("Add item failed: " + responseStr);
        }
        return response.get("itemId").getAsString();
    }

    public static void runPurchase(Client c, String username, String itemId){
        try {
            JsonObject purchaseReq = new JsonObject();
            purchaseReq.addProperty("action", "PURCHASE");
            purchaseReq.addProperty("itemId", itemId);

            System.out.println("[User " + username + "]: Sending purchase request for item ID " + itemId + "...");
            c.sendRequest(purchaseReq);
            System.out.println("[User " + username + "]: Server replied: " + c.receiveResponse());

        } catch (IOException e) {
            System.out.println("Purchase Error: " + e);
        }
    }

    public static void runSocketDeleteItem(Client c, String username, String itemId, String email) throws Exception {
        JsonObject req = new JsonObject();
        req.addProperty("action", "DELETE_ITEM");
        req.addProperty("itemId", itemId);
        req.addProperty("email", email);

        System.out.println("[User " + username + "]: Sending DELETE_ITEM for item ID " + itemId + "...");
        c.sendRequest(req);
        String responseStr = c.receiveResponse();
        System.out.println("[User " + username + "]: " + responseStr);

        JsonObject response = JsonParser.parseString(responseStr).getAsJsonObject();
        if (!"OK".equals(response.get("status").getAsString())) {
            throw new RuntimeException("Delete item failed: " + responseStr);
        }
    }

    public static void runSocketDeposit(Client c, String username, String email, double amount) throws Exception {
        JsonObject req = new JsonObject();
        req.addProperty("action", "DEPOSIT");
        req.addProperty("email", email);
        req.addProperty("amount", amount);

        System.out.println("[User " + username + "]: Sending DEPOSIT for " + amount + "...");
        c.sendRequest(req);
        String responseStr = c.receiveResponse();
        System.out.println("[User " + username + "]: " + responseStr);

        JsonObject response = JsonParser.parseString(responseStr).getAsJsonObject();
        if (!"OK".equals(response.get("status").getAsString())) {
            throw new RuntimeException("Deposit failed: " + responseStr);
        }
    }

    public static void sendExit(Client c, String username) {
        try {
            JsonObject exitReq = new JsonObject();
            exitReq.addProperty("action", "EXIT");
            c.sendRequest(exitReq);
            System.out.println("[User " + username + "]: Connection closing.");
        } catch (IOException e) {
            System.out.println("Exit error: " + e);
        }
    }

    public static void runRestDepositTest(String email, double amount) throws Exception {
        HttpClient client = HttpClient.newHttpClient();

        String json = "{\"email\":\"" + email + "\",\"amount\":" + amount + "}";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:7000/deposit"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("REST /deposit status: " + response.statusCode());
        System.out.println("REST /deposit body: " + response.body());
    }

    public static String runRestAddItem(String email) throws Exception {
        return runRestAddItem(email, "Gaming Laptop", 10);
    }

    public static String runRestAddItem(String email, String itemName, double price) throws Exception {
        HttpClient client = HttpClient.newHttpClient();

        String json = "{\"itemName\":\"" + itemName + "\",\"description\":\"" + itemName + "\",\"price\":" + price + ",\"quantity\":2,\"categoryId\":" + testCategoryId + ",\"brandId\":" + testBrandId + ",\"email\":\"" + email + "\"}";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:7000/items"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("REST /items status: " + response.statusCode());
        System.out.println("REST /items body: " + response.body());

        JsonObject body = JsonParser.parseString(response.body()).getAsJsonObject();
        if (!"OK".equals(body.get("status").getAsString())) {
            throw new RuntimeException("Add item failed: " + response.body());
        }
        return body.get("itemId").getAsString();
    }

    public static void runRestDeleteItem(String itemId, String email) throws Exception {
        HttpClient client = HttpClient.newHttpClient();

        String json = "{\"email\":\"" + email + "\"}";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:7000/items/" + itemId))
                .header("Content-Type", "application/json")
                .method("DELETE", HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("REST DELETE /items/" + itemId + " status: " + response.statusCode());
        System.out.println("REST DELETE /items/" + itemId + " body: " + response.body());
    }

    private static void ensureTestCatalogData() throws SQLException {
        try (Connection conGlobal = DriverManager.getConnection(DB_BASE_URL + "databaseName=Global;", DB_USER, DB_PASS)) {
            Category_DAO categoryDao = new Category_DAO(conGlobal);
            Brand_DAO brandDao = new Brand_DAO(conGlobal);
            testCategoryId = categoryDao.findOrCreateCategory("Electronics");
            testBrandId = brandDao.findOrCreateBrand("Dell", "https://example.com/dell.png");
        }
        System.out.println("Test catalog ready: categoryId=" + testCategoryId + ", brandId=" + testBrandId);
    }

    public static void runReportTest(Client c, String email) {
        try {
            JsonObject req = new JsonObject();
            req.addProperty("action", "GET_REPORT");
            req.addProperty("email", email);

            c.sendRequest(req);
            System.out.println("REPORT response: " + c.receiveResponse());
        } catch (IOException e) {
            System.out.println("Report Error: " + e);
        }
    }
}
