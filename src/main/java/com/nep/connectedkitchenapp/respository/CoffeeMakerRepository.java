package com.nep.connectedkitchenapp.respository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.nep.connectedkitchenapp.appliances.CoffeeMaker;

public interface CoffeeMakerRepository extends JpaRepository<CoffeeMaker, Long> {

	@Query("SELECT cm FROM CoffeeMaker cm ORDER BY cm.id DESC")
    CoffeeMaker findLatestCoffeeMaker(); // This fetches the most recent coffee maker state
}
