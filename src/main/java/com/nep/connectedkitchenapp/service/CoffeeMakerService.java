package com.nep.connectedkitchenapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nep.connectedkitchenapp.appliances.CoffeeMaker;
import com.nep.connectedkitchenapp.respository.CoffeeMakerRepository;

@Service
public class CoffeeMakerService {
	@Autowired
    private CoffeeMakerRepository coffeeMakerRepository;

    public CoffeeMaker startBrewing() {
        CoffeeMaker coffeeMaker = new CoffeeMaker();
        coffeeMaker.setState("ON");
        coffeeMaker.setTemperature(90);
        coffeeMaker.setBrewTime(5);
        return coffeeMakerRepository.save(coffeeMaker);
    }

    public void stopBrewing(Long id) {
        CoffeeMaker coffeeMaker = coffeeMakerRepository.findById(id).orElseThrow();
        coffeeMaker.setState("OFF");
        coffeeMakerRepository.save(coffeeMaker);
    }
    
    public CoffeeMaker getCoffeeMakerState() {
        return coffeeMakerRepository.findLatestCoffeeMaker(); // Implement this query in your repository
    }
	
}
