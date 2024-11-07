package com.nep.connectedkitchenapp.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nep.connectedkitchenapp.appliances.CoffeeMaker;
import com.nep.connectedkitchenapp.service.CoffeeMakerService;

@RestController
@RequestMapping("/coffeeMaker")
public class CoffeeMakerController {
	
    @Autowired
    private CoffeeMakerService coffeeMakerService;

    @PostMapping("/start")
    public CoffeeMaker startBrewing(@RequestParam String brewStrength) {
        return coffeeMakerService.startBrewing(brewStrength);
    }

    @PostMapping("/stop/{id}")
    public void stopBrewing(@PathVariable Long id) {
        coffeeMakerService.stopBrewing(id);
    }
    
    @PostMapping("/refillWater")
    public CoffeeMaker refillWaterResource() {
        return coffeeMakerService.refillWaterResource();
    }
    
    @PostMapping("/refillCoffeeGrounds")
    public CoffeeMaker refillCoffeeGroundsResource() {
        return coffeeMakerService.refillCoffeeGroundsResource();
    }
    
}
