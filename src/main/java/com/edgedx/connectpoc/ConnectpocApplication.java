package com.edgedx.connectpoc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ConnectpocApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConnectpocApplication.class, args);
    }

}
