package com.testpilot.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class VectorRepository {

    private final JdbcTemplate jdbcTemplate;

    public VectorRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Long saveDocument(String filename) {
        String sql = "INSERT INTO documents (filename) VALUES (?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, filename);
            return ps;
        }, keyHolder);

        return (Long) keyHolder.getKeys().get("id");
    }

    public void batchSaveChunks(Long documentId, List<String> chunks, List<List<Double>> embeddings) {
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
                ps.setString(4, toPgVector(embeddings.get(i)));
            }

            @Override
            public int getBatchSize() {
                return chunks.size();
            }
        });
    }

    private String toPgVector(List<Double> embedding) {
        return "[" + embedding.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(",")) + "]";
    }

    public List<String> findSimilarContent(List<Double> queryEmbedding, int limit) {
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
