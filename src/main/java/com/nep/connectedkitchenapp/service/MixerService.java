package com.nep.connectedkitchenapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nep.connectedkitchenapp.appliances.Microwave;
import com.nep.connectedkitchenapp.appliances.Mixer;
import com.nep.connectedkitchenapp.respository.MixerRepository;

@Service
public class MixerService {
	@Autowired
    private MixerRepository mixerRepository;

    public Mixer startMixing(int speedLevel) {
        Mixer mixer = new Mixer();
        mixer.setState("ON");
        mixer.setSpeedLevel(speedLevel);
        return mixerRepository.save(mixer);
    }

    public void stopMixing(Long id) {
        Mixer mixer = mixerRepository.findById(id).orElseThrow();
        mixer.setState("OFF");
        mixerRepository.save(mixer);
    }

	public Mixer getMixerState() {
		return mixerRepository.findLatestMixer();
	}
}
