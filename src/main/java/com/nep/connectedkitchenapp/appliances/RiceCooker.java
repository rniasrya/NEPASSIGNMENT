package com.nep.connectedkitchenapp.appliances;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class RiceCooker {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String state;       // ON or OFF
    private String mode;        // Cooking mode (e.g., "Rice", "Warm", "Steam")
    private int cookingTime;
    private int remainingTime;
    

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

    public String getMode() { 
    	return mode; 
    }
    
    public void setMode(String mode) { 
    	this.mode = mode; 
    }
    
    public int getCookingTime() {
    	return cookingTime;
    }
    
    public void setCookingTime(int cookingTime) {
    	this.cookingTime = cookingTime;
    }
    
    public int getRemainingTime() {
    	return remainingTime;
    }
    
    public void setRemainingTime(int remainingTime) {
    	this.remainingTime = remainingTime;
    }
}
