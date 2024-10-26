package com.nep.connectedkitchenappliances.socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ApplianceSocketClient {
	private String serverAddress;
    private int port;

    public ApplianceSocketClient(String serverAddress, int port) {
        this.serverAddress = serverAddress;
        this.port = port;
    }

    // Method to send command to the socket server
    public String sendCommand(String command) {
        try (Socket socket = new Socket(serverAddress, port);
             PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            output.println(command); // Send command to the server
            return input.readLine(); // Read response from the server
        } catch (IOException e) {
            e.printStackTrace();
            return "Error communicating with the appliance.";
        }
    }
}
