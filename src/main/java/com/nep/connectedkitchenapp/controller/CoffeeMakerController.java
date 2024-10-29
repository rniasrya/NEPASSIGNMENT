package com.nep.connectedkitchenapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nep.connectedkitchenapp.appliances.CoffeeMaker;
import com.nep.connectedkitchenapp.service.CoffeeMakerService;

@RestController
@RequestMapping("/coffeeMaker")
public class CoffeeMakerController {
    @Autowired
    private CoffeeMakerService coffeeMakerService;

    @PostMapping("/start")
    public CoffeeMaker startBrewing() {
        return coffeeMakerService.startBrewing();
    }

    @PostMapping("/stop/{id}")
    public void stopBrewing(@PathVariable Long id) {
        coffeeMakerService.stopBrewing(id);
    }
}
