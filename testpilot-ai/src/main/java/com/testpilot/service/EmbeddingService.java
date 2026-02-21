package com.testpilot.service;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

/**
 * Service for generating vector embeddings from text strings using the Ollama
 * API.
 */
@Service
public class EmbeddingService {
    private final WebClient webClient;

    /**
     * Constructor for EmbeddingService.
     * 
     * @param ollamaWebClient WebClient configured for Ollama.
     */
    public EmbeddingService(WebClient ollamaWebClient) {
        this.webClient = ollamaWebClient;
    }

    /**
     * Generates a vector embedding for a single text string.
     * 
     * @param text The input text.
     * @return A list of doubles representing the vector embedding.
     * @throws IllegalStateException if the API response is invalid.
     */
    @SuppressWarnings("unchecked")
    public List<Double> generateEmbedding(String text) {
        // Prepare the request for the Ollama embeddings endpoint
        Map<String, Object> request = Map.of(
                "model", "nomic-embed-text",
                "prompt", text);

        // Execute synchronous POST request
        Map<String, Object> response = webClient.post()
                .uri("/api/embeddings")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                })
                .block();

        // Validate the response
        if (response == null || !response.containsKey("embedding")) {
            throw new IllegalStateException("No embedding returned from Ollama");
        }

        return (List<Double>) response.get("embedding");
    }

    /**
     * Generates vector embeddings for a list of text strings in parallel.
     * 
     * @param texts List of input strings.
     * @return List of vector embeddings.
     */
    public List<List<Double>> generateEmbeddings(List<String> texts) {
        return texts.stream()
                .parallel() // Utilize multiple threads for concurrent API calls
                .map(this::generateEmbedding)
                .toList();
    }
}
