package com.orquestro;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main entry point for the Orquestro platform.
 * 
 * This class initializes the Spring Boot application context. 
 * In a multi-module environment, explicit scanning is required to ensure 
 * that entities and repositories from 'orquestro-data' and services from 
 * 'orquestro-management' are correctly discovered and managed by the Spring IoC container.
 * 
 * @author L.F. Desenvolvimento de Softwares LTDA
 */
@SpringBootApplication(scanBasePackages = "com.orquestro")
@EntityScan(basePackages = "com.orquestro")
@EnableJpaRepositories(basePackages = "com.orquestro")
@EnableScheduling
public class OrquestroApplication {

    /**
     * Starts the Orquestro application.
     * 
     * @param args command-line arguments passed to the application.
     */
    public static void main(String[] args) {
        SpringApplication.run(OrquestroApplication.class, args);
    }
}