package com.nep.connectedkitchenappliances.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nep.connectedkitchenappliances.socket.ApplianceSocketClient;

@RestController
@RequestMapping("/api/appliances") // Base URL for appliance endpoints
public class ApplianceController {

    private final ApplianceSocketClient socketClient;
    
    @GetMapping("/")
    public String home() {
        return "Connected Kitchen Appliances - Welcome!";
    }

    @Autowired
    public ApplianceController(ApplianceSocketClient socketClient) {
        this.socketClient = socketClient; // Inject the socket client
    }

    // Start Coffee Maker
    @PostMapping("/coffee-maker/start")
    public String startCoffeeMaker(@RequestParam int brewTime) {
        String command = "COFFEE_MAKER_START:" + brewTime; // Create command with brew time
        return socketClient.sendCommand(command); // Send command to the socket server
    }

    // Stop Coffee Maker
    @PostMapping("/coffee-maker/stop")
    public String stopCoffeeMaker() {
        String command = "COFFEE_MAKER_STOP"; // Command to stop brewing
        return socketClient.sendCommand(command);
    }

    // Start Rice Cooker
    @PostMapping("/rice-cooker/start")
    public String startRiceCooker() {
        String command = "RICE_COOKER_START"; // Command to start the rice cooker
        return socketClient.sendCommand(command);
    }

    // Stop Rice Cooker
    @PostMapping("/rice-cooker/stop")
    public String stopRiceCooker() {
        String command = "RICE_COOKER_STOP"; // Command to stop the rice cooker
        return socketClient.sendCommand(command);
    }

    // Start Microwave
    @PostMapping("/microwave/start")
    public String startMicrowave(@RequestParam int time) {
        String command = "MICROWAVE_START:" + time; // Create command with time
        return socketClient.sendCommand(command);
    }

    // Stop Microwave
    @PostMapping("/microwave/stop")
    public String stopMicrowave() {
        String command = "MICROWAVE_STOP"; // Command to stop the microwave
        return socketClient.sendCommand(command);
    }

    // Start Mixer
    @PostMapping("/mixer/start")
    public String startMixer() {
        String command = "MIXER_START"; // Command to start the mixer
        return socketClient.sendCommand(command);
    }

    // Stop Mixer
    @PostMapping("/mixer/stop")
    public String stopMixer() {
        String command = "MIXER_STOP"; // Command to stop the mixer
        return socketClient.sendCommand(command);
    }
}
