package com.nep.connectedkitchenapp.service;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Random;
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
	private int currentSpeed; // Current motor speed in RPM
    private final int maxSpeed = 1000; // Maximum motor speed (mapped from speed level 5)
    private final int minSpeed = 0; // Minimum motor speed
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
    private ScheduledExecutorService sensorScheduler = Executors.newScheduledThreadPool(1);  // Scheduler for the motor speed sensor
    private ScheduledFuture<?> motorSpeedTask;
    private Thread speedSensorThread;
    
    public MixerService(MixerRepository mixerRepository, SimpMessagingTemplate messagingTemplate) {
		this.socketServer = new ApplianceSocketServer(); // Initialize socket server
		this.mixerRepository = mixerRepository;
        this.messagingTemplate = messagingTemplate;
        this.currentSpeed = 0;
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
        
        // Set motor speed based on user speed level (1 to 5)
        setMotorSpeed(speedLevel);
        monitorMotorSpeed(); // Simulate monitoring of motor speed
        
        int mixingTime = getMixingTime();
        currentMixer.setMixingTime(mixingTime);
        currentMixer.setRemainingTime(mixingTime);
                
        notifyAppliances("Mixer is now running.");
		notifyAppliances("Mixer current status:" + 
				"\n\nID: " + currentMixer.getId() + 
				"\n" + "State: " + currentMixer.getState() + 
				"\nSpeed Level: " + currentMixer.getSpeedLevel() +
				"\nMotor Speed: " + currentMixer.getMotorSpeed() + " RPM" +
				"\nUsage Count: " + currentMixer.getUsageCount());
        
		sendSocketMessage("Mixer has started.");
		sendSocketMessage("Mixer session ID: " + sessionId);
		sendSocketMessage("Mixer is now running at " + currentMixer.getSpeedLevel() + " speed level for " + currentMixer.getMixingTime() + " seconds.");
		sendSocketMessage("Mixer Usage Count: " + currentMixer.getUsageCount());
		sendSocketMessage("Mixer Motor Speed: " + currentMixer.getMotorSpeed() + " RPM");
		
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
        
		// Simulate mixer task
        simulateSpeedSensor();
		
        // Save state in repository
        return mixerRepository.save(currentMixer);
    }
    
    // To simulate setting the speed based on speed level (1 to 5)
    private void setMotorSpeed(int speedLevel) {
        // Ensure the speed level is between 1 and 5
        if (speedLevel < 1) speedLevel = 1;
        if (speedLevel > 5) speedLevel = 5;

        // Map speed level to motor speed (e.g., 1 -> 200 RPM, 5 -> 1000 RPM)
        int[] speedMapping = {200, 400, 600, 800, 1000}; // Define motor speeds for levels 1 to 5
        currentSpeed = speedMapping[speedLevel - 1]; // Adjust motor speed based on level
        
        // Set motor speed in the currentMixer object
        currentMixer.setMotorSpeed(currentSpeed);
        mixerRepository.save(currentMixer);
    }
    
    // Simulates the mixing process and monitoring the motor speed
    private void simulateSpeedSensor() {
    	speedSensorThread = new Thread(() -> {
            Random random = new Random();
            int baseSpeed = currentSpeed;

            try {
                while ("ON".equals(currentMixer.getState()) && !Thread.currentThread().isInterrupted()) {
                    // Introduce a fluctuation in speed (randomly increase/decrease by 10 RPM)
                    int fluctuation = random.nextInt(21) - 10; // Random value between -10 and 10
                    int newSpeed = baseSpeed + fluctuation;

                    // Ensure the speed stays within the allowable range
                    newSpeed = Math.max(minSpeed, Math.min(maxSpeed, newSpeed));
                    currentSpeed = newSpeed;
                    monitorMotorSpeed(); 

                    // Wait for a short delay 
                    Thread.sleep(2000); // 2 seconds delay between updates
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Preserve the interrupt status
            }
        });
        speedSensorThread.start(); // Start the sensor thread
    }
    
    // Monitors the motor speed (simulating the sensor monitoring)
    private void monitorMotorSpeed() {    	
    	// Send the motor speed to the UI through WebSocket
    	messagingTemplate.convertAndSend("/topic/motorSpeed", currentSpeed);
    }
    
    // To start monitoring the motor speed at fixed intervals
    public void startMotorSpeedMonitoring() {
        if (motorSpeedTask != null && !motorSpeedTask.isDone()) {
            motorSpeedTask.cancel(false);  
        }
        
        // Schedule the motor speed monitoring to run every 1 second (or another interval of your choice)
        motorSpeedTask = sensorScheduler.scheduleAtFixedRate(this::monitorMotorSpeed, 0, 1, TimeUnit.SECONDS);
    }
    

    // Stop the motor speed monitoring when it's no longer needed
    public void stopMotorSpeedMonitoring() {
        if (motorSpeedTask != null && !motorSpeedTask.isDone()) {
            motorSpeedTask.cancel(false);  // Cancel the monitoring task
        }
        scheduler.shutdownNow();
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

        if (mixingTask != null && !mixingTask.isDone()) {
        	mixingTask.cancel(false);
        }
        
        if (speedSensorThread != null && speedSensorThread.isAlive()) {
            speedSensorThread.interrupt(); // Interrupt the speed simulation thread
        }

        // Notify appliances that mixing has finished
        sendSocketMessage("Mixer has finished.");
        notifyAppliances("Mixer has finished.");
               
        stopMotorSpeedMonitoring();

        // Shutdown the scheduler
        scheduler.shutdown();
	}

    public void stopMixing(Long id) {
        Mixer mixer = mixerRepository.findById(id).orElseThrow();
        
        if (!mixer.getState().equals("ON")) {
            throw new IllegalStateException("Mixer is already OFF.");
        }
                
        mixer.setState("OFF");
        currentMixer.setRemainingTime(0);
        mixerRepository.save(mixer);
        
        currentSpeed = 0;  // Set to 0 directly instead of using setMotorSpeed
        
        monitorMotorSpeed();
        
        messagingTemplate.convertAndSend("/topic/mixer", mixer);
        messagingTemplate.convertAndSend("/topic/mixerTimer", "00:00");
        
        sendSocketMessage("Mixer has stopped.");
        notifyAppliances("Mixer has stopped.");
        
        // Stop the motor speed sensor simulation
        if (speedSensorThread != null && speedSensorThread.isAlive()) {
            speedSensorThread.interrupt(); // Interrupt the speed simulation thread
        }
        
        stopMotorSpeedMonitoring();
                
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