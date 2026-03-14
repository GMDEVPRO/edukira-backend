package com.edukira;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class EdukiraApplication {

    public static void main(String[] args) {
        SpringApplication.run(EdukiraApplication.class, args);
    }
}
