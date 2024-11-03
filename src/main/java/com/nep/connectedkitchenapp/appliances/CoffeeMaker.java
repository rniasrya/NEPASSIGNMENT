package com.nep.connectedkitchenapp.appliances;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
public class CoffeeMaker {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
    private String state;
    private int temperature;
    private int brewTime; 
    private String brewStrength;
    private int remainingTime;
    private int usageCount; 
    private String sessionId;
    private int waterLevel;
    private int coffeeGroundsLevel;


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

    public int getBrewTime() {
        return brewTime;
    }

    public void setBrewTime(int brewTime) {
        this.brewTime = brewTime;
    }
    
    public String getBrewStrength() {
    	return brewStrength;
    }
    
    public void setBrewStrength(String brewStrength) {
    	this.brewStrength = brewStrength;
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
	
	public int getWaterLevel() {
		return waterLevel;
	}
	
	public void setWaterLevel(int waterLevel) {
		this.waterLevel = waterLevel;
	}
	
	public int getCoffeeGroundsLevel() {
		return coffeeGroundsLevel;
	}
	
	public void setCoffeeGroundsLevel(int coffeeGroundsLevel) {
		this.coffeeGroundsLevel = coffeeGroundsLevel;
	}
}
