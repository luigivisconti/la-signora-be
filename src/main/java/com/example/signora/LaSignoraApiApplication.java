package com.example.signora;

// Backend API Implementation for Azure OpenAI

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties

@SpringBootApplication
public class LaSignoraApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(LaSignoraApiApplication.class, args);
    }
}

