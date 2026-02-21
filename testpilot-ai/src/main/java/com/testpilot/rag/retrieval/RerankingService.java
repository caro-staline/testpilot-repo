package com.testpilot.rag.retrieval;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for re-ranking search result candidates based on text relevance.
 * This compensates for initial vector search inaccuracies by performing
 * keyword-based scoring.
 */
@Service
public class RerankingService {

    /**
     * Reranks a list of candidate context segments based on their keyword overlap
     * with the original query.
     * This is a "lithweight" BM25-like scoring mechanism.
     * 
     * @param query      The search query string.
     * @param candidates List of retrieved content strings.
     * @param topN       Number of top results to return.
     * @return Refined list containing the most relevant segments.
     */
    public List<String> rerank(String query, List<String> candidates, int topN) {
        if (candidates == null || candidates.isEmpty()) {
            return new ArrayList<>();
        }

        // 1. Break down the query into its constituent unique terms (lowercased)
        Set<String> queryTerms = tokenize(query);

        // 2. Score each candidate based on term overlap
        Map<String, Double> scores = new HashMap<>();
        for (String candidate : candidates) {
            double score = calculateScore(candidate, queryTerms);
            scores.put(candidate, score);
        }

        // 3. Sort candidates by their score in descending order and limit to the
        // request size
        return candidates.stream()
                .sorted((c1, c2) -> Double.compare(scores.get(c2), scores.get(c1)))
                .limit(topN)
                .collect(Collectors.toList());
    }

    /**
     * Calculates a simple relevance score based on identical term matches.
     * 
     * @param text       The candidate text segment.
     * @param queryTerms Set of terms from the user query.
     * @return A numerical score (higher is better).
     */
    private double calculateScore(String text, Set<String> queryTerms) {
        Set<String> textTerms = tokenize(text);
        // Simple match count: how many query keywords appear in this segment?
        long matchCount = queryTerms.stream()
                .filter(textTerms::contains)
                .count();

        return (double) matchCount;
    }

    /**
     * Splits text into individual lowercased alphanumeric tokens.
     * 
     * @param text Input text.
     * @return Set of unique tokens.
     */
    private Set<String> tokenize(String text) {
        // Lowercase and split by non-word characters (\W)
        return Arrays.stream(text.toLowerCase().split("\\W+"))
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }
}
