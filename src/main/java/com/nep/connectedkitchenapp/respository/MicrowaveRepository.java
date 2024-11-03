package com.nep.connectedkitchenapp.respository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.nep.connectedkitchenapp.appliances.CoffeeMaker;
import com.nep.connectedkitchenapp.appliances.Microwave;

public interface MicrowaveRepository extends JpaRepository<Microwave, Long>{

	List<Microwave> findAll();
	
	@Query("SELECT cm FROM Microwave cm ORDER BY cm.id DESC")
    List<Microwave> findAllMicrowavesOrderedById();
	
	@Query("SELECT cm FROM Microwave cm ORDER BY cm.id DESC")
	Microwave findLatestMicrowave();

}
