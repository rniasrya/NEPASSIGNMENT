package com.nep.connectedkitchenapp.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.nep.connectedkitchenapp.appliances.CoffeeMaker;
import com.nep.connectedkitchenapp.appliances.Microwave;
import com.nep.connectedkitchenapp.appliances.Mixer;
import com.nep.connectedkitchenapp.appliances.RiceCooker;
import com.nep.connectedkitchenapp.service.CoffeeMakerService;
import com.nep.connectedkitchenapp.service.MicrowaveService;
import com.nep.connectedkitchenapp.service.MixerService;
import com.nep.connectedkitchenapp.service.RiceCookerService;

public class WebSocketController {
	
	@Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private CoffeeMakerService coffeeMakerService;

    @Autowired
    private MicrowaveService microwaveService;
    
    @Autowired
    private MixerService mixerService;
    
    @Autowired
    private RiceCookerService riceCookerService;
    
    @MessageMapping("/coffeeMaker")
    @SendTo("/topic/coffeeMaker")
    public CoffeeMaker getCoffeeMakerState() {
        return coffeeMakerService.getCoffeeMakerState(); 
    }
    
    @MessageMapping("/microwave")
    @SendTo("/topic/microwave")
    public Microwave getMicrowaveState() {
        return microwaveService.getMicrowaveState(); 
    }
    
    @MessageMapping("/mixer")
    @SendTo("/topic/mixer")
    public Mixer getMixerState() {
        return mixerService.getMixerState(); 
    }
    
    @MessageMapping("/riceCooker")
    @SendTo("/topic/riceCooker")
    public RiceCooker getRiceCookerState() {
        return riceCookerService.getriceCookerState(); 
    }
    
    
}
