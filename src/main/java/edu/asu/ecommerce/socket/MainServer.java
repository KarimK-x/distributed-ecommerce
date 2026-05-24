    /*
     * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
     * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
     */
    package edu.asu.ecommerce.socket;

    import java.io.*;
    import java.net.*;
    import java.sql.Connection;
    import java.sql.DriverManager;
    import java.sql.SQLException;

    import com.google.gson.JsonObject;
    import com.google.gson.JsonParser;
    import edu.asu.ecommerce.dataaccess.models.Item;
    import edu.asu.ecommerce.services.*;
    import edu.asu.ecommerce.services.ItemService;
    import edu.asu.ecommerce.services.UserService;
    import edu.asu.ecommerce.socket.handlers.*;


    public class MainServer {
        private ServerSocket server;

        public MainServer(ServerSocket s) {
            this.server = s;
            System.out.println("Main Server Initialized...");
        }
        public static void main(String[] args) throws IOException, SQLException {


            MainServer s = new MainServer(new ServerSocket(1234));
            while(true){
                Socket client = s.server.accept();
                ClientThread c = new ClientThread(client);
                c.start();
            }
        }
    }

    class ClientThread extends Thread{
        private Socket client;

        public ClientThread(Socket client){
            this.client = client;
        }

        @Override
        public void run(){
            try{
                DataInputStream dis = new DataInputStream(client.getInputStream());
                DataOutputStream dos = new DataOutputStream(client.getOutputStream());
                boolean isRunning = true;
                
                //-----CONNECTIONS-----
                //One connection per client thread.
                String baseURL = "jdbc:sqlserver://localhost:1433;encrypt=true;trustServerCertificate=true;";
                
                //Connection to Centralized Database
                //Connection con = DriverManager.getConnection(url, "sa", "123456");
                //Connection con = DriverManager.getConnection(baseURL + "databaseName=CentralizedMarketplace;", "sa", "123456");
                
                //Connections to Distributed Database
                Connection conSecure = DriverManager.getConnection(baseURL + "databaseName=Secure;", "sa", "123456");
                Connection conGlobal = DriverManager.getConnection(baseURL + "databaseName=Global;", "sa", "123456");
                Connection conNorth  = DriverManager.getConnection(baseURL + "databaseName=North;", "sa", "123456");
                Connection conSouth  = DriverManager.getConnection(baseURL + "databaseName=South;", "sa", "123456");
                
                //----SERVICES----
                AuthenticationService authService = new AuthenticationService(conSecure, conNorth, conSouth); //Sayebha using centralized db for now
                ItemService itemService = new ItemService(conGlobal);
                OrderService orderService = new OrderService(conSecure);
                UserService userService = new UserService(conSecure,conNorth,conSouth);
                ExternalStoreService externalStoreService = new ExternalStoreService(conNorth,conSouth);

                String user_logged = null;

                ReportService reportService = new ReportService(conSecure, conGlobal, conNorth, conSouth);

                
                while(isRunning){
                    String jsonMessage = dis.readUTF();
                    System.out.println("Request Received: " + jsonMessage);
                    JsonObject request = JsonParser.parseString(jsonMessage).getAsJsonObject();
                    String action = request.get("action").getAsString();

                    JsonObject response = new JsonObject();

                    switch(action){
                        case "REGISTER":
                            RegisterHandler regHandler = new RegisterHandler(authService);
                            response = regHandler.handle(request);
                            break;
                        case "LOGIN":
                            LoginHandler logHandler = new LoginHandler(authService, request);
                            response = logHandler.handle();
                            user_logged = response.get("userId").getAsString();
                            break;
                        case "PURCHASE":
                            PurchaseHandler purchaseHandler = new PurchaseHandler(userService,itemService, orderService);
                            response = purchaseHandler.handle(request,user_logged);
                            break;
                        case "GET_REPORT":
                            ReportHandler repHandler = new ReportHandler(reportService, request);
                            response = repHandler.handle();
                            break;
                        case "VIEW_ACCOUNT":
                            ViewAccountHandler viewHandler = new ViewAccountHandler(userService, itemService);
                            response = viewHandler.handle(request);
                            break;
                        case "EDIT_ITEM":
                            EditItemHandler editHandler = new EditItemHandler(userService, itemService);
                            response = editHandler.handle(request);
                            break;
                        case "ADD_ITEM":
                            AddItemHandler addHandler = new AddItemHandler(authService, itemService, userService);
                            response = addHandler.handle(request);
                            break;
                        case "DEPOSIT":
                            DepositHandler depositHandler = new DepositHandler(userService, authService);
                            response = depositHandler.handle(request);
                            break;
                        case "DELETE_ITEM":
                            DeleteItemHandler deleteHandler = new DeleteItemHandler(authService, itemService, conNorth, conSouth);
                            response = deleteHandler.handle(request);
                            break;
                        case "SEARCH_ITEMS":
                            SearchItemsHandler searchHandler = new SearchItemsHandler(itemService, conNorth, conSouth);
                            response = searchHandler.handle(request);
                            break;
                        case "MANAGE_INVENTORY":
                            ManageInventoryHandler inventoryHandler = new ManageInventoryHandler(userService, itemService);
                            response = inventoryHandler.handle(request);
                            break;
                        case "BULK_UPLOAD_ITEMS":
                            BulkUploadItemsHandler bulkHandler = new BulkUploadItemsHandler(authService, itemService, userService);
                            response = bulkHandler.handle(request);
                            break;
                        case "ADD_STORE":
                            ExternalStoreHandler externalStoreHandler = new ExternalStoreHandler(externalStoreService,authService);
                            response = externalStoreHandler.handle(request);
                            break;
                        case "EXIT":
                            isRunning = false;
                            response.addProperty("status", "OK");
                            response.addProperty("message", "Connection closing. Goodbye!");
                            break;

                        default:
                            response.addProperty("status", "ERR");
                            response.addProperty("code", "400");
                            response.addProperty("message", "Bad Request");
                            System.out.println("No handler available for this request");
                            break;
                    }

                    System.out.println("Output Response is: " + response.toString());
                    dos.writeUTF(response.toString());

                }
                dis.close();
                dos.close();
                conSecure.close();
                conGlobal.close();
                conNorth.close();
                conSouth.close();
                //con.close();
                this.client.close();



            }
            catch(IOException ioe){
                System.out.println("Error Occured: " + ioe);
            }
            catch(Exception e){
                System.out.println("Unknown Error: " + e);
            }

        }
    }
