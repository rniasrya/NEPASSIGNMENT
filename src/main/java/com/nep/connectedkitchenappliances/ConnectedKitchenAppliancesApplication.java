package com.nep.connectedkitchenappliances;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.nep.connectedkitchenappliances.socket.ApplianceSocketServer;

@SpringBootApplication
public class ConnectedKitchenAppliancesApplication implements CommandLineRunner {

	public static void main(String[] args) {
        SpringApplication.run(ConnectedKitchenAppliancesApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        ApplianceSocketServer socketServer = new ApplianceSocketServer(8080); // Use your desired port
        new Thread(() -> socketServer.start()).start();
    }

}
