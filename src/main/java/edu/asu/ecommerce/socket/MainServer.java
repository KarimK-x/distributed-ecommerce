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
                UserService userService = new UserService(conSecure, conNorth, conSouth); //Sayebha using centralized db for now
                //Add other services here, using the connection they need.

                
                while(isRunning){
                    String jsonMessage = dis.readUTF();
                    System.out.println("Request Received: " + jsonMessage);
                    JsonObject request = JsonParser.parseString(jsonMessage).getAsJsonObject();
                    String action = request.get("action").getAsString();

                    JsonObject response = new JsonObject();

                    switch(action){
                        case "REGISTER":
                            RegisterHandler regHandler = new RegisterHandler(userService);
                            response = regHandler.handle(request);
                            break;
                        case "LOGIN":
                            LoginHandler logHandler = new LoginHandler(userService, request);
                            response = logHandler.handle();
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
