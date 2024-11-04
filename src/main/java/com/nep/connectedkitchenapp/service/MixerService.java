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

import com.nep.connectedkitchenapp.appliances.Microwave;
import com.nep.connectedkitchenapp.appliances.Mixer;
import com.nep.connectedkitchenapp.appliances.RiceCooker;
import com.nep.connectedkitchenapp.exception.ApplianceConflictException;
import com.nep.connectedkitchenapp.respository.MixerRepository;
import com.nep.connectedkitchenapp.respository.RiceCookerRepository;

@Service
public class MixerService {
	
	private Mixer currentMixer;
	
	private ApplianceSocketServer socketServer;
	
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

	@Autowired
	private SimpMessagingTemplate messagingTemplate; // Message template for WebSocket communication
	
	private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);; // Scheduler for timed tasks
    
    private ScheduledFuture<?> mixingTask; // Stores the mixing task for cancellation or modification
    
	public MixerService(MixerRepository mixerRepository, SimpMessagingTemplate messagingTemplate) {
		this.socketServer = new ApplianceSocketServer(); // Initialize socket server
		this.mixerRepository = mixerRepository;
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
	
	// Starts the mixing process
    public Mixer startMixing(int speedLevel) {
    	currentMixer = mixerRepository.findLatestMixer();
    	
    	// Check if mixer is currently running
    	if (currentMixer != null && "ON".equals(currentMixer.getState())) {
    		throw new ApplianceConflictException("Mixer is already running.");
    	}
    	
    	// Check if any other appliance is currently on
        if (coffeeMakerService.isCoffeeMakerOn() || microwaveService.isMicrowaveOn() || riceCookerService.isRiceCookerOn()) {
            throw new ApplianceConflictException("Another appliance is currently on. Please turn it off before starting the mixer.");
        }
        
        // Initialize mixer if it's new or or has been reset yet 
        if (currentMixer == null) {
        	currentMixer = new Mixer();
        }
        
        // Set up unique session, usage count, and state
        currentMixer.setUsageCount(currentMixer.getUsageCount() + 1); 
        String sessionId = UUID.randomUUID().toString();
        currentMixer.setSessionId(sessionId);
        
        // Send usage updates
        messagingTemplate.convertAndSend("/topic/mixerUsage", currentMixer.getUsageCount());        
        
        // Set up mixing properties and notify
        currentMixer.setState("ON");
        currentMixer.setSpeedLevel(speedLevel);
        int mixingTime = getMixingTime();
        currentMixer.setMixingTime(mixingTime);
        currentMixer.setRemainingTime(mixingTime);
                
        notifyAppliances("Mixer is now running.");
		notifyAppliances("Mixer current status:" + "\n\nID: " + currentMixer.getId() + "\n" + "State: " + currentMixer.getState() + "\nSpeed Level: " + currentMixer.getSpeedLevel());
        
		sendSocketMessage("Mixer start");
		sendSocketMessage("Mixer session ID: " + sessionId);
		sendSocketMessage("Mixer is now running at " + currentMixer.getSpeedLevel() + " speed level for " + currentMixer.getMixingTime() + " seconds.");
		sendSocketMessage("Mixer Usage Count: " + currentMixer.getUsageCount());
		
		// Mixer state in the UI
		messagingTemplate.convertAndSend("/topic/mixer", currentMixer);
		
        scheduler = Executors.newScheduledThreadPool(1);
		
		if (mixingTask != null && !mixingTask.isDone()) {
			mixingTask.cancel(false);
        }
		
		// Schedule the mixing to stop automatically after set time
		mixingTask = scheduler.schedule(() -> stopMixingforTimer(), mixingTime, TimeUnit.SECONDS);
        
		// Schedule regular updates for remaining mixing time
		scheduler.scheduleAtFixedRate(this::updateMixingTime, 0, 1, TimeUnit.SECONDS);
        
        // Save state in repository
        return mixerRepository.save(currentMixer);
    }

	private void updateMixingTime() {
        int remainingTime = currentMixer.getRemainingTime();

        if (remainingTime > 0) {
            remainingTime--; 
            currentMixer.setRemainingTime(remainingTime);

            int minutes = remainingTime / 60;
            int seconds = remainingTime % 60;
            String timeFormatted = String.format("%02d:%02d", minutes, seconds);
            
            sendSocketMessage("Mixer Timer: " + timeFormatted);
            messagingTemplate.convertAndSend("/topic/mixerTimer", timeFormatted);
        } else {            
            stopMixingforTimer();                
        }
    }
	
	public void stopMixingforTimer() {
		if (currentMixer == null) {
            return;
        }
		
		currentMixer.setState("OFF");
		mixerRepository.save(currentMixer);
        messagingTemplate.convertAndSend("/topic/mixer", currentMixer);

        // Cancel the scheduled cooking task
        if (mixingTask != null && !mixingTask.isDone()) {
        	mixingTask.cancel(false);
        }

        // Notify appliances that mixing has finished
        sendSocketMessage("Mixer has finished.");
        notifyAppliances("Mixer has finished.");
        
        // Shutdown the scheduler
        scheduler.shutdown();
	}

    public void stopMixing(Long id) {
        Mixer mixer = mixerRepository.findById(id).orElseThrow();
        
        if (!mixer.getState().equals("ON")) {
            throw new IllegalStateException("Mixer is already OFF.");
        }
                
        mixer.setState("OFF");
        mixerRepository.save(mixer);
        messagingTemplate.convertAndSend("/topic/mixer", mixer);
        
        sendSocketMessage("Mixer has stopped.");
        notifyAppliances("Mixer has stopped.");
                
        scheduler.shutdownNow();
    }
    
    private int getMixingTime() {
    	return 15;
    }

    // Gets the latest mixer state
	public Mixer getMixerState() {
		return mixerRepository.findLatestMixer();
	}
	
	// Sends a message to notify all appliances
	private void notifyAppliances(String message) {
        messagingTemplate.convertAndSend("/topic/applianceStatus", message);
    }
	
	// Checks if mixer is currently mixing
	public boolean isMixerOn() {
		return mixerRepository.findAll().stream()
				.anyMatch(mixer -> "ON".equals(mixer.getState()));
	}
}