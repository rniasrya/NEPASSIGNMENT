package com.nep.connectedkitchenapp.respository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.nep.connectedkitchenapp.appliances.Microwave;

public interface MicrowaveRepository extends JpaRepository<Microwave, Long>{

	@Query("SELECT cm FROM Microwave cm ORDER BY cm.id DESC")
	Microwave findLatestMicrowave();

}
