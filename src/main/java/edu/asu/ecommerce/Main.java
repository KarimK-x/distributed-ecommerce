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
import java.util.concurrent.CompletableFuture;

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

        CompletableFuture<String> publishedItemFuture = new CompletableFuture<>();
        
        CompletableFuture<Void> purchaseCompletedFuture = new CompletableFuture<>();

        // THREAD 1 THE SELLER
        Thread sellerThread = new Thread(() -> {
            try {
                System.out.println("=== [" + TEST_USERNAME + "] Setup: register & login ===");
                runRegistration(sellerClient, TEST_USERNAME, TEST_PASSWORD, TEST_EMAIL, TEST_REGION_1);
                String sellerId = runLogin(sellerClient, TEST_EMAIL, TEST_PASSWORD, TEST_USERNAME);
                
                if (sellerId == null) {
                    System.out.println("[" + TEST_USERNAME + "] Login failed; stopping tests.");
                    publishedItemFuture.completeExceptionally(new RuntimeException("Seller login failed"));
                    sendExit(sellerClient, TEST_USERNAME);
                    return;
                }
                System.out.println("[" + TEST_USERNAME + "] Logged in userId: " + sellerId);

                System.out.println("\n=== [" + TEST_USERNAME + "] REST: add item (purchase) ===");
                String purchasedItemId = runRestAddItem(TEST_EMAIL, "Gaming Laptop", 10);
                System.out.println("[" + TEST_USERNAME + "] Created itemId (purchase): " + purchasedItemId);
                
                System.out.println("\n=== [" + TEST_USERNAME + "] Socket: add item (available) ===");
                String availableItemId = runSocketAddItem(sellerClient, TEST_USERNAME, TEST_EMAIL, "Office Laptop", 12);
                System.out.println("[" + TEST_USERNAME + "] Created itemId (available): " + availableItemId);

                // SYNC POINT 1: Hand the itemId over to the buyer thread!
                publishedItemFuture.complete(purchasedItemId);

                // SYNC POINT 2: Wait for the buyer to finish purchasing before checking account state
                System.out.println("[" + TEST_USERNAME + "] Waiting for buyer to complete transaction...");
                purchaseCompletedFuture.join();
                System.out.println("[" + TEST_USERNAME + "] Transaction verified. Resuming checks...");

                System.out.println("\n=== [" + TEST_USERNAME + "] Socket: MANAGE_INVENTORY ===");
                runManageInventory(sellerClient, sellerId);

                System.out.println("\n=== [" + TEST_USERNAME + "] Socket: SEARCH_ITEMS ===");
                runSocketSearch(sellerClient, null, "Dell");
                
                System.out.println("\n=== [" + TEST_USERNAME + "] REST: search items ===");
                runRestSearch(null, "Dell");
                
                System.out.println("\n=== [" + TEST_USERNAME + "] Socket: EDIT_ITEM ===");
                runEditItem(sellerClient, TEST_EMAIL, availableItemId, "Office Laptop Pro", "Updated description", 60, 1);
                
                System.out.println("\n=== [" + TEST_USERNAME + "] REST: edit item ===");
                runRestEditItem(availableItemId, TEST_EMAIL, "Office Laptop Pro REST", "Updated via REST", 60, 1);
                
                System.out.println("\n=== [" + TEST_USERNAME + "] REST: delete item ===");
                runRestDeleteItem(availableItemId, TEST_EMAIL);
                
                System.out.println("\n=== [" + TEST_USERNAME + "] Socket: delete item ===");

                // Note: This is meant to fail y3ny to show deleting item that is sold is not an option
                runSocketDeleteItem(sellerClient, TEST_USERNAME, purchasedItemId, TEST_EMAIL);
                
                System.out.println("\n=== [" + TEST_USERNAME + "] Socket: VIEW_ACCOUNT ===");
                runViewAccount(sellerClient, TEST_EMAIL);
              
                System.out.println("\n=== [" + sellerUsername + "] Socket: BULK_UPLOAD_ITEMS ===");
                String bulkCsv = "itemName,description,price,quantity,categoryId,brandId,email\n"
                    + "Mechanical Keyboard,RGB TKL keyboard,89.99,20," + testCategoryId + "," + testBrandId + "," + sellerEmail + "\n"
                    + "Gaming Mouse,High DPI gaming mouse,59.99,15," + testCategoryId + "," + testBrandId + "," + sellerEmail + "\n"
                    + "USB Hub,7-port USB 3.0 hub,34.99,50," + testCategoryId + "," + testBrandId + "," + sellerEmail;
                runBulkUploadItems(sellerClient, bulkCsv);

                System.out.println("\n=== [" + TEST_USERNAME + "] Socket: GET_REPORT ===");
                runReportTest(sellerClient, TEST_EMAIL);
                
                System.out.println("\n=== [" + TEST_USERNAME + "] Socket: SEARCH_ITEMS (after edit) ===");
                runSocketSearch(sellerClient, "Gaming", null);

                sendExit(sellerClient, TEST_USERNAME);
                
            } catch (Exception e) {
                System.out.println("[" + TEST_USERNAME + "] Demo flow error:");
                e.printStackTrace();
                publishedItemFuture.completeExceptionally(e);
                sendExit(sellerClient, TEST_USERNAME);
            }
        });

        // THREAD 2 THE BUYER

        Thread buyerThread = new Thread(() -> {
            try {
                System.out.println("=== [" + TEST_USERNAME_2 + "] Setup: register & login ===");
                runRegistration(buyerClient, TEST_USERNAME_2, TEST_PASSWORD_2, TEST_EMAIL_2, TEST_REGION_2);
                String buyerId = runLogin(buyerClient, TEST_EMAIL_2, TEST_PASSWORD_2, TEST_USERNAME_2);
                
                if (buyerId == null) {
                    System.out.println("[" + TEST_USERNAME_2 + "] Login failed; stopping purchase step.");
                    purchaseCompletedFuture.completeExceptionally(new RuntimeException("Buyer login failed"));
                } else {
                    System.out.println("[" + TEST_USERNAME_2 + "] Logged in userId: " + buyerId);
                    
                    System.out.println("\n=== [" + TEST_USERNAME_2 + "] REST: deposit ===");
                    runRestDepositTest(TEST_EMAIL_2, 10000);

                    System.out.println("\n=== [" + TEST_USERNAME_2 + "] Socket: deposit ===");
                    runSocketDeposit(buyerClient, TEST_USERNAME_2, TEST_EMAIL_2, 50);

                    System.out.println("[" + TEST_USERNAME_2 + "] Waiting for item to hit the market...");

                    // Like el sempahore it waits until seller signals it with the id
                    String itemIdToBuy = publishedItemFuture.join(); 

                    System.out.println("\n=== [" + TEST_USERNAME_2 + "] Item found! Purchasing... ===");
                    runPurchase(buyerClient, TEST_USERNAME_2, itemIdToBuy);
                    
                    // SYNC POINT 2: Tell the seller we are done!
                    purchaseCompletedFuture.complete(null);
                }

                System.out.println("\n=== [" + TEST_USERNAME_2 + "] Socket: GET_REPORT ===");
                runReportTest(buyerClient, TEST_EMAIL_2);

                sendExit(buyerClient, TEST_USERNAME_2);
                
            } catch (Exception e) {
                System.out.println("[" + TEST_USERNAME_2 + "] Demo flow error:");
                e.printStackTrace();
                purchaseCompletedFuture.completeExceptionally(e);
                sendExit(buyerClient, TEST_USERNAME_2);
            }
        });

        sellerThread.start();
        buyerThread.start();

        sellerThread.join();
        buyerThread.join();
        
        System.out.println("Demo complete.");
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

    /**
     * Sends a BULK_UPLOAD_ITEMS socket action with CSV content embedded in the JSON payload.
     *
     * Example csvContent:
     *   "itemName,description,price,quantity,categoryId,brandId,email\n
     *    Gaming Laptop,Fast laptop,999.99,5,1,1,seller@email.com"
     */
    public static void runBulkUploadItems(Client c, String csvContent) {
        try {
            JsonObject req = new JsonObject();
            req.addProperty("action", "BULK_UPLOAD_ITEMS");
            req.addProperty("csvContent", csvContent);

            System.out.println("Sending BULK_UPLOAD_ITEMS...");
            c.sendRequest(req);
            System.out.println("BULK_UPLOAD_ITEMS response: " + c.receiveResponse());
        } catch (IOException e) {
            System.out.println("BULK_UPLOAD_ITEMS error: " + e);
        }
    }
}
