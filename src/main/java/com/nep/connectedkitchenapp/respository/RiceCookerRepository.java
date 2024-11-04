package com.nep.connectedkitchenapp.respository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.nep.connectedkitchenapp.appliances.Microwave;
import com.nep.connectedkitchenapp.appliances.Mixer;
import com.nep.connectedkitchenapp.appliances.RiceCooker;

public interface RiceCookerRepository extends JpaRepository<RiceCooker, Long> {

	List<RiceCooker> findAll();
	
	@Query("SELECT cm FROM RiceCooker cm ORDER BY cm.id DESC")
    List<RiceCooker> findAllRiceCookersOrderedById();
	
	@Query("SELECT cm FROM RiceCooker cm ORDER BY cm.id DESC")
	RiceCooker findLatestriceCooker();

}