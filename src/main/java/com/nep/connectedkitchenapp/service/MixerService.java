package com.nep.connectedkitchenapp.service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.nep.connectedkitchenapp.appliances.Microwave;
import com.nep.connectedkitchenapp.appliances.Mixer;
import com.nep.connectedkitchenapp.appliances.RiceCooker;
import com.nep.connectedkitchenapp.exception.ApplianceConflictException;
import com.nep.connectedkitchenapp.respository.MixerRepository;
import com.nep.connectedkitchenapp.respository.RiceCookerRepository;

@Service
public class MixerService {
	
	@Autowired
    private MixerRepository mixerRepository;
	
	@Autowired
    private CoffeeMakerService coffeeMakerService;

	@Lazy
    @Autowired
    private MicrowaveService microwaveService;

	@Lazy
    @Autowired
    private RiceCookerService riceCookerService;
	
	private Mixer currentMixer;
	
	@Autowired
    private SimpMessagingTemplate messagingTemplate;
	
	private ScheduledExecutorService scheduler;

	public MixerService(MixerRepository mixerRepository, SimpMessagingTemplate messagingTemplate) {
        this.mixerRepository = mixerRepository;
        this.messagingTemplate = messagingTemplate;
        this.scheduler = Executors.newScheduledThreadPool(1); // Initialize the scheduler
    }
	
    public Mixer startMixing(int speedLevel) {
    	currentMixer = mixerRepository.findLatestMixer();
    	
    	if (currentMixer != null && "ON".equals(currentMixer.getState())) {
    		throw new ApplianceConflictException("Mixer is already running.");
    	}
    	// Check if any other appliance is currently on
        if (coffeeMakerService.isCoffeeMakerOn() || microwaveService.isMicrowaveOn() || riceCookerService.isRiceCookerOn()) {
            throw new ApplianceConflictException("Another appliance is currently on. Please turn it off before starting the mixer.");
        }
        
        if (currentMixer == null) {
        	currentMixer = new Mixer();
        }
        
        
        currentMixer.setState("ON");
        currentMixer.setSpeedLevel(speedLevel);
        
        int mixingTime = getMixingTime();
        
        currentMixer.setMixingTime(mixingTime);
        currentMixer.setRemainingTime(mixingTime);
        
        //return mixerRepository.save(mixer);
        
        notifyAppliances("Mixer is now running.");
        
        scheduler.schedule(() -> stopMixing(currentMixer.getId()), mixingTime, TimeUnit.SECONDS);
        
        // Schedule to start mixer after rice cooker
        scheduler.scheduleAtFixedRate(this::updateMixingTime, 0, 1, TimeUnit.SECONDS);
        
        messagingTemplate.convertAndSend("/topic/mixer", currentMixer);
        return mixerRepository.save(currentMixer);
    }
    
    private void updateMixingTime() {
        int remainingTime = currentMixer.getRemainingTime();

        if (remainingTime > 0) {
            remainingTime--; // Decrement the remaining time
            currentMixer.setRemainingTime(remainingTime);
            mixerRepository.save(currentMixer);

            // Send the updated brewing time to the UI
            int minutes = remainingTime / 60;
            int seconds = remainingTime % 60;
            String timeFormatted = String.format("%02d:%02d", minutes, seconds);
            
			System.out.println("Mixer Timer: " + timeFormatted);

            messagingTemplate.convertAndSend("/topic/mixerTimer", timeFormatted);
        } else {
        	System.out.println("Mixer has finished.");
            notifyAppliances("Mixer has finished.");
			
            stopMixing(currentMixer.getId()); 
        }
        
    }

    public void stopMixing(Long id) {
        Mixer mixer = mixerRepository.findById(id).orElseThrow();
        
        if (!mixer.getState().equals("ON")) {
            throw new IllegalStateException("Mixer is already OFF.");
        }
        
        mixer.setState("OFF");
        mixerRepository.save(mixer);
        messagingTemplate.convertAndSend("/topic/mixer", mixer);
        
        notifyAppliances("Mixer has stopped.");
    }
    
    private int getMixingTime() {
    	return 10;
    }

	public Mixer getMixerState() {
		return mixerRepository.findLatestMixer();
	}
	
	
	private void notifyAppliances(String message) {
        messagingTemplate.convertAndSend("/topic/applianceStatus", message);
    }
	
	public boolean isMixerOn() {
		return mixerRepository.findAll().stream()
				.anyMatch(mixer -> "ON".equals(mixer.getState()));
	}
}
