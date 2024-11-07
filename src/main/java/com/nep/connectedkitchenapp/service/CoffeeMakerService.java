package com.nep.connectedkitchenapp.service;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.nep.connectedkitchenapp.appliances.CoffeeMaker;
import com.nep.connectedkitchenapp.appliances.Microwave;
import com.nep.connectedkitchenapp.exception.ApplianceConflictException;
import com.nep.connectedkitchenapp.respository.CoffeeMakerRepository;

@Service
public class CoffeeMakerService {
	
	private CoffeeMaker coffeeMaker = new CoffeeMaker();
	private CoffeeMaker currentCoffeeMaker;
	private ApplianceSocketServer socketServer;
	private final Random random = new Random();
	
	@Autowired
    private CoffeeMakerRepository coffeeMakerRepository;

	@Lazy
	@Autowired
    private MicrowaveService microwaveService;
	
	@Lazy
	@Autowired
    private MixerService mixerService;
	
	@Lazy
	@Autowired
	private RiceCookerService riceCookerService;
	
	@Autowired
    private SimpMessagingTemplate messagingTemplate; // Messaging template for WebSocket communication
		
	private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);; // Scheduler for timed tasks
    private ScheduledFuture<?> brewingTask; // Stores the brewing task for cancellation or modification 
    private final AtomicBoolean brewingTaskRunning = new AtomicBoolean(false);
    
    @Autowired
    public CoffeeMakerService(CoffeeMakerRepository coffeeMakerRepository, SimpMessagingTemplate messagingTemplate) {
    	this.socketServer = new ApplianceSocketServer(); // Initialize socket server
    	this.coffeeMakerRepository = coffeeMakerRepository;
        this.messagingTemplate = messagingTemplate;
    }
    
    // Sends a message via socket communication
    private void sendSocketMessage(String message) {
	    try (Socket socket = new Socket("localhost", 5000);
	         PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

	        out.println(message);
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
    
    // Starts the brewing process
    public CoffeeMaker startBrewing(String brewStrength) {
        
    	// Get the latest state of the coffee maker
    	currentCoffeeMaker = coffeeMakerRepository.findLatestCoffeeMaker();
    	
    	// Check if coffee maker is already brewing
    	if (currentCoffeeMaker != null && "ON".equals(currentCoffeeMaker.getState())) {
    		throw new ApplianceConflictException("Coffee Maker is already running.");
    	}
    	
    	// Ensure no other appliances are running
    	if (mixerService.isMixerOn() || microwaveService.isMicrowaveOn() || riceCookerService.isRiceCookerOn()) {
            throw new ApplianceConflictException("Another appliance is currently on. Please turn it off before starting the coffeemaker.");
        }
        
    	// Initialize coffee maker if it’s new or has been reset
        if (currentCoffeeMaker == null) {
        	currentCoffeeMaker = new CoffeeMaker();
        	currentCoffeeMaker.setWaterLevel(100);
        	currentCoffeeMaker.setCoffeeGroundsLevel(100);
        	currentCoffeeMaker.setUsageCount(0);
        }
        
     // Check if there is enough water
    	if (currentCoffeeMaker.getWaterLevel() > 0 && currentCoffeeMaker.getCoffeeGroundsLevel() > 0) {
        	// Decrease water by 10 units per start button 
        	currentCoffeeMaker.setWaterLevel(currentCoffeeMaker.getWaterLevel() - 20);         	
        	messagingTemplate.convertAndSend("/topic/coffeeMakerWaterResource", currentCoffeeMaker.getWaterLevel());
        } else {
        	// Notify if resources are empty
        	messagingTemplate.convertAndSend("/topic/coffeeMakerWaterResource", "Empty");
        	throw new ApplianceConflictException("Not enough water, please refill.");
        }
        
        // Check if there are enough coffee grounds
        if (currentCoffeeMaker.getCoffeeGroundsLevel() > 0 && currentCoffeeMaker.getCoffeeGroundsLevel() > 0) {
        	// Decrease coffee grounds level by 5 units per start button 
        	currentCoffeeMaker.setCoffeeGroundsLevel(currentCoffeeMaker.getCoffeeGroundsLevel() - 5);
        	messagingTemplate.convertAndSend("/topic/coffeeMakerCGResource", currentCoffeeMaker.getCoffeeGroundsLevel());
        } else {
        	messagingTemplate.convertAndSend("/topic/coffeeMakerCGResource", "Empty");
        	throw new ApplianceConflictException("Not enough coffee grounds, please refill.");
        }
        
        // Set up unique session, usage count, and state
        currentCoffeeMaker.setUsageCount(currentCoffeeMaker.getUsageCount() + 1); 
        String sessionId = UUID.randomUUID().toString(); 
        currentCoffeeMaker.setSessionId(sessionId);
        
        // Send usage and resource updates
        messagingTemplate.convertAndSend("/topic/coffeeMakerUsage", currentCoffeeMaker.getUsageCount());
                
        // Set up brewing properties and notify
        currentCoffeeMaker.setState("ON");
        currentCoffeeMaker.setTemperature(90);
        currentCoffeeMaker.setBrewStrength(brewStrength);
        int brewTime = getBrewTime();        
        currentCoffeeMaker.setBrewTime(brewTime);
        currentCoffeeMaker.setRemainingTime(brewTime);
        
        notifyAppliances("Coffee Maker is now brewing.");

        notifyAppliances("Coffee Maker current status:" + 
        					"\n\nID: " + currentCoffeeMaker.getId() + 
        					"\n" + "State: " + currentCoffeeMaker.getState() + 
        					"\nBrew Strength: " + currentCoffeeMaker.getBrewStrength() + 
        					"\nTemperature: " + currentCoffeeMaker.getTemperature() + "°C" + 
        					"\nBrew Time: " + currentCoffeeMaker.getBrewTime() + " seconds" +
        					"\nUsage Count: " + currentCoffeeMaker.getUsageCount());
        
        sendSocketMessage("Coffee Maker has started.");
        sendSocketMessage("Coffee Maker session ID: " + sessionId);
        sendSocketMessage("Coffee Maker is set at a " + currentCoffeeMaker.getBrewStrength() + " level" + " and is brewing at " + currentCoffeeMaker.getTemperature() + "°C for " + brewTime + " seconds.");
        sendSocketMessage("Coffee Maker current water level: " + currentCoffeeMaker.getWaterLevel());
        sendSocketMessage("Coffee Maker current coffee ground level: " + currentCoffeeMaker.getCoffeeGroundsLevel());
		sendSocketMessage("Coffee Maker Usage Count: " + currentCoffeeMaker.getUsageCount());

        // Coffee maker state in the UI
        messagingTemplate.convertAndSend("/topic/coffeeMaker", currentCoffeeMaker);
        
        scheduler = Executors.newScheduledThreadPool(1);
        
        if (brewingTask != null && !brewingTask.isDone()) {
            brewingTask.cancel(false);
        }
                
        // Schedule the brewing to stop automatically after set time
	    brewingTask = scheduler.schedule(() -> stopBrewingforTimer(), brewTime, TimeUnit.SECONDS);
        
	    // Schedule regular updates for remaining brewing time
	    scheduler.scheduleAtFixedRate(this::updateBrewingTime, 0, 1, TimeUnit.SECONDS);
        
        // Save state in repository
        return coffeeMakerRepository.save(currentCoffeeMaker);
    }

	private void updateBrewingTime() {
    	int remainingTime = currentCoffeeMaker.getRemainingTime();

        if (remainingTime > 0) {
            remainingTime--;
            currentCoffeeMaker.setRemainingTime(remainingTime);
            
            int minutes = remainingTime / 60;
            int seconds = remainingTime % 60;
            String timeFormatted = String.format("%02d:%02d", minutes, seconds);
            
            sendSocketMessage("Coffee Brewing Timer: " + timeFormatted);
            messagingTemplate.convertAndSend("/topic/coffeeBrewingTimer", timeFormatted);
        } else {
            stopBrewingforTimer();
        }
    }
	
	public void stopBrewingforTimer() {
		
		if (currentCoffeeMaker == null) {
            return;
        }
		
		currentCoffeeMaker.setState("OFF");
		coffeeMakerRepository.save(currentCoffeeMaker);
        messagingTemplate.convertAndSend("/topic/coffeeMaker", currentCoffeeMaker);

        // Cancel the scheduled brewing task
        if (brewingTask != null && !brewingTask.isDone()) {
        	brewingTask.cancel(false);
        }

        // Notify appliances that brewing has finished
        notifyAppliances("Coffee brewing has finished. Rice cooker will start cooking in 10 seconds.");
        sendSocketMessage("Coffee brewing has finished. Rice cooker will start cooking in 10 seconds.");

        // Schedule the rice cooker to start after 10 seconds
        if (scheduler != null && !scheduler.isShutdown()) {
        	scheduler.schedule(() -> riceCookerService.startCooking("Steam"), 10, TimeUnit.SECONDS);
        }
        
        // Shutdown the scheduler
        scheduler.shutdown();
	}

	public void stopBrewing(Long id) {
		currentCoffeeMaker = coffeeMakerRepository.findLatestCoffeeMaker();
        
        if (!currentCoffeeMaker.getState().equals("ON")) {
            throw new IllegalStateException("Coffee Maker is already OFF.");
        }
        
        // Set state to OFF and reset the remaining time to 0
        currentCoffeeMaker.setState("OFF");
        currentCoffeeMaker.setRemainingTime(0);
        messagingTemplate.convertAndSend("/topic/coffeeMaker", currentCoffeeMaker);
        messagingTemplate.convertAndSend("/topic/coffeeBrewingTimer", "00:00");
      
        coffeeMakerRepository.save(currentCoffeeMaker);
        
        notifyAppliances("Coffee Maker has stopped."); 
        sendSocketMessage("Coffee Maker has stopped.");   
        
        // Cancel the brewing task if it exists
        if (brewingTask != null && !brewingTask.isDone()) {
        	brewingTask.cancel(false);
        }
        
        scheduler.shutdownNow();
        
    }
	
	private int getBrewTime() {
        return 15; // Fixed brewing time of 15 seconds
    }
    
	// Gets the latest coffee maker state
    public CoffeeMaker getCoffeeMakerState() {
        return coffeeMakerRepository.findLatestCoffeeMaker();
    }
    
    // Sends a message to notify all appliances
    private void notifyAppliances(String message) {
        messagingTemplate.convertAndSend("/topic/applianceStatus", message);
    }

    // Checks if coffee maker is currently brewing
	public boolean isCoffeeMakerOn() {
		// Retrieve the latest microwave record and check its state
		CoffeeMaker coffeeMaker = coffeeMakerRepository.findLatestCoffeeMaker(); 
		return coffeeMaker != null && "ON".equals(coffeeMaker.getState());
	}
	
	// Retrieves all previous state of the coffee maker 
	public List<CoffeeMaker> getAllCoffeeMakerStates() {
	    return coffeeMakerRepository.findAllCoffeeMakersOrderedById(); // Fetch all coffee makers in a specific order
	}
	
	// Refills coffee maker resources if not already full
	public CoffeeMaker refillWaterResource() {
    	currentCoffeeMaker = coffeeMakerRepository.findLatestCoffeeMaker();
        
    	// Simulate the water level sensor reading before refilling
        int currentWaterLevel = currentCoffeeMaker.getWaterLevel();
    	
        if (currentCoffeeMaker != null && "ON".equals(currentCoffeeMaker.getState())) {
    		throw new IllegalStateException("Coffee Maker is currently running.");
    	}

        if (currentWaterLevel == 100) {
            throw new IllegalStateException("Water level is already full.");
        }
        
        if (currentWaterLevel > 0) {
            throw new IllegalStateException("Water level are not empty. Cannot refill yet.");
        }
        
        // Simulate refilling the water to full
        currentCoffeeMaker.setWaterLevel(100);
        
        coffeeMakerRepository.save(currentCoffeeMaker);
        
        notifyAppliances("Refilling water...");
        sendSocketMessage("Refilling water...");
        
        try {
        	Thread.sleep(3000);
        } catch (InterruptedException e) {
        	e.printStackTrace();
        }
        
        // Notify the UI about the updated water level
        messagingTemplate.convertAndSend("/topic/coffeeMakerWaterResource", 100);
        
        notifyAppliances("Coffee Maker has been refilled.");
        sendSocketMessage("Coffee Maker has been refilled.");
        
        return coffeeMakerRepository.save(currentCoffeeMaker);
    }
	
	// Refilling coffee grounds logic can simulate sensor reading as well
    public CoffeeMaker refillCoffeeGroundsResource() {
        currentCoffeeMaker = coffeeMakerRepository.findLatestCoffeeMaker();
        
        if (currentCoffeeMaker != null && "ON".equals(currentCoffeeMaker.getState())) {
    		throw new IllegalStateException("Coffee Maker is currently running.");
    	}
        
        // Simulate the coffee grounds sensor before refilling
        int currentCoffeeGroundsLevel = currentCoffeeMaker.getCoffeeGroundsLevel();
        
        if (currentCoffeeGroundsLevel == 100) {
            throw new IllegalStateException("Coffee grounds are already full.");
        }
        
        if (currentCoffeeGroundsLevel > 0) {
            throw new IllegalStateException("Coffee grounds are not empty. Cannot refill yet.");
        }
        
        // Simulate refilling the coffee grounds to full
        currentCoffeeMaker.setCoffeeGroundsLevel(100);
        
        coffeeMakerRepository.save(currentCoffeeMaker);
        
        notifyAppliances("Refilling coffee grounds...");
        sendSocketMessage("Refilling coffee grounds...");
        
        try {
        	Thread.sleep(3000);
        } catch (InterruptedException e) {
        	e.printStackTrace();
        }
        
        // Notify the UI about the updated coffee grounds level
        messagingTemplate.convertAndSend("/topic/coffeeMakerCGResource", 100);
        
        notifyAppliances("Coffee Maker has been refilled.");
        sendSocketMessage("Coffee Maker has been refilled.");
        
        return coffeeMakerRepository.save(currentCoffeeMaker);
    }
	
}