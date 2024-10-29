package com.nep.connectedkitchenapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nep.connectedkitchenapp.appliances.Microwave;
import com.nep.connectedkitchenapp.service.MicrowaveService;

@RestController
@RequestMapping("/microwave")
public class MicrowaveController {
	@Autowired
    private MicrowaveService microwaveService;

    @PostMapping("/start")
    public Microwave startHeating(@RequestParam int temperature, @RequestParam int timer) {
        return microwaveService.startHeating(temperature, timer);
    }

    @PostMapping("/stop/{id}")
    public void stopHeating(@PathVariable Long id) {
        microwaveService.stopHeating(id);
    }
}
