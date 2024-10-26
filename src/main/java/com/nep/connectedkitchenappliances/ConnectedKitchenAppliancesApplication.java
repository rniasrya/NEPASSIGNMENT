package com.nep.connectedkitchenappliances;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.nep.connectedkitchenappliances.socket.ApplianceSocketClient;
import com.nep.connectedkitchenappliances.socket.ApplianceSocketServer;

@SpringBootApplication
public class ConnectedKitchenAppliancesApplication implements CommandLineRunner {

	public static void main(String[] args) {
        SpringApplication.run(ConnectedKitchenAppliancesApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        ApplianceSocketServer socketServer = new ApplianceSocketServer(2004); // Use your desired port
        new Thread(() -> socketServer.start()).start();
    }
    
    @Bean
    public ApplianceSocketClient applianceSocketClient() {
        return new ApplianceSocketClient("localhost", 2004); // Adjust server address and port as needed
    }

}
