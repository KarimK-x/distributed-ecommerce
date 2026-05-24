package edu.asu.ecommerce.socket.handlers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import edu.asu.ecommerce.dataaccess.models.User;
import edu.asu.ecommerce.services.AuthenticationService;
import edu.asu.ecommerce.services.ItemService;
import edu.asu.ecommerce.services.UserService;
import edu.asu.ecommerce.util.CSVParserUtil;

import java.util.List;
import java.util.Map;

/**
 * Handles the socket action BULK_UPLOAD_ITEMS.
 *
 * Expected request JSON:
 * {
 *   "action": "BULK_UPLOAD_ITEMS",
 *   "csvContent": "itemName,description,price,quantity,categoryId,brandId,email\nLaptop,Fast,999.99,5,1,1,seller@email.com"
 * }
 *
 * Response JSON (always status OK at the envelope level; per-row ERR inside results):
 * {
 *   "status": "OK",
 *   "totalRows": 2,
 *   "successCount": 1,
 *   "failCount": 1,
 *   "results": [
 *     { "row": 1, "status": "OK",  "itemId": "G-001", "itemName": "Laptop" },
 *     { "row": 2, "status": "ERR", "message": "User not found for email: x@y.com" }
 *   ]
 * }
 */
public class BulkUploadItemsHandler {

    private final AuthenticationService authService;
    private final ItemService itemService;
    private final UserService userService;

    public BulkUploadItemsHandler(AuthenticationService authService,
                                  ItemService itemService,
                                  UserService userService) {
        this.authService = authService;
        this.itemService = itemService;
        this.userService = userService;
    }

    public JsonObject handle(JsonObject request) {
        JsonObject response = new JsonObject();

        // ── 1. Extract CSV content ────────────────────────────────────────────────
        if (!request.has("csvContent") || request.get("csvContent").isJsonNull()) {
            response.addProperty("status", "ERR");
            response.addProperty("code", "400");
            response.addProperty("message", "csvContent field is required");
            return response;
        }
        String csvContent = request.get("csvContent").getAsString();

        // ── 2. Parse CSV ──────────────────────────────────────────────────────────
        List<Map<String, String>> rows;
        try {
            rows = CSVParserUtil.parse(csvContent);
        } catch (Exception e) {
            response.addProperty("status", "ERR");
            response.addProperty("code", "400");
            response.addProperty("message", "CSV parse error: " + e.getMessage());
            return response;
        }

        // ── 3. Process each row ───────────────────────────────────────────────────
        JsonArray results = new JsonArray();
        int successCount = 0;
        int failCount = 0;

        for (int i = 0; i < rows.size(); i++) {
            Map<String, String> row = rows.get(i);
            JsonObject rowResult = new JsonObject();
            rowResult.addProperty("row", i + 1);

            try {
                // Validate & extract fields
                String itemName    = row.get("itemName");
                String description = row.get("description");
                String email       = row.get("email");

                if (itemName == null || itemName.isBlank()) {
                    throw new Exception("itemName is blank");
                }
                if (description == null || description.isBlank()) {
                    throw new Exception("description is blank");
                }
                if (email == null || email.isBlank()) {
                    throw new Exception("email is blank");
                }

                double price      = Double.parseDouble(row.get("price"));
                int    quantity   = Integer.parseInt(row.get("quantity"));
                int    categoryId = Integer.parseInt(row.get("categoryId"));
                int    brandId    = Integer.parseInt(row.get("brandId"));

                if (price <= 0)    throw new Exception("price must be > 0");
                if (quantity <= 0) throw new Exception("quantity must be > 0");

                // Look up seller
                User user = authService.getUserByEmail(email);
                if (user == null) {
                    throw new Exception("User not found for email: " + email);
                }

                // Create item + inventory entry (with rollback if inventory insert fails)
                String itemId = itemService.addItem(itemName, description, price, quantity,
                        categoryId, brandId, user.getId());
                try {
                    userService.createInventoryEntry(user.getId(), itemId, "Available");
                } catch (Exception e) {
                    itemService.deleteItem(itemId); // rollback item
                    throw e;
                }

                rowResult.addProperty("status", "OK");
                rowResult.addProperty("itemId", itemId);
                rowResult.addProperty("itemName", itemName);
                successCount++;

            } catch (NumberFormatException nfe) {
                rowResult.addProperty("status", "ERR");
                rowResult.addProperty("message", "Invalid number value in row: " + nfe.getMessage());
                failCount++;
            } catch (Exception e) {
                rowResult.addProperty("status", "ERR");
                rowResult.addProperty("message", e.getMessage());
                failCount++;
            }

            results.add(rowResult);
        }

        // ── 4. Build summary response ─────────────────────────────────────────────
        response.addProperty("status", "OK");
        response.addProperty("totalRows", rows.size());
        response.addProperty("successCount", successCount);
        response.addProperty("failCount", failCount);
        response.add("results", results);
        return response;
    }
}
