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

    private String state;       // ON or OFF
    private int temperature;    // Temperature setting
    private int timer;          // Timer in minutes
    private int remainingTime;	// Time in seconds
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

    public int getTemperature() { 
    	return temperature; 
    }
    
    public void setTemperature(int temperature) { 
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
    
}
