/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package edu.asu.ecommerce.rest;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import edu.asu.ecommerce.dataaccess.Item_DAO;
import edu.asu.ecommerce.dataaccess.UserInventory_DAO;
import edu.asu.ecommerce.dataaccess.models.ExternalStoreInfo;
import edu.asu.ecommerce.dataaccess.models.Item;
import edu.asu.ecommerce.dataaccess.models.User;
import edu.asu.ecommerce.services.AuthenticationService;
import edu.asu.ecommerce.services.ExternalStoreService;
import edu.asu.ecommerce.services.ItemService;
import edu.asu.ecommerce.services.UserService;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Dell
 */
public class RestServer {
	private static final String BASE_URL = "jdbc:sqlserver://localhost:1433;encrypt=true;trustServerCertificate=true;";
	private static final String DB_USER = "sa";
	private static final String DB_PASS = "123456";

	public static void main(String[] args) {
		Javalin app = Javalin.create(config -> config.http.defaultContentType = "application/json").start(7000);

		app.post("/items", ctx -> {
            implementAddItem(ctx);
		});

		app.put("/items/{id}", ctx -> {
			notImplemented(ctx, "Update item not implemented");
		});

		app.delete("/items/{id}", ctx -> {
			notImplemented(ctx, "Delete item not implemented");
		});

		app.get("/items/search", ctx -> {
			notImplemented(ctx, "Search items not implemented");
		});

		app.post("/deposit", ctx -> {
            implementDeposit(ctx);
        });

		app.post("/external-stores/register", ctx -> {
			implementExternalStoreRegister(ctx);
		});

		app.get("/external-stores/items", ctx -> {
			implementExternalStoreItems(ctx);
		});
	}

	private static JsonObject parseJson(Context ctx) {
		String body = ctx.body();
		if (body == null || body.isBlank()) {
			return new JsonObject();
		}
		try {
			return JsonParser.parseString(body).getAsJsonObject();
		} catch (Exception e) {
			return null;
		}
	}


