package com.nep.connectedkitchenappliances.model;

public class CoffeeMaker {
	private boolean isBrewing;
    private int brewTime; // in minutes

    public void startBrewing(int brewTime) {
        this.brewTime = brewTime;
        isBrewing = true;
        // Logic to start brewing
    }

    public void stopBrewing() {
        isBrewing = false;
        // Logic to stop brewing
    }

    public boolean isBrewing() {
        return isBrewing;
    }

    public int getBrewTime() {
        return brewTime;
    }
}
