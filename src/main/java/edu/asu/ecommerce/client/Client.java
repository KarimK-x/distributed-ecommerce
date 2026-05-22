/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package edu.asu.ecommerce.client;

import com.google.gson.JsonObject;
import java.net.*;
import java.io.*;

/**
 *
 * @author Dell
 */
// When main app is written, every new request should 
// create a Socket Client using this class

public class Client {
    
    private Socket client;
    private DataInputStream is;
    private DataOutputStream os;

    public Client(Socket c) throws IOException {
        client = c;
        is = new DataInputStream(client.getInputStream());
        os = new DataOutputStream(client.getOutputStream());
    }

    public void sendRequest(JsonObject request) throws IOException {
        this.os.writeUTF(request.toString());
    }
    public String receiveResponse() throws IOException {
        return this.is.readUTF();
    }
    
}
