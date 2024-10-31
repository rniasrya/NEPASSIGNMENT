package com.nep.connectedkitchenapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nep.connectedkitchenapp.appliances.RiceCooker;
import com.nep.connectedkitchenapp.service.RiceCookerService;

@RestController
@RequestMapping("/riceCooker")
public class RiceCookerController {
	
	@Autowired
    private RiceCookerService riceCookerService;

    @PostMapping("/start")
    public RiceCooker startCooking(@RequestParam String mode) {
        return riceCookerService.startCooking(mode);
    }

    @PostMapping("/stop/{id}")
    public void stopCooking(@PathVariable Long id) {
        riceCookerService.stopCooking(id);
    }
}
