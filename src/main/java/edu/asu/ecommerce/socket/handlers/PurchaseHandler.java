package edu.asu.ecommerce.socket.handlers;

import com.google.gson.JsonObject;
import edu.asu.ecommerce.dataaccess.models.Item;
import edu.asu.ecommerce.services.AuthenticationService;
import edu.asu.ecommerce.services.ItemService;
import edu.asu.ecommerce.services.OrderService;
import edu.asu.ecommerce.services.UserService;

import java.sql.SQLException;

public class PurchaseHandler {
    private UserService userService;
    private ItemService itemService;
    private OrderService orderService;

    public PurchaseHandler(UserService userService, ItemService itemService, OrderService orderService){
        this.userService = userService;
        this.itemService = itemService;
        this.orderService = orderService;
    }

    public JsonObject handle(JsonObject request, String buyerId){
        String itemId = request.get("itemId").getAsString();

        JsonObject response = new JsonObject();

        try{
            Item item = itemService.getItemById(itemId);
            userService.purchase(buyerId,item);
            orderService.processFullOrderTransaction(
                buyerId,
                item.getSellerId(),
                itemId,
                item.getPrice(),
                "STANDARD_USER"
            );

            response.addProperty("status","OK");
            response.addProperty("message", "Purchased Successfully");
        }


        catch(SQLException se){
            response.addProperty("status", "ERR");
            response.addProperty("code", "777");
            response.addProperty("message", se.getMessage());
        }




        catch (Exception e){
            response.addProperty("status","ERR");
            response.addProperty("code", "505");
            response.addProperty("message", e.getMessage());
        }


        return response;
    }
}
