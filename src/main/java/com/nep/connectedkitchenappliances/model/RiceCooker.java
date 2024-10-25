package com.nep.connectedkitchenappliances.model;

public class RiceCooker {
	private boolean isCooking;
    private int cookingTime; // in minutes

    public void startCooking(int cookingTime) {
        this.cookingTime = cookingTime;
        isCooking = true;
        // Logic to start cooking
    }

    public void stopCooking() {
        isCooking = false;
        // Logic to stop cooking
    }

    public boolean isCooking() {
        return isCooking;
    }

    public int getCookingTime() {
        return cookingTime;
    }
}
