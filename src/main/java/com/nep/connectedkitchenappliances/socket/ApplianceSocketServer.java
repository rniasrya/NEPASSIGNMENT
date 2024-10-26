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
            try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
                 
                String inputLine;
                // Read incoming messages
                while ((inputLine = in.readLine()) != null) {
                    // Process the message
                    System.out.println("Received: " + inputLine);
                    // Optionally send a response back to the client
                    out.println("Acknowledged: " + inputLine);
                }
            } catch (IOException e) {
                e.printStackTrace();  // Log the exception for debugging
            } finally {
                try {
                    socket.close();  // Ensure the socket is closed properly
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private String processCommand(String command) {
            String[] parts = command.split(":"); // Split command and parameters
            String applianceCommand = parts[0]; // Get the main command
            String response = "";

            switch (applianceCommand) {
                case "COFFEE_MAKER_START":
                    int brewTime = Integer.parseInt(parts[1]); // Get brew time
                    // Logic to start the coffee maker with brewTime
                    response = "Coffee maker started for " + brewTime + " minutes.";
                    break;
                case "COFFEE_MAKER_STOP":
                    // Logic to stop the coffee maker
                    response = "Coffee maker stopped.";
                    break;
                case "RICE_COOKER_START":
                    // Logic to start the rice cooker
                    response = "Rice cooker started.";
                    break;
                case "RICE_COOKER_STOP":
                    // Logic to stop the rice cooker
                    response = "Rice cooker stopped.";
                    break;
                case "MICROWAVE_START":
                    // Logic to start the microwave
                    response = "Microwave started.";
                    break;
                case "MICROWAVE_STOP":
                    // Logic to stop the microwave
                    response = "Microwave stopped.";
                    break;
                case "MIXER_START":
                    // Logic to start the mixer
                    response = "Mixer started.";
                    break;
                case "MIXER_STOP":
                    // Logic to stop the mixer
                    response = "Mixer stopped.";
                    break;
                default:
                    response = "Unknown command.";
                    break;
            }
            return response;
            
        }
    }
}
