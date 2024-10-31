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
import com.nep.connectedkitchenapp.respository.MicrowaveRepository;

@Service
public class MicrowaveService {
	
	/*@Autowired
    private MicrowaveRepository microwaveRepository;
	
	@Autowired
    private SimpMessagingTemplate messagingTemplate;

	public Microwave startHeating(int temperature, int timer) {
		Microwave microwave = new Microwave();
		microwave.setState("ON");
		microwave.setTemperature(temperature);
		microwave.setTimer(timer);
		
		messagingTemplate.convertAndSend("/topic/microwave", microwave);
		return microwaveRepository.save(microwave);
        //return microwave;
	}
	
    public void stopHeating(Long id) {
        Microwave microwave = microwaveRepository.findById(id).orElseThrow();
        microwave.setState("OFF");
        
        microwaveRepository.save(microwave);
        messagingTemplate.convertAndSend("/topic/microwave", microwave);
    }

	public Microwave getMicrowaveState() {
		return microwaveRepository.findLatestMicrowave();
	}*/
	
	private final MicrowaveRepository microwaveRepository;
	
    private final SimpMessagingTemplate messagingTemplate;

    @Lazy
    @Autowired
    private CoffeeMakerService coffeeMakerService;
    
    @Lazy
    @Autowired
    private MixerService mixerService;

    @Autowired
    private RiceCookerService riceCookerService;

    private ScheduledExecutorService scheduler;
    
    private Microwave currentMicrowave;
    
    private String brewStrength;
	
    @Autowired
    public MicrowaveService(MicrowaveRepository microwaveRepository, SimpMessagingTemplate messagingTemplate, CoffeeMakerService coffeeMakerService) {
        this.microwaveRepository = microwaveRepository;
        this.messagingTemplate = messagingTemplate;
        this.scheduler = Executors.newScheduledThreadPool(1); // Initialize the scheduler once
    }

	public Microwave startHeating(int temperature, int timer) {
    	// Retrieve the latest coffee maker state
		currentMicrowave = microwaveRepository.findLatestMicrowave();
		
		if (coffeeMakerService.isCoffeeMakerOn() || mixerService.isMixerOn() || riceCookerService.isRiceCookerOn()) {
            throw new ApplianceConflictException("Another appliance is currently on. Please turn it off before starting the microwave.");
        }
		
		// Check if the microwave is already running
		if (currentMicrowave != null && "ON".equals(currentMicrowave.getState())) {
	        throw new ApplianceConflictException("Microwave is already ON.");
	    }
		
		// Initialize and start Microwave if it is OFF
		if (currentMicrowave  == null) {
			currentMicrowave  = new Microwave();
		}
		
		currentMicrowave.setState("ON");
		currentMicrowave.setTemperature(temperature);
		currentMicrowave.setTimer(timer);
		currentMicrowave.setRemainingTime(timer * 60);
		
		// Notify the user that it is starting to run
		notifyAppliances("Microwave is now running.");
		
		// Start a scheduled executor to decrease remaining time
	    scheduler = Executors.newScheduledThreadPool(1);
	    
	    // Start a scheduled executor to decrease remaining time
	    scheduler.schedule(() -> stopHeating(currentMicrowave.getId()), timer, TimeUnit.MINUTES);

	    // Schedule the remaining time update
	    scheduler.scheduleAtFixedRate(this::updateRemainingTime, 0, 1, TimeUnit.SECONDS);
        
	    messagingTemplate.convertAndSend("/topic/microwave", currentMicrowave.getState());
        return microwaveRepository.save(currentMicrowave);
	}
	
	private void updateRemainingTime() {
		int remainingTime = currentMicrowave.getRemainingTime();
		
		if (remainingTime > 0) {
			remainingTime--; // Decrement the remaining time
			currentMicrowave.setRemainingTime(remainingTime);
			microwaveRepository.save(currentMicrowave);
			
			int minutes = remainingTime / 60;
			int seconds = remainingTime % 60;
			String timeFormatted = String.format("%02d:%02d", minutes, seconds);
			
			System.out.println("Microwave Timer: " + timeFormatted);
			
			messagingTemplate.convertAndSend("/topic/microwaveTimer", timeFormatted);
		} else {
			System.out.println("Microwave has finished heating. Coffee Maker will start brewing in 10 seconds.");
			
			notifyAppliances("Microwave has finished heating. Coffee Maker will start brewing in 10 seconds.");
			
			//messagingTemplate.convertAndSend("/topic/applianceStatus", "Microwave has finished. Coffee Maker will start brewing in 10 seconds.");
	        
			String brewStrength = "Medium";
			
			scheduler = Executors.newScheduledThreadPool(1); // Re-initialize scheduler
	        scheduler.schedule(() -> coffeeMakerService.startBrewing(brewStrength), 10, TimeUnit.SECONDS);
			
	        stopHeating(currentMicrowave.getId());
	        // Shut down the scheduler
            if (scheduler != null && !scheduler.isShutdown()) {
                scheduler.shutdown();
                stopHeating(currentMicrowave.getId());                
            } 
            
            // Stop heating the microwave
            
			// Schedule coffee maker start in 5 seconds
	        //messagingTemplate.convertAndSend("/topic/applianceStatus", "Microwave has finished. Coffee Maker will start brewing in 10 seconds.");
	        //scheduler = Executors.newScheduledThreadPool(1); // Re-initialize scheduler
	        //scheduler.schedule(() -> coffeeMakerService.startBrewing(), 10, TimeUnit.SECONDS);
			
		}
	}
	
	public void stopHeating(Long id) {
		Microwave microwave = microwaveRepository.findById(id).orElseThrow();
		
		if (!microwave.getState().equals("ON")) {
			throw new IllegalStateException("Microwave is already OFF.");
		}
		
		microwave.setState("OFF");
		microwaveRepository.save(microwave);
		
		//isRunning = false;
		//notifyAppliances("Microwave has stopped.");
		//messagingTemplate.convertAndSend("/topic/microwave", microwave);
		notifyAppliances("Microwave has stopped.");
		System.out.println("Microwave has stopped.");
		//messagingTemplate.convertAndSend("/topic/microwave", microwave);
		//messagingTemplate.convertAndSend("/topic/microwave", "Mirowave has stopped.");
		
		// Shut down the scheduler
		//if (scheduler != null && !scheduler.isShutdown()) {
			//scheduler.shutdown();
		//}
		
	}
	
    
    public int getRemainingTime(Long id) {
        Microwave microwave = microwaveRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Microwave not found"));
        return microwave.getRemainingTime();
    }


	public Microwave getMicrowaveState() {
		return microwaveRepository.findLatestMicrowave();
	}

	
	private void notifyAppliances(String message) {
        messagingTemplate.convertAndSend("/topic/applianceStatus", message);
    }
	
	public boolean isMicrowaveOn() {
		// Retrieve the latest microwave record and check its state
		Microwave microwave = microwaveRepository.findLatestMicrowave(); // Implement this in MicrowaveRepository
		return microwave != null && "ON".equals(microwave.getState());
	}
}
