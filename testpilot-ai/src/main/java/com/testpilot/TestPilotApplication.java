package com.testpilot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the TestPilot application.
 * This is a Spring Boot application designed to facilitate AI-driven test case
 * generation.
 */

@SpringBootApplication
public class TestPilotApplication {

    /**
     * Bootstraps the Spring application context.
     * 
     * @param args command line arguments.
     */
    public static void main(String[] args) {
        SpringApplication.run(TestPilotApplication.class, args);
    }
}
