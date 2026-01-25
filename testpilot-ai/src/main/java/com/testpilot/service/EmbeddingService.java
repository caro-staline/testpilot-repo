package com.testpilot.service;

import org.springframework.ai.embedding.EmbeddingClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
public class EmbeddingService {
	private final WebClient webClient;

    public EmbeddingService(WebClient ollamaWebClient) {
        this.webClient = ollamaWebClient;
    }

    @SuppressWarnings("unchecked")
    public List<Double> generateEmbedding(String text) {
        Map<String, Object> request = Map.of(
                "model", "nomic-embed-text",
                "prompt", text
        );

//        Map response = webClient.post()
//                .uri("/api/embeddings")
//                .bodyValue(request)
//                .retrieve()
//                .bodyToMono(Map.class)
//                .block();
//       return (List<Double>) response.get("embedding");
        
        Map<String, Object> response = webClient.post()
                .uri("/api/embeddings")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();
        
        if (response == null || !response.containsKey("embedding")) {
            throw new IllegalStateException("No embedding returned from Ollama");
        }
       
        return (List<Double>) response.get("embedding");
        
    }
    
}
