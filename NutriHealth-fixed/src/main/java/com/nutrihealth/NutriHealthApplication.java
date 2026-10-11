package com.nutrihealth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class NutriHealthApplication {

    public static void main(String[] args) {
        SpringApplication.run(NutriHealthApplication.class, args);
    }
}
