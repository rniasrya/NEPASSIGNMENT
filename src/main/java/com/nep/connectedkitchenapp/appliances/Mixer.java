package com.nep.connectedkitchenapp.appliances;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Mixer {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String state;       
    private int speedLevel;      
    private int mixingTime;
    private int remainingTime;
    private int usageCount; 
    private String sessionId;
    

	// Getters and Setters
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

    public int getSpeedLevel() { 
    	return speedLevel; 
    }
    
    public void setSpeedLevel(int speedLevel) { 
    	this.speedLevel = speedLevel; 
    }
	
    public int getMixingTime() {
    	return mixingTime;
    }
    
    public void setMixingTime(int mixingTime) {
    	this.mixingTime = mixingTime;
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
