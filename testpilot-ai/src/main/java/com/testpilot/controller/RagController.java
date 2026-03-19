package com.testpilot.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.testpilot.model.Document;
import com.testpilot.model.DocumentChunk;
import com.testpilot.model.RagQueryRequest;
import com.testpilot.rag.ingestion.PdfRagIngestionService;
import com.testpilot.repository.VectorRepository;
import com.testpilot.service.EmbeddingService;
import com.testpilot.rag.retrieval.RerankingService;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * REST Controller for RAG (Retrieval-Augmented Generation) operations.
 * Handles PDF ingestion and similarity search queries.
 */
@RestController
@RequestMapping("/rag")
public class RagController {

    private final PdfRagIngestionService ingestionService;
    private final EmbeddingService embeddingService;
    private final VectorRepository vectorRepository;
    private final RerankingService rerankingService;

    /**
     * Constructor for RagController.
     * 
     * @param ingestionService Service for processing PDF files.
     * @param embeddingService Service for generating vector embeddings.
     * @param vectorRepository Repository for storing and retrieving vectors from
     *                         the database.
     * @param rerankingService Service for re-ranking search results.
     */
    public RagController(PdfRagIngestionService ingestionService,
            EmbeddingService embeddingService,
            VectorRepository vectorRepository,
            RerankingService rerankingService) {
        this.ingestionService = ingestionService;
        this.embeddingService = embeddingService;
        this.vectorRepository = vectorRepository;
        this.rerankingService = rerankingService;
    }

    /**
     * Endpoint to upload and ingest a PDF file into the RAG system.
     * 
     * @param file     The PDF file to be processed.
     * @param sourceId A unique identifier for the source of the document.
     * @return ResponseEntity with a success message.
     */
    @PostMapping(value = "/upload/pdf", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadPdf(
            @RequestPart("file") MultipartFile file) {
        // Delegate ingestion logic to the service
        ingestionService.ingestPdf(file);
        return ResponseEntity.ok("PDF ingested into RAG successfully");
    }

    /**
     * Endpoint for testing purposes to retrieve relevant chunks for a given query.
     * Performs embedding, similarity search, and reranking.
     * 
     * @param request The query request containing the search string.
     * @return List of reranked content strings.
     */
    @PostMapping("/getchunks")
    public ResponseEntity<List<String>> getRelavantChunks(
            @RequestBody RagQueryRequest request) {

        // 1. Convert user query text into a vector embedding
        List<Double> queryEmbedding = embeddingService.generateEmbedding(request.getQuery());

        // 2. Perform similarity search in the vector database to retrieve candidates
        // We fetch 20 candidates initially to allow for a better re-ranking phase.
        List<String> results = vectorRepository.findSimilarContent(queryEmbedding, 10);

        // 3. Re-rank the retrieved chunks based on relevance to the original query
        // This refines the initial vector search results.
        List<String> rerankedResults = rerankingService.rerank(request.getQuery(), results, 3);

        return ResponseEntity.ok(rerankedResults);
    }

    /**
     * Endpoint to retrieve all ingested documents.
     * 
     * @return List of Document metadata.
     */
    @GetMapping("/documents")
    public List<Document> listDocuments() {
        return vectorRepository.findAllDocuments();
    }

    /**
     * Endpoint to retrieve all chunks for a specific document.
     * 
     * @param id The ID of the document.
     * @return List of DocumentChunk objects.
     */
    @GetMapping("/documents/{id}/chunks")
    public List<DocumentChunk> listChunks(@PathVariable Long id) {
        return vectorRepository.findChunksByDocumentId(id);
    }
}
