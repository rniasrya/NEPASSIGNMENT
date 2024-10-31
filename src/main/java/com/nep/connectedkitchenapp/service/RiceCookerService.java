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
import com.nep.connectedkitchenapp.appliances.RiceCooker;
import com.nep.connectedkitchenapp.exception.ApplianceConflictException;
import com.nep.connectedkitchenapp.respository.CoffeeMakerRepository;
import com.nep.connectedkitchenapp.respository.RiceCookerRepository;

@Service
public class RiceCookerService {
	
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
    
    private ScheduledExecutorService scheduler;
    
    private RiceCooker currentRiceCooker;
	
	@Autowired
    private SimpMessagingTemplate messagingTemplate;
	
	@Autowired
    public RiceCookerService(RiceCookerRepository riceCookerRepository, SimpMessagingTemplate messagingTemplate) {
        this.riceCookerRepository = riceCookerRepository;
        this.messagingTemplate = messagingTemplate;
        this.scheduler = Executors.newScheduledThreadPool(1); // Initialize the scheduler
    }

    public RiceCooker startCooking(String mode) {
    	
    	currentRiceCooker = riceCookerRepository.findLatestriceCooker();
    	
    	// Check if any other appliance is currently on
        if (coffeeMakerService.isCoffeeMakerOn() || microwaveService.isMicrowaveOn() || mixerService.isMixerOn()) {
            throw new ApplianceConflictException("Another appliance is currently on. Please turn it off before starting the rice cooker.");
        }
    	
        
        if (currentRiceCooker == null) {
        	currentRiceCooker = new RiceCooker();
        }
        
        if (currentRiceCooker != null && "ON".equals(currentRiceCooker.getState())) {
        	throw new ApplianceConflictException("Ricecooker is already running.");
        }
        
        currentRiceCooker.setState("ON");
        currentRiceCooker.setMode(mode);
        
        int cookingTime = getCookingTime();
        
        currentRiceCooker.setCookingTime(cookingTime);
        currentRiceCooker.setRemainingTime(cookingTime);
        
        //return riceCooker;
        
        notifyAppliances("Ricecooker is now running.");
                
	    scheduler.schedule(() -> stopCooking(currentRiceCooker.getId()), cookingTime, TimeUnit.SECONDS);
        
        // Schedule to start rice cooker after brewing time
        scheduler.scheduleAtFixedRate(this::updateCookingTime, 0, 1, TimeUnit.SECONDS);
        
        messagingTemplate.convertAndSend("/topic/riceCooker", currentRiceCooker);
        return riceCookerRepository.save(currentRiceCooker);
    }
    

	private void updateCookingTime() {
        int remainingTime = currentRiceCooker.getRemainingTime();

        if (remainingTime > 0) {
            remainingTime--; // Decrement the remaining time
            currentRiceCooker.setRemainingTime(remainingTime);
            riceCookerRepository.save(currentRiceCooker);

            // Send the updated brewing time to the UI
            int minutes = remainingTime / 60;
            int seconds = remainingTime % 60;
            String timeFormatted = String.format("%02d:%02d", minutes, seconds);
            
			System.out.println("Ricecooker Timer: " + timeFormatted);

            messagingTemplate.convertAndSend("/topic/riceCookerTimer", timeFormatted);
        } else {
        	System.out.println("Ricecooker has finished. Mixer will start in 10 seconds.");
            notifyAppliances("Ricecooker has finished. Mixer will start in 10 seconds.");
			
            int speed = 5;
            
            scheduler = Executors.newScheduledThreadPool(1); // Re-initialize scheduler
	        scheduler.schedule(() -> mixerService.startMixing(speed), 10, TimeUnit.SECONDS);
            
            stopCooking(currentRiceCooker.getId()); 
        }
        
    }

    public void stopCooking(Long id) {
    	currentRiceCooker = riceCookerRepository.findById(id).orElseThrow();
        
        if (!currentRiceCooker.getState().equals("ON")) {
            throw new IllegalStateException("Rice Cooker is already OFF.");
        }
        
        currentRiceCooker.setState("OFF");
        riceCookerRepository.save(currentRiceCooker);
        messagingTemplate.convertAndSend("/topic/riceCooker", currentRiceCooker);
        notifyAppliances("Ricecooker has now stopped.");
    }

    private int getCookingTime() {
    	return 10;
    }
    
	public RiceCooker getriceCookerState() {
		return riceCookerRepository.findLatestriceCooker();
	}
	
	
	private void notifyAppliances(String message) {
        messagingTemplate.convertAndSend("/topic/applianceStatus", message);
    }
	
	public boolean isRiceCookerOn() {
		currentRiceCooker = riceCookerRepository.findLatestriceCooker(); // Assuming this method exists
		return currentRiceCooker != null && currentRiceCooker.getState().equals("ON");
	}
}