	private static JsonObject errorResponse(String code, String message) {
		JsonObject response = new JsonObject();
		response.addProperty("status", "ERR");
		response.addProperty("code", code);
		response.addProperty("message", message);
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
	private static Integer getInteger(JsonObject obj, String key) {
		if (obj == null || !obj.has(key) || obj.get(key).isJsonNull()) {
			return null;
		}
		return obj.get(key).getAsInt();
	}

	private static void notImplemented(Context ctx, String message) {
		ctx.status(501).result(errorResponse("501", message).toString());
	}

    private static void implementAddItem(Context ctx){
        {
			JsonObject request = parseJson(ctx);
			if (request == null) {
				ctx.status(400).result(errorResponse("400", "Invalid JSON").toString());
				return;
			}
			String itemName = getString(request, "itemName");
			String description = getString(request, "description");
			Double price = getDouble(request, "price");
			Integer quantity = getInteger(request, "quantity");
			Integer categoryId = getInteger(request, "categoryId");
			Integer brandId = getInteger(request, "brandId");
			String email = getString(request, "email");

			if (itemName == null || description == null || price == null || quantity == null
					|| categoryId == null || brandId == null || email == null || email.isBlank()) {
				ctx.status(400).result(errorResponse("400", "Missing required fields").toString());
				return;
			}

			try (Connection conSecure = DriverManager.getConnection(BASE_URL + "databaseName=Secure;", DB_USER, DB_PASS);
					 Connection conGlobal = DriverManager.getConnection(BASE_URL + "databaseName=Global;", DB_USER, DB_PASS);
					 Connection conNorth = DriverManager.getConnection(BASE_URL + "databaseName=North;", DB_USER, DB_PASS);
					 Connection conSouth = DriverManager.getConnection(BASE_URL + "databaseName=South;", DB_USER, DB_PASS)) {

				AuthenticationService authService = new AuthenticationService(conSecure, conNorth, conSouth);
				User user = authService.getUserByEmail(email);
				if (user == null) {
					ctx.status(404).result(errorResponse("404", "User not found").toString());
					return;
				}

				ItemService productServices = new ItemService(conGlobal);
				String itemId = productServices.addItem(itemName, description, price, quantity,
						categoryId, brandId);

				UserService userService = new UserService(conSecure, conNorth, conSouth);
				try {
					userService.createInventoryEntry(user.getId(), itemId, "Available");
				} catch (Exception e) {
					productServices.deleteItem(itemId);
					throw e;
				}

				JsonObject response = new JsonObject();
				response.addProperty("status", "OK");
				response.addProperty("message", "Item created");
				response.addProperty("itemId", itemId);
				ctx.result(response.toString());
			} catch (SQLException se) {
				ctx.status(500).result(errorResponse("777", "SQL ERROR").toString());
			} catch (Exception e) {
				ctx.status(400).result(errorResponse("505", e.getMessage()).toString());
			}
        }
    }
    private static void implementDeposit(Context ctx){
        JsonObject request = parseJson(ctx);
			if (request == null) {
				ctx.status(400).result(errorResponse("400", "Invalid JSON").toString());
				return;
			}

			String email = getString(request, "email");
			Double amount = getDouble(request, "amount");

			if (email == null || email.isBlank() || amount == null) {
				ctx.status(400).result(errorResponse("400", "email and amount are required").toString());
				return;
			}
			if (amount <= 0) {
				ctx.status(400).result(errorResponse("400", "amount must be greater than zero").toString());
				return;
			}

			try (Connection conSecure = DriverManager.getConnection(BASE_URL + "databaseName=Secure;", DB_USER, DB_PASS);
					 Connection conNorth = DriverManager.getConnection(BASE_URL + "databaseName=North;", DB_USER, DB_PASS);
					 Connection conSouth = DriverManager.getConnection(BASE_URL + "databaseName=South;", DB_USER, DB_PASS)) {

				UserService userService = new UserService(conSecure, conNorth, conSouth);
				AuthenticationService authService = new AuthenticationService(conSecure, conNorth, conSouth);

                userService.depositCash(email, amount);
				User user = authService.getUserByEmail(email);

				if (user == null) {
					ctx.status(404).result(errorResponse("404", "User not found").toString());
					return;
				}

				JsonObject response = new JsonObject();
				response.addProperty("status", "OK");
				response.addProperty("message", "Deposit successful");
				response.addProperty("balance", user.getBalance());
				ctx.result(response.toString());
			} catch (SQLException se) {
				ctx.status(500).result(errorResponse("777", "SQL ERROR").toString());
			} catch (Exception e) {
				ctx.status(400).result(errorResponse("505", e.getMessage()).toString());
			}
    }

	private static void implementExternalStoreRegister(Context ctx) {
		JsonObject request = parseJson(ctx);
		if (request == null) {
			ctx.status(400).result(errorResponse("400", "Invalid JSON").toString());
			return;
		}

		String ownerEmail = getString(request, "ownerEmail");
		String storeName = getString(request, "storeName");
		String apiEndpoint = getString(request, "apiEndpoint");

		if (ownerEmail == null || ownerEmail.isBlank() || storeName == null || storeName.isBlank()
				|| apiEndpoint == null || apiEndpoint.isBlank()) {
			ctx.status(400).result(errorResponse("400", "Missing required fields").toString());
			return;
		}

		try (Connection conSecure = DriverManager.getConnection(BASE_URL + "databaseName=Secure;", DB_USER, DB_PASS);
			 Connection conNorth = DriverManager.getConnection(BASE_URL + "databaseName=North;", DB_USER, DB_PASS);
			 Connection conSouth = DriverManager.getConnection(BASE_URL + "databaseName=South;", DB_USER, DB_PASS)) {

			AuthenticationService authService = new AuthenticationService(conSecure, conNorth, conSouth);
			User owner = authService.getUserByEmail(ownerEmail);
			if (owner == null) {
				ctx.status(404).result(errorResponse("404", "Owner not found").toString());
				return;
			}

			ExternalStoreService storeService = new ExternalStoreService(conNorth, conSouth);
			ExternalStoreInfo store = storeService.createStore(owner.getId(), storeName, apiEndpoint);

			JsonObject response = new JsonObject();
			response.addProperty("status", "OK");
			response.addProperty("storeId", store.getStoreId());
			response.addProperty("apiKey", store.getApiKey());
			ctx.result(response.toString());
		} catch (SQLException se) {
			ctx.status(500).result(errorResponse("777", "SQL ERROR").toString());
		} catch (Exception e) {
			ctx.status(400).result(errorResponse("505", e.getMessage()).toString());
		}
	}

	private static void implementExternalStoreItems(Context ctx) {
		String apiKey = ctx.header("X-API-KEY");
		if (apiKey == null || apiKey.isBlank()) {
			ctx.status(401).result(errorResponse("401", "Missing API key").toString());
			return;
		}

		try (Connection conGlobal = DriverManager.getConnection(BASE_URL + "databaseName=Global;", DB_USER, DB_PASS);
			 Connection conNorth = DriverManager.getConnection(BASE_URL + "databaseName=North;", DB_USER, DB_PASS);
			 Connection conSouth = DriverManager.getConnection(BASE_URL + "databaseName=South;", DB_USER, DB_PASS)) {

			ExternalStoreService storeService = new ExternalStoreService(conNorth, conSouth);
			ExternalStoreInfo store = storeService.getStoreByApiKey(apiKey);
			if (store == null) {
				ctx.status(401).result(errorResponse("401", "Invalid API key").toString());
				return;
			}

			UserInventory_DAO inventoryDaoNorth = new UserInventory_DAO(conNorth);
			UserInventory_DAO inventoryDaoSouth = new UserInventory_DAO(conSouth);
			Set<String> itemIds = new HashSet<>();
			itemIds.addAll(inventoryDaoNorth.getItemIdsByState("Available"));
			itemIds.addAll(inventoryDaoSouth.getItemIdsByState("Available"));

			Item_DAO itemDao = new Item_DAO(conGlobal);
			List<Item> items = itemDao.getItemsByIds(new ArrayList<>(itemIds));

			JsonArray itemsArray = new JsonArray();
			for (Item item : items) {
				JsonObject obj = new JsonObject();
				obj.addProperty("itemId", item.getId());
				obj.addProperty("itemName", item.getItemName());
				obj.addProperty("description", item.getDescription());
				obj.addProperty("price", item.getPrice());
				obj.addProperty("quantity", item.getQuantity());
				obj.addProperty("categoryId", item.getCategoryId());
				obj.addProperty("brandId", item.getBrandId());
				itemsArray.add(obj);
			}

			JsonObject response = new JsonObject();
			response.addProperty("status", "OK");
			response.add("items", itemsArray);
			ctx.result(response.toString());
		} catch (SQLException se) {
			ctx.status(500).result(errorResponse("777", "SQL ERROR").toString());
		}
	}
}
