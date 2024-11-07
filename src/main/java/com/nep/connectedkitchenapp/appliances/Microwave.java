package com.nep.connectedkitchenapp.appliances;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Microwave {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String state;    
    private double temperature; 

	private int timer;          
    private int remainingTime;	
    private int usageCount; 
    private String sessionId;
    

	public Long getId() { 
    	return id; 
    }
    
    public void setId(Long id) { 
    	this.id = id; 
    }

    public String getState() { 
    	return state; 
    }
    
    public void setState(String state) { 
    	this.state = state; 
    }

    public double getTemperature() { 
    	return temperature; 
    }
    
    public void setTemperature(double temperature) { 
    	this.temperature = temperature; 
    }

    public int getTimer() { 
    	return timer; 
    }
    
    public void setTimer(int timer) { 
    	this.timer = timer; 
    }
    
    public int getRemainingTime() {
    	return remainingTime;
    }
    
    public void setRemainingTime(int remainingTime) {
    	this.remainingTime = remainingTime;
    }
    
    public int getUsageCount() {
    	return usageCount;
    }
    
    public void setUsageCount(int usageCount) {
    	this.usageCount = usageCount;
    }
    
    public String getSessionId() {
    	return sessionId;
    }
    
    public void setSessionId(String sessionId) {
    	this.sessionId = sessionId;
    }
}
