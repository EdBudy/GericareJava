package com.example.Gericare;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class GericareApplication {
    public static void main(String[] args) {
        SpringApplication.run(GericareApplication.class, args);
    }
}
