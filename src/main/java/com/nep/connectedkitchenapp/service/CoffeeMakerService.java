package com.nep.connectedkitchenapp.service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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
	
	/*@Autowired
    private CoffeeMakerRepository coffeeMakerRepository;
	
	@Autowired
    private SimpMessagingTemplate messagingTemplate;

    public CoffeeMaker startBrewing() {
        CoffeeMaker coffeeMaker = new CoffeeMaker();
        coffeeMaker.setState("ON");
        coffeeMaker.setTemperature(90);
        coffeeMaker.setBrewTime(5);
        
        //return coffeeMakerRepository.save(coffeeMaker);
        messagingTemplate.convertAndSend("/topic/coffeeMaker", coffeeMaker);
        //return coffeeMaker;
        return coffeeMakerRepository.save(coffeeMaker);
    }

    public void stopBrewing(Long id) {
        CoffeeMaker coffeeMaker = coffeeMakerRepository.findById(id).orElseThrow();
        coffeeMaker.setState("OFF");
        
        coffeeMakerRepository.save(coffeeMaker);
        messagingTemplate.convertAndSend("/topic/coffeeMaker", coffeeMaker);
    }
    
    public CoffeeMaker getCoffeeMakerState() {
        return coffeeMakerRepository.findLatestCoffeeMaker(); // Implement this query in your repository
    } */
	
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
    private SimpMessagingTemplate messagingTemplate;
	
	private String mode;
	
    private ScheduledExecutorService scheduler;
    
    private CoffeeMaker currentCoffeeMaker;
    
    @Autowired
    public CoffeeMakerService(CoffeeMakerRepository coffeeMakerRepository, SimpMessagingTemplate messagingTemplate) {
        this.coffeeMakerRepository = coffeeMakerRepository;
        this.messagingTemplate = messagingTemplate;
        this.scheduler = Executors.newScheduledThreadPool(1); // Initialize the scheduler
    }

    public CoffeeMaker startBrewing(String brewStrength) {
    	// Retrieve the latest coffee maker state
    	currentCoffeeMaker = coffeeMakerRepository.findLatestCoffeeMaker();
    	
    	// Check if the microwave is on before starting the coffee maker
    	if (mixerService.isMixerOn() || microwaveService.isMicrowaveOn() || riceCookerService.isRiceCookerOn()) {
            throw new ApplianceConflictException("Another appliance is currently on. Please turn it off before starting the coffeemaker.");
        }
        
        // Check if the coffee maker is already running
        if (currentCoffeeMaker != null && "ON".equals(currentCoffeeMaker.getState())) {
        	throw new ApplianceConflictException("Coffee Maker is already running.");
        }
        
        // Initialize and start coffee maker if it is OFF
        if (currentCoffeeMaker == null) {
        	currentCoffeeMaker = new CoffeeMaker();
        }
        
        // Start coffee maker if microwave is off
        currentCoffeeMaker.setState("ON");
        currentCoffeeMaker.setTemperature(90);
        currentCoffeeMaker.setBrewStrength(brewStrength);
        
        int brewTime = getBrewTime();
        //currentCoffeeMaker.setBrewTime(5);
        
        currentCoffeeMaker.setBrewTime(brewTime);
        currentCoffeeMaker.setRemainingTime(brewTime);
        
        
        // Notify return updated coffee maker status
        notifyAppliances("Coffee Maker is now brewing.");
        
        if (scheduler == null) {
            scheduler = Executors.newScheduledThreadPool(1);
        }
        
	    scheduler.schedule(() -> stopBrewing(currentCoffeeMaker.getId()), brewTime, TimeUnit.SECONDS);
        
        // Schedule to start rice cooker after brewing time
        scheduler.scheduleAtFixedRate(this::updateBrewingTime, 0, 1, TimeUnit.SECONDS);
        
        messagingTemplate.convertAndSend("/topic/coffeeMaker", currentCoffeeMaker);
        return coffeeMakerRepository.save(currentCoffeeMaker);
    }
    
    private void updateBrewingTime() {
        int remainingTime = currentCoffeeMaker.getRemainingTime();

        if (remainingTime > 0) {
            remainingTime--; // Decrement the remaining time
            currentCoffeeMaker.setRemainingTime(remainingTime);
            coffeeMakerRepository.save(currentCoffeeMaker);

            // Send the updated brewing time to the UI
            int minutes = remainingTime / 60;
            int seconds = remainingTime % 60;
            String timeFormatted = String.format("%02d:%02d", minutes, seconds);
            
			System.out.println("Coffee Brewing Timer: " + timeFormatted);

            messagingTemplate.convertAndSend("/topic/coffeeBrewingTimer", timeFormatted);
        } else {
        	System.out.println("Coffee brewing has finished. Ricecooker will start brewing in 10 seconds.");
            notifyAppliances("Coffee brewing has finished. Ricecooker will start brewing in 10 seconds.");
            
            String mode = "Steam";
			
			scheduler = Executors.newScheduledThreadPool(1); // Re-initialize scheduler
	        scheduler.schedule(() -> riceCookerService.startCooking(mode), 10, TimeUnit.SECONDS);
			
	        // Shut down the scheduler
            if (scheduler != null && !scheduler.isShutdown()) {
                scheduler.shutdown();
                stopBrewing(currentCoffeeMaker.getId());                
            } 
        }
    }

	public void stopBrewing(Long id) {
        CoffeeMaker coffeeMaker = coffeeMakerRepository.findById(id).orElseThrow();
        
        if (!coffeeMaker.getState().equals("ON")) {
            throw new IllegalStateException("Coffee Maker is already OFF.");
        }
        
        coffeeMaker.setState("OFF");
        coffeeMakerRepository.save(coffeeMaker);
        messagingTemplate.convertAndSend("/topic/coffeeMaker", coffeeMaker);
      
        notifyAppliances("Coffee Maker has stopped brewing.");
        
    }
	
	private int getBrewTime() {
        return 10; // Fixed brewing time of 60 seconds
    }
    
    public CoffeeMaker getCoffeeMakerState() {
        return coffeeMakerRepository.findLatestCoffeeMaker(); // Implement this query in your repository
    }
    
    private void notifyAppliances(String message) {
        messagingTemplate.convertAndSend("/topic/applianceStatus", message);
    }

	public boolean isCoffeeMakerOn() {
		// Retrieve the latest microwave record and check its state
		CoffeeMaker coffeeMaker = coffeeMakerRepository.findLatestCoffeeMaker(); // Implement this in MicrowaveRepository
		return coffeeMaker != null && "ON".equals(coffeeMaker.getState());
	}
	
	
	
}
