package com.nep.connectedkitchenapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nep.connectedkitchenapp.appliances.Microwave;
import com.nep.connectedkitchenapp.appliances.RiceCooker;
import com.nep.connectedkitchenapp.respository.RiceCookerRepository;

@Service
public class RiceCookerService {
	@Autowired
    private RiceCookerRepository riceCookerRepository;

    public RiceCooker startCooking(String mode) {
        RiceCooker riceCooker = new RiceCooker();
        riceCooker.setState("ON");
        riceCooker.setMode(mode);
        return riceCookerRepository.save(riceCooker);
    }

    public void stopCooking(Long id) {
        RiceCooker riceCooker = riceCookerRepository.findById(id).orElseThrow();
        riceCooker.setState("OFF");
        riceCookerRepository.save(riceCooker);
    }

	public RiceCooker getriceCookerState() {
		return riceCookerRepository.findLatestriceCooker();
	}
}
