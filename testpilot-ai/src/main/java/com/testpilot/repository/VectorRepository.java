package com.testpilot.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

//@Repository
//public class VectorRepository {
//
//    private final JdbcTemplate jdbcTemplate;
//
//    public VectorRepository(JdbcTemplate jdbcTemplate) {
//        this.jdbcTemplate = jdbcTemplate;
//    }
//
//    public void saveEmbedding(String source, String content, List<Double> embedding) {
//
//        String sql = """
//            INSERT INTO document_embeddings (source, content, embedding)
//            VALUES (?, ?, ?::vector)
//        """;
//
//        String vectorString = embedding.toString(); // [0.12, 0.98, ...]
//
//        jdbcTemplate.update(sql, source, content, vectorString);
//    }
//
//    public List<String> findSimilarContent(List<Double> queryEmbedding, int limit) {
//
//        String sql = """
//            SELECT content
//            FROM document_embeddings
//            WHERE embedding <-> ?::vector < 0.35
//            ORDER BY embedding <-> ?::vector
//            LIMIT ?
//        """;
//
//        String pgVector = toPgVector(queryEmbedding);
//
//        return jdbcTemplate.query(
//            sql,
//            (rs, rowNum) -> rs.getString("content"),
//            pgVector,
//            pgVector,
//            limit
//        );
//    }
//    
//    private String toPgVector(List<Double> embedding) {
//        return embedding.stream()
//                .map(d -> String.format("%.6f", d))
//                .collect(Collectors.joining(",", "[", "]"));
//    }
//}

@Repository
public class VectorRepository {

    private final JdbcTemplate jdbcTemplate;

    public VectorRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void saveChunk(
            String source,
            String sourceType,
            String sourceId,
            int chunkIndex,
            String content,
            List<Double> embedding
    ) {

        String sql = """
            INSERT INTO document_embeddings
            (source, source_type, source_id, chunk_index, content, embedding)
            VALUES (?, ?, ?, ?, ?, ?::vector)
        """;

        jdbcTemplate.update(
            sql,
            source,
            sourceType,
            sourceId,
            chunkIndex,
            content,
            toPgVector(embedding)
        );
    }

    private String toPgVector(List<Double> embedding) {
        return "[" + embedding.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(",")) + "]";
    }
    
    public List<String> findSimilarContent(
            List<Double> queryEmbedding,
            int limit
    ) {
        String sql = """
            SELECT content
            FROM document_embeddings
            ORDER BY embedding <-> ?::vector
            LIMIT ?
        """;

        String vector = toPgVector(queryEmbedding);

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> rs.getString("content"),
                vector,
                limit
        );
    }

}


