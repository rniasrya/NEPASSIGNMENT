package com.nep.connectedkitchenapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nep.connectedkitchenapp.appliances.Mixer;
import com.nep.connectedkitchenapp.service.MixerService;

@RestController
@RequestMapping("/mixer")
public class MixerController {
	
    @Autowired
    private MixerService mixerService;

    @PostMapping("/start")
    public Mixer startMixing(@RequestParam int speedLevel) {
        return mixerService.startMixing(speedLevel);
    }

    @PostMapping("/stop/{id}")
    public void stopMixing(@PathVariable Long id) {
        mixerService.stopMixing(id);
    }
}
