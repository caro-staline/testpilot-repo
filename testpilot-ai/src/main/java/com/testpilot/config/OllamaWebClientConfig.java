package com.testpilot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class OllamaWebClientConfig {

    @Bean
    public WebClient ollamaWebClient() {
        return WebClient.builder()
                .baseUrl("http://localhost:11434")
                .build();
    }
}

