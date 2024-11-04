package com.nep.connectedkitchenapp.respository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.nep.connectedkitchenapp.appliances.CoffeeMaker;

public interface CoffeeMakerRepository extends JpaRepository<CoffeeMaker, Long> {

	List<CoffeeMaker> findAll();
	
	@Query("SELECT cm FROM CoffeeMaker cm ORDER BY cm.id DESC")
    List<CoffeeMaker> findAllCoffeeMakersOrderedById();
	
	@Query("SELECT cm FROM CoffeeMaker cm ORDER BY cm.id DESC")
	CoffeeMaker findLatestCoffeeMaker();
}