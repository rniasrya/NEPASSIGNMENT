package com.nep.connectedkitchenapp.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class ApplianceSocketServer {
	private static final int PORT = 5000;  // Port to listen on
    private ServerSocket serverSocket;

    public ApplianceSocketServer() {
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Socket Server started on port " + PORT);

            // Start a new thread to handle connections
            new Thread(() -> listenForClients()).start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void listenForClients() {
        while (true) {
            try (Socket clientSocket = serverSocket.accept();
                 BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                 PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

                String message = in.readLine();
                System.out.println("Received from client: " + message);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void stopServer() {
        try {
            serverSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        new ApplianceSocketServer();
    }
}
