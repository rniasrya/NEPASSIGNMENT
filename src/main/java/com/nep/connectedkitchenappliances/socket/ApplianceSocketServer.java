package com.nep.connectedkitchenappliances.socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class ApplianceSocketServer {
	private int port;
    
    public ApplianceSocketServer(int port) {
        this.port = port;
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Socket server is listening on port " + port);
            while (true) {
                Socket socket = serverSocket.accept();
                new ApplianceHandler(socket).start(); // Handle each socket connection in a new thread
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
// ==========================
    
    private class ApplianceHandler extends Thread {
        private Socket socket;

        public ApplianceHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 PrintWriter output = new PrintWriter(socket.getOutputStream(), true)) {

                String command;
                while ((command = input.readLine()) != null) {
                    // Process the command and respond
                    String response = processCommand(command);
                    output.println(response);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private String processCommand(String command) {
            // Logic to handle commands from appliances
            switch (command) {
                case "COFFEE_MAKER_START":
                    // Start the coffee maker
                    return "Coffee maker started.";
                case "COFFEE_MAKER_STOP":
                    // Stop the coffee maker
                    return "Coffee maker stopped.";
                // Add cases for other appliances as needed
                default:
                    return "Unknown command.";
            }
        }
    }
}
