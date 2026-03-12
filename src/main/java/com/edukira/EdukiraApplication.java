package com.edukira;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class EdukiraApplication {

    public static void main(String[] args) {
        SpringApplication.run(EdukiraApplication.class, args);
    }
}
