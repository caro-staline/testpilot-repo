package com.testpilot.rag.retrieval;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RerankingService {

    /**
     * Reranks a list of content strings based on keyword overlap with the query.
     * This is a simplified "light" version of BM25/keyword scoring.
     */
    public List<String> rerank(String query, List<String> candidates, int topN) {
        if (candidates == null || candidates.isEmpty()) {
            return new ArrayList<>();
        }

        // Tokenize query
        Set<String> queryTerms = tokenize(query);

        // Score each candidate
        Map<String, Double> scores = new HashMap<>();
        for (String candidate : candidates) {
            double score = calculateScore(candidate, queryTerms);
            scores.put(candidate, score);
        }

        // Sort by score descending
        return candidates.stream()
                .sorted((c1, c2) -> Double.compare(scores.get(c2), scores.get(c1)))
                .limit(topN)
                .collect(Collectors.toList());
    }

    private double calculateScore(String text, Set<String> queryTerms) {
        Set<String> textTerms = tokenize(text);
        long matchCount = queryTerms.stream()
                .filter(textTerms::contains)
                .count();

        // Simple scoring: number of matched terms.
        // Could be improved with TF-IDF or actual BM25 if we had corpus stats.
        return (double) matchCount;
    }

    private Set<String> tokenize(String text) {
        // Lowercase and split by non-word characters
        return Arrays.stream(text.toLowerCase().split("\\W+"))
                .filter(s -> !s.isEmpty())
                // filter stop words if we had a list
                .collect(Collectors.toSet());
    }
}
