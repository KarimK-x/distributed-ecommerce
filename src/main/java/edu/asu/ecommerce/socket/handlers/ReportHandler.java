package edu.asu.ecommerce.socket.handlers;

import com.google.gson.JsonObject;
import edu.asu.ecommerce.services.ReportService;

import java.sql.SQLException;

public class ReportHandler {
    private final ReportService reportService;
    private final JsonObject request;

    public ReportHandler(ReportService reportService, JsonObject request){
        this.reportService = reportService;
        this.request = request;
    }

    public JsonObject handle(){
        JsonObject response;
        try {
            String email = request.get("email").getAsString();
            response = reportService.generateReport(email);
        } catch (SQLException se) {
            response = new JsonObject();
            response.addProperty("status", "ERR");
            response.addProperty("code", "777");
            response.addProperty("message", "SQL ERROR");
        } catch (Exception e) {
            response = new JsonObject();
            response.addProperty("status", "ERR");
            response.addProperty("code", "505");
            response.addProperty("message", e.getMessage());
        }

        return response;
    }
}
