package com.nep.connectedkitchenapp.respository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.nep.connectedkitchenapp.appliances.Microwave;
import com.nep.connectedkitchenapp.appliances.Mixer;

public interface MixerRepository extends JpaRepository<Mixer, Long>{

	@Query("SELECT cm FROM Mixer cm ORDER BY cm.id DESC")
	Mixer findLatestMixer();

}
