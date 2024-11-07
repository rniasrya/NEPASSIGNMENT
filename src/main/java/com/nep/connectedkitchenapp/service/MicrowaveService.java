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
import com.nep.connectedkitchenapp.exception.ApplianceConflictException;
import com.nep.connectedkitchenapp.respository.MicrowaveRepository;

@Service
public class MicrowaveService {
	
	private Microwave currentMicrowave;
	private ApplianceSocketServer socketServer;
	private int simulatedTemperature = 25;
	
	private final MicrowaveRepository microwaveRepository;
	
    @Lazy
    @Autowired
    private CoffeeMakerService coffeeMakerService;
    
    @Lazy
    @Autowired
    private MixerService mixerService;

    @Autowired
    private RiceCookerService riceCookerService;
    
    private String brewStrength;
    
    private final SimpMessagingTemplate messagingTemplate; // Message template for WebSocket communication
    
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);; // Scheduler for timed tasks
    private ScheduledFuture<?> heatingTask; // Stores the heating task for cancellation or modification
    private ScheduledFuture<?> sensorTask;
    private ScheduledExecutorService sensorScheduler = Executors.newScheduledThreadPool(1);
    
    @Autowired
    public MicrowaveService(MicrowaveRepository microwaveRepository, SimpMessagingTemplate messagingTemplate, CoffeeMakerService coffeeMakerService) {
    	this.socketServer = new ApplianceSocketServer(); // Initialize socket server
    	this.microwaveRepository = microwaveRepository;
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

    // Starts the heating process
	public Microwave startHeating(int temperature, int timer) {
		
    	// Retrieve the latest microwave state
		currentMicrowave = microwaveRepository.findLatestMicrowave();
		
		// Check if the microwave is already running
		if (currentMicrowave != null && "ON".equals(currentMicrowave.getState())) {
			throw new ApplianceConflictException("Microwave is already ON.");
		}
		
		// Check if other appliances are running
		if (coffeeMakerService.isCoffeeMakerOn() || mixerService.isMixerOn() || riceCookerService.isRiceCookerOn()) {
            throw new ApplianceConflictException("Another appliance is currently on. Please turn it off before starting the microwave.");
        }
		
    	// Initialize microwave if it’s new or has been reset
		if (currentMicrowave  == null) {
			currentMicrowave  = new Microwave();
		}
		
		// Set up unique session, usage count, and state
		currentMicrowave.setUsageCount(currentMicrowave.getUsageCount() + 1); 
        String sessionId = UUID.randomUUID().toString();
        currentMicrowave.setSessionId(sessionId);
        
        // Send usage updates
        messagingTemplate.convertAndSend("/topic/microwaveUsage", currentMicrowave.getUsageCount());
		
        // Set up heating properties and notify
		currentMicrowave.setState("ON");
		currentMicrowave.setTemperature(temperature);
		currentMicrowave.setTimer(timer);
		currentMicrowave.setRemainingTime(timer * 60);
		
		notifyAppliances("Microwave is now running.");
		notifyAppliances("Microwave current status:" + 
				"\n\nID: " + currentMicrowave.getId() + 
				"\n" + "State: " + currentMicrowave.getState() + 
				"\nTemperature: " + currentMicrowave.getTemperature() + 
				"°C" + "\nDuration: " + currentMicrowave.getTimer()  + " mins" +
				"\nUsage Count: " + currentMicrowave.getUsageCount());
		
		sendSocketMessage("Microwave start");
		sendSocketMessage("Microwave session ID: " + sessionId);
		sendSocketMessage("Microwave is now running at " + currentMicrowave.getTemperature() + "°C for " + currentMicrowave.getTimer() + " minutes.");
		sendSocketMessage("Microwave Usage Count: " + currentMicrowave.getUsageCount());

		// Microwave state in the UI
		messagingTemplate.convertAndSend("/topic/microwave", currentMicrowave);
		
        scheduler = Executors.newScheduledThreadPool(1);
			    
		if (heatingTask != null && !heatingTask.isDone()) {
            heatingTask.cancel(false);
        }
		
		// Schedule the heating to stop automatically after set time
	    heatingTask = scheduler.schedule(() -> stopHeatingforTimer(), timer, TimeUnit.MINUTES);

	    // Schedule regular updates for remaining heating time
	    scheduler.scheduleAtFixedRate(this::updateRemainingTime, 0, 1, TimeUnit.SECONDS);
        
	    // Start the temperature sensor simulation
        startMicrowaveTemperatureSensor();
	    
	    // Save state in repository
        return microwaveRepository.save(currentMicrowave);
	}
	
	// Simulates a temperature sensor that periodically updates and checks for overheating
    private void startMicrowaveTemperatureSensor() {
    	sensorTask = sensorScheduler.scheduleAtFixedRate(() -> {
    		// Gradually increase temperature towards target temperature with a heating rate
            if (simulatedTemperature < currentMicrowave.getTemperature()) {
                // Simulate the microwave heating up
                simulatedTemperature += Math.min(2, currentMicrowave.getTemperature() - simulatedTemperature); // Heating rate
            } else if (simulatedTemperature > currentMicrowave.getTemperature()) {
                // If overshot, bring it down slightly 
                simulatedTemperature -= Math.min(1, simulatedTemperature - currentMicrowave.getTemperature());
            }
    		
            // Randomly increase or decrease temperature 
            simulatedTemperature += (Math.random() > 0.5 ? 1 : -1);

            // Send the temperature data to WebSocket clients
            messagingTemplate.convertAndSend("/topic/microwaveTemperature", simulatedTemperature);
            currentMicrowave.setTemperature(simulatedTemperature);
            
            // Check for overheating condition
            if (simulatedTemperature > currentMicrowave.getTemperature() + 20) { // Threshold set to 120°C
                stopHeating(currentMicrowave.getId());
                sendSocketMessage("Microwave overheated and stopped.");
                notifyAppliances("Microwave stopped due to overheating.");
            }
        }, 0, 1, TimeUnit.SECONDS);
    }
    
    // Call this method to stop the sensor
    private void stopMicrowaveTemperatureSensor() {
        if (sensorTask != null && !sensorTask.isDone()) {
            sensorTask.cancel(false); 
        }
    }

	private void updateRemainingTime() {
		int remainingTime = currentMicrowave.getRemainingTime();
		
		if (remainingTime > 0) {
			remainingTime--; 
			currentMicrowave.setRemainingTime(remainingTime);
			
			int minutes = remainingTime / 60;
			int seconds = remainingTime % 60;
			String timeFormatted = String.format("%02d:%02d", minutes, seconds);
			
			sendSocketMessage("Microwave Timer: " + timeFormatted);
			
			messagingTemplate.convertAndSend("/topic/microwaveTimer", timeFormatted);
		} else {
            stopHeatingforTimer();                
		}
	}
	
	public void stopHeatingforTimer() {
		
		if (currentMicrowave == null) {
            return;
        }
		
		stopMicrowaveTemperatureSensor();
		
		currentMicrowave.setState("OFF");
		microwaveRepository.save(currentMicrowave);
		messagingTemplate.convertAndSend("/topic/microwaveTemperature", "0");
        messagingTemplate.convertAndSend("/topic/microwave", currentMicrowave);

        // Cancel the scheduled heating task
        if (heatingTask != null && !heatingTask.isDone()) {
        	heatingTask.cancel(false);
        }

        // Notify appliances that microwave has finished
        notifyAppliances("Microwave has finished heating. Coffee Maker will start brewing in 10 seconds.");
        sendSocketMessage("Microwave has finished heating. Coffee Maker will start brewing in 10 seconds.");

		String brewStrength = "Medium";
		
        // Schedule the coffee maker to start after 10 seconds
        if (scheduler != null && !scheduler.isShutdown()) {
        	scheduler.schedule(() -> coffeeMakerService.startBrewing(brewStrength), 10, TimeUnit.SECONDS);
        }
        
        // Shutdown the scheduler
        scheduler.shutdown();
	}
	
	public void stopHeating(Long id) {
		Microwave microwave = microwaveRepository.findById(id).orElseThrow();
		
		if (!microwave.getState().equals("ON")) {
			throw new IllegalStateException("Microwave is already OFF.");
		}
				
		microwave.setState("OFF");
		currentMicrowave.setRemainingTime(0);
		microwaveRepository.save(microwave);
		messagingTemplate.convertAndSend("/topic/microwave", microwave);
		messagingTemplate.convertAndSend("/topic/microwaveTemperature", "0°C");
		messagingTemplate.convertAndSend("/topic/microwaveTimer", "00:00");
		
		sendSocketMessage("Microwave has stopped.");
	    notifyAppliances("Microwave has stopped.");
		
		if (heatingTask != null && !heatingTask.isDone()) {
			heatingTask.cancel(false);
	    }
		
		stopMicrowaveTemperatureSensor();
	    scheduler.shutdown();		
	}
	
    
    public int getRemainingTime(Long id) {
        Microwave microwave = microwaveRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Microwave not found"));
        return microwave.getRemainingTime();
    }

    // Gets the latest microwave state
	public Microwave getMicrowaveState() {
		return microwaveRepository.findLatestMicrowave();
	}

	// Sends a message to notify all appliances
	private void notifyAppliances(String message) {
        messagingTemplate.convertAndSend("/topic/applianceStatus", message);
    }
	
	// Checks if microwave is currently heating
	public boolean isMicrowaveOn() {
		// Retrieve the latest microwave record and check its state
		Microwave microwave = microwaveRepository.findLatestMicrowave();
		return microwave != null && "ON".equals(microwave.getState());
	}
}