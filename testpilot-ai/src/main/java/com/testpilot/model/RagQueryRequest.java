package com.testpilot.model;

/**
 * Data Transfer Object (DTO) for RAG similarity search queries.
 */
public class RagQueryRequest {

    private String query;

    /**
     * @return The query string.
     */
    public String getQuery() {
        return query;
    }

    /**
     * @param query The search query to set.
     */
    public void setQuery(String query) {
        this.query = query;
    }
}
