package com.nep.connectedkitchenappliances.model;

public class Microwave {
	private boolean isHeating;
    private int heatTime; // in minutes

    public void startHeating(int heatTime) {
        this.heatTime = heatTime;
        isHeating = true;
        // Logic to start heating
    }

    public void stopHeating() {
        isHeating = false;
        // Logic to stop heating
    }

    public boolean isHeating() {
        return isHeating;
    }

    public int getHeatTime() {
        return heatTime;
    }
}
