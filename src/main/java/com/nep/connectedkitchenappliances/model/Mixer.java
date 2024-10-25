package com.nep.connectedkitchenappliances.model;

public class Mixer {
	private boolean isMixing;
    private int mixTime; // in minutes

    public void startMixing(int mixTime) {
        this.mixTime = mixTime;
        isMixing = true;
        // Logic to start mixing
    }

    public void stopMixing() {
        isMixing = false;
        // Logic to stop mixing
    }

    public boolean isMixing() {
        return isMixing;
    }

    public int getMixTime() {
        return mixTime;
    }
}
