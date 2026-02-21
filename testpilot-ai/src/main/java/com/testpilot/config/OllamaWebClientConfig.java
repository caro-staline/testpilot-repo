package com.testpilot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Configuration class for the Ollama WebClient.
 * Provides a bean for WebClient pre-configured with the Ollama service URL.
 */
@Configuration
public class OllamaWebClientConfig {

    /**
     * Creates and configures a WebClient bean for interacting with the Ollama API.
     * 
     * @return a configured WebClient instance.
     */
    @Bean
    public WebClient ollamaWebClient() {
        // Build WebClient with the local Ollama base URL
        return WebClient.builder()
                .baseUrl("http://localhost:11434")
                .build();
    }
}
