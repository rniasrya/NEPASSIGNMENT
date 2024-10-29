package com.nep.connectedkitchenapp.respository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.nep.connectedkitchenapp.appliances.Microwave;
import com.nep.connectedkitchenapp.appliances.RiceCooker;

public interface RiceCookerRepository extends JpaRepository<RiceCooker, Long> {

	@Query("SELECT cm FROM RiceCooker cm ORDER BY cm.id DESC")
	RiceCooker findLatestriceCooker();

}
