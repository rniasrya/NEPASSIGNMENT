package com.nep.connectedkitchenapp.service;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.nep.connectedkitchenapp.appliances.CoffeeMaker;
import com.nep.connectedkitchenapp.appliances.Microwave;
import com.nep.connectedkitchenapp.appliances.RiceCooker;
import com.nep.connectedkitchenapp.exception.ApplianceConflictException;
import com.nep.connectedkitchenapp.respository.CoffeeMakerRepository;
import com.nep.connectedkitchenapp.respository.RiceCookerRepository;

@Service
public class RiceCookerService {
	
	private RiceCooker currentRiceCooker;
	private ApplianceSocketServer socketServer;
	private int simulatedTemperature = 20;
	private int targetTemperature = 0;

	@Autowired
    private RiceCookerRepository riceCookerRepository;
	
	@Lazy
	@Autowired
    private CoffeeMakerService coffeeMakerService;

	@Lazy
    @Autowired
    private MicrowaveService microwaveService;

    @Lazy
    @Autowired
    private MixerService mixerService;
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate; // Message template for WebSocket communication
    
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);; // Scheduler for timed tasks
    private ScheduledFuture<?> cookingTask; // Stores the cooking task for cancellation or modification
    private ScheduledFuture<?> sensorTask;
    private ScheduledExecutorService sensorScheduler = Executors.newScheduledThreadPool(1);
    
	@Autowired
    public RiceCookerService(RiceCookerRepository riceCookerRepository, SimpMessagingTemplate messagingTemplate) {
		this.socketServer = new ApplianceSocketServer(); // Initialize socket server
		this.riceCookerRepository = riceCookerRepository;
        this.messagingTemplate = messagingTemplate;
    }
	
    // Sends a message via socket communication
	private void sendSocketMessage(String message) {
        try (Socket socket = new Socket("localhost", 5000);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
             
            out.println(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

	// Starts the cooking process
    public RiceCooker startCooking(String mode) {
    	
    	// Get the latest state of the rice cooker
    	currentRiceCooker = riceCookerRepository.findLatestriceCooker();
    	
    	// Check if rice cooker is already running
    	if (currentRiceCooker != null && "ON".equals(currentRiceCooker.getState())) {
    		throw new ApplianceConflictException("Ricecooker is already running.");
    	}
    	
    	// Check if any other appliance is currently on
        if (coffeeMakerService.isCoffeeMakerOn() || microwaveService.isMicrowaveOn() || mixerService.isMixerOn()) {
            throw new ApplianceConflictException("Another appliance is currently on. Please turn it off before starting the rice cooker.");
        }
        
        // Initialize rice cooker if it's new or or has been reset yet 
        if (currentRiceCooker == null) {
        	currentRiceCooker = new RiceCooker();
        }
        
        // Set up unique session, usage count, and state
        currentRiceCooker.setUsageCount(currentRiceCooker.getUsageCount() + 1); 
        String sessionId = UUID.randomUUID().toString(); 
        currentRiceCooker.setSessionId(sessionId);
        
        // Send usage updates
        messagingTemplate.convertAndSend("/topic/riceCookerUsage", currentRiceCooker.getUsageCount());  
        
        // Set up mixing properties and notify
        currentRiceCooker.setState("ON");
        currentRiceCooker.setMode(mode);
        
        // Set the target temperature based on the mode
        switch (currentRiceCooker.getMode()) {
            case "Rice":
                targetTemperature = 90;  // Target temperature for Rice mode
                break;
            case "Steam":
            	targetTemperature = 95;  // Target temperature for Steam mode
                break;
            case "Warm":
            	targetTemperature = 60;  // Target temperature for Warm mode
                break;
        }
        
        int cookingTime = getCookingTime();
        currentRiceCooker.setCookingTime(cookingTime);
        currentRiceCooker.setRemainingTime(cookingTime);
                
        notifyAppliances("Ricecooker is now running.");
        notifyAppliances("Ricecooker current status: " + 
        		"\n\nID: " + currentRiceCooker.getId() + 
        		"\n" + "State: " + currentRiceCooker.getState() + 
        		"\nMode: " + currentRiceCooker.getMode() +
        		"\nTemperature: " + targetTemperature + "°C" +
        		"\nUsage Count: " + currentRiceCooker.getUsageCount());
        
        sendSocketMessage("Ricecooker has started.");
        sendSocketMessage("Ricecooker session ID: " + sessionId);
		sendSocketMessage("Ricecooker is set as " + currentRiceCooker.getMode() + " mode and is running for " + currentRiceCooker.getCookingTime() + " seconds.");
		sendSocketMessage("Ricecooker current temperature: " + targetTemperature + "°C");
		sendSocketMessage("Ricecooker Usage Count: " + currentRiceCooker.getUsageCount());
    
		// Rice cooker state in the UI
		messagingTemplate.convertAndSend("/topic/riceCooker", currentRiceCooker);
	    
		// Initialize the scheduler here
        scheduler = Executors.newScheduledThreadPool(1);
		
		if (cookingTask != null && !cookingTask.isDone()) {
            cookingTask.cancel(false);
        }
	    
		// Schedule the cooking to stop automatically after set time
        cookingTask = scheduler.schedule(() -> stopCookingforTimer(), cookingTime, TimeUnit.SECONDS);
        
	    // Schedule regular updates for remaining mixing time
	    scheduler.scheduleAtFixedRate(this::updateCookingTime, 0, 1, TimeUnit.SECONDS);
        
	    startRiceCookerTemperatureSensor();
	    
        // Save state in repository
        return riceCookerRepository.save(currentRiceCooker);
    }
    
    // Simulates a temperature sensor that periodically updates and checks for overheating
    private void startRiceCookerTemperatureSensor() {
            	
    	sensorTask = sensorScheduler.scheduleAtFixedRate(() -> {
    		// Gradually increase temperature to reach the target temperature
            if (simulatedTemperature < targetTemperature) {
                simulatedTemperature += 1 + Math.random(); // Adjust the rate as needed to simulate heating
            } else {
                simulatedTemperature += (Math.random() > 0.5 ? 1 : -1); // Small fluctuations around target
            }

            messagingTemplate.convertAndSend("/topic/riceCookerTemperature", simulatedTemperature);
            currentRiceCooker.setTemperature(simulatedTemperature);

            // Check for overheating condition
            if (simulatedTemperature > targetTemperature + 20) { // Threshold set to 100°C
            	stopCooking(currentRiceCooker.getId());
                sendSocketMessage("Rice Cooker overheated and stopped.");
                notifyAppliances("Rice Cooker is overheating.");
            }
        }, 0, 1, TimeUnit.SECONDS);
    }
    
    // To stop the sensor
    private void stopRiceCookerTemperatureSensor() {
        if (sensorTask != null && !sensorTask.isDone()) {
            sensorTask.cancel(false);       
        }
    }

	private void updateCookingTime() {
        int remainingTime = currentRiceCooker.getRemainingTime();

        if (remainingTime > 0) {
            remainingTime--; 
            currentRiceCooker.setRemainingTime(remainingTime);

            int minutes = remainingTime / 60;
            int seconds = remainingTime % 60;
            String timeFormatted = String.format("%02d:%02d", minutes, seconds);
          
            sendSocketMessage("Ricecooker Timer: " + timeFormatted);
            messagingTemplate.convertAndSend("/topic/riceCookerTimer", timeFormatted);
        } else {
            stopCookingforTimer();                   
        } 
    }
	
	public void stopCookingforTimer() {
		
		if (currentRiceCooker == null) {
            return;
        }
		
		stopRiceCookerTemperatureSensor();
		
		currentRiceCooker.setState("OFF");
        riceCookerRepository.save(currentRiceCooker);
        messagingTemplate.convertAndSend("/topic/riceCookerTemperature", "0");
        messagingTemplate.convertAndSend("/topic/riceCooker", currentRiceCooker);

        // Cancel the scheduled cooking task
        if (cookingTask != null && !cookingTask.isDone()) {
            cookingTask.cancel(false);
        }

        // Notify appliances that cooking has finished
        sendSocketMessage("Ricecooker has finished. Mixer will start in 10 seconds.");
        notifyAppliances("Ricecooker has finished. Mixer will start in 10 seconds.");

        // Schedule the mixer to start after 10 seconds
        int speed = 2;
        
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.schedule(() -> mixerService.startMixing(speed), 10, TimeUnit.SECONDS);
        }
        
        // Shutdown the scheduler
        scheduler.shutdown();       
    }

    public void stopCooking(Long id) {
    	currentRiceCooker = riceCookerRepository.findById(id).orElseThrow();
        
        if (!currentRiceCooker.getState().equals("ON")) {
            throw new IllegalStateException("Rice Cooker is already OFF.");
        }
                
        currentRiceCooker.setState("OFF");
        currentRiceCooker.setRemainingTime(0);
        riceCookerRepository.save(currentRiceCooker);
        messagingTemplate.convertAndSend("/topic/riceCookerTemperature", "0");
        messagingTemplate.convertAndSend("/topic/riceCooker", currentRiceCooker);
        messagingTemplate.convertAndSend("/topic/riceCookerTimer", "00:00");
        
        sendSocketMessage("Ricecooker has stopped.");
        notifyAppliances("Ricecooker has stopped.");
        
        if (cookingTask != null && !cookingTask.isDone()) {
            cookingTask.cancel(false);
        }

        stopRiceCookerTemperatureSensor();
        
        // Shutdown the scheduler to stop all scheduled tasks
        scheduler.shutdownNow();
               
    }

    private int getCookingTime() {
    	return 15;
    }
    
    // Gets the latest rice cooker state
	public RiceCooker getriceCookerState() {
		return riceCookerRepository.findLatestriceCooker();
	}
	
	// Sends a message to notify all appliances
	private void notifyAppliances(String message) {
        messagingTemplate.convertAndSend("/topic/applianceStatus", message);
    }
	
	// Checks if rice cooker is currently cooking
	public boolean isRiceCookerOn() {
		currentRiceCooker = riceCookerRepository.findLatestriceCooker(); 
		return currentRiceCooker != null && currentRiceCooker.getState().equals("ON");
	}
}