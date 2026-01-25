package com.testpilot.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class VectorRepository {

    private final JdbcTemplate jdbcTemplate;

    public VectorRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void saveEmbedding(String source, String content, List<Double> embedding) {

        String sql = """
            INSERT INTO document_embeddings (source, content, embedding)
            VALUES (?, ?, ?::vector)
        """;

        String vectorString = embedding.toString(); // [0.12, 0.98, ...]

        jdbcTemplate.update(sql, source, content, vectorString);
    }

    public List<String> findSimilarContent(List<Double> queryEmbedding, int limit) {

        String sql = """
            SELECT content
            FROM document_embeddings
            ORDER BY embedding <-> ?::vector
            LIMIT ?
        """;

        String vectorString = queryEmbedding.toString();

        return jdbcTemplate.query(
            sql,
            (rs, rowNum) -> rs.getString("content"),
            vectorString,
            limit
        );
    }
}
