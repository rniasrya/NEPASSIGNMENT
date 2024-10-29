package com.nep.connectedkitchenapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nep.connectedkitchenapp.appliances.CoffeeMaker;
import com.nep.connectedkitchenapp.appliances.Microwave;
import com.nep.connectedkitchenapp.respository.MicrowaveRepository;

@Service
public class MicrowaveService {
	@Autowired
    private MicrowaveRepository microwaveRepository;

	public Microwave startHeating(int temperature, int timer) {
		Microwave microwave = new Microwave();
		microwave.setState("ON");
		microwave.setTemperature(temperature);
		microwave.setTimer(timer);
		return microwaveRepository.save(microwave);
	}
	
    public void stopHeating(Long id) {
        Microwave microwave = microwaveRepository.findById(id).orElseThrow();
        microwave.setState("OFF");
        microwaveRepository.save(microwave);
    }

	public Microwave getMicrowaveState() {
		return microwaveRepository.findLatestMicrowave();
	}

}
