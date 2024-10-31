package com.nep.connectedkitchenapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class ConnectedKitchenAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(ConnectedKitchenAppApplication.class, args);
	}

}
