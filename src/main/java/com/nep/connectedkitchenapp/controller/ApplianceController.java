package com.nep.connectedkitchenapp.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.nep.connectedkitchenapp.appliances.CoffeeMaker;
import com.nep.connectedkitchenapp.appliances.Microwave;
import com.nep.connectedkitchenapp.appliances.Mixer;
import com.nep.connectedkitchenapp.appliances.RiceCooker;

@Controller
public class ApplianceController {
	
	private final SimpMessagingTemplate messagingTemplate;

    public ApplianceController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }
    
    // Coffee Maker
    @MessageMapping("/coffeeMaker")
    public void sendCoffeeMakerStatus(CoffeeMaker coffeeMaker) {
        messagingTemplate.convertAndSend("/topic/coffeeMaker", coffeeMaker);
    }

    // Mixer
    @MessageMapping("/mixer")
    public void sendMixerStatus(Mixer mixer) {
        messagingTemplate.convertAndSend("/topic/mixer", mixer);
    }

    // Microwave
    @MessageMapping("/microwave")
    public void sendMicrowaveStatus(Microwave microwave) {
        messagingTemplate.convertAndSend("/topic/microwave", microwave);
    }

    // Rice Cooker
    @MessageMapping("/riceCooker")
    public void sendRiceCookerStatus(RiceCooker riceCooker) {
        messagingTemplate.convertAndSend("/topic/riceCooker", riceCooker);
    }

}
