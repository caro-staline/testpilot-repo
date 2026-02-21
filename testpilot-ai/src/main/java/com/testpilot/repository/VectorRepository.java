package com.testpilot.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Repository for managing vector-based storage and retrieval in PostgreSQL
 * using pgvector.
 * Handles document metadata and chunked text embeddings.
 */
@Repository
public class VectorRepository {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Constructor for VectorRepository.
     * 
     * @param jdbcTemplate Spring JDBC Template for database interaction.
     */
    public VectorRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Saves document metadata and returns the generated primary key.
     * 
     * @param filename Name of the uploaded file.
     * @return The unique ID of the saved document.
     */
    public Long saveDocument(String filename) {
        String sql = "INSERT INTO documents (filename) VALUES (?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        // Use KeyHolder to retrieve the auto-generated ID after insertion
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, filename);
            return ps;
        }, keyHolder);

        return (Long) keyHolder.getKeys().get("id");
    }

    /**
     * Persists multiple text chunks and their embeddings in a single batch
     * operation.
     * 
     * @param documentId Reference to the parent document.
     * @param chunks     List of text strings.
     * @param embeddings List of corresponding vector embeddings.
     */
    public void batchSaveChunks(Long documentId, List<String> chunks, List<List<Double>> embeddings) {
        // SQL query with a cast to the 'vector' type for the embedding column
        String sql = """
                    INSERT INTO document_chunks (document_id, chunk_index, content, embedding)
                    VALUES (?, ?, ?, ?::vector)
                """;

        jdbcTemplate.batchUpdate(sql, new org.springframework.jdbc.core.BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws java.sql.SQLException {
                ps.setLong(1, documentId);
                ps.setInt(2, i);
                ps.setString(3, chunks.get(i));
                // Convert List<Double> to a string format compatible with pgvector (e.g.,
                // "[0.1, 0.2, ...]")
                ps.setString(4, toPgVector(embeddings.get(i)));
            }

            @Override
            public int getBatchSize() {
                return chunks.size();
            }
        });
    }

    /**
     * Converts a list of doubles into a PostgreSQL vector string representation.
     * 
     * @param embedding The vector data.
     * @return Formatted string "[val1,val2,...]".
     */
    private String toPgVector(List<Double> embedding) {
        return "[" + embedding.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(",")) + "]";
    }

    /**
     * Performs a similarity search using vector distance calculation.
     * 
     * @param queryEmbedding Vector representing the search query.
     * @param limit          Maximum number of results to return.
     * @return List of content strings from the most similar chunks.
     */
    public List<String> findSimilarContent(List<Double> queryEmbedding, int limit) {
        // The <-> operator in pgvector performs Euclidean distance calculation
        String sql = """
                    SELECT content
                    FROM document_chunks
                    ORDER BY embedding <-> ?::vector
                    LIMIT ?
                """;

        String vector = toPgVector(queryEmbedding);

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> rs.getString("content"),
                vector,
                limit);
    }
}
