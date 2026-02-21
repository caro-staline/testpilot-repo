package com.testpilot.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.testpilot.model.RagQueryRequest;
import com.testpilot.rag.ingestion.PdfRagIngestionService;
import com.testpilot.repository.VectorRepository;
import com.testpilot.service.EmbeddingService;
import com.testpilot.rag.retrieval.RerankingService;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestPart;

@RestController
@RequestMapping("/rag")
public class RagController {

    private final PdfRagIngestionService ingestionService;
    private final EmbeddingService embeddingService;
    private final VectorRepository vectorRepository;

    private final RerankingService rerankingService;

    public RagController(PdfRagIngestionService ingestionService,
            EmbeddingService embeddingService,
            VectorRepository vectorRepository,
            RerankingService rerankingService) {
        this.ingestionService = ingestionService;
        this.embeddingService = embeddingService;
        this.vectorRepository = vectorRepository;
        this.rerankingService = rerankingService;
    }

    @PostMapping(value = "/upload/pdf", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadPdf(
            @RequestPart("file") MultipartFile file,
            @RequestPart("sourceId") String sourceId) {
        ingestionService.ingestPdf(file, sourceId);
        return ResponseEntity.ok("PDF ingested into RAG successfully");
    }

    // **** below one for testing purpose only ******
    @PostMapping("/getchunks")
    public ResponseEntity<List<String>> getRelavantChunks(
            @RequestBody RagQueryRequest request) {

        // 1. Embed query
        List<Double> queryEmbedding = embeddingService.generateEmbedding(request.getQuery());

        // 2. Retrieve similar chunks (fetch more candidates for reranking)
        List<String> results = vectorRepository.findSimilarContent(queryEmbedding, 20);

        // 3. Rerank
        List<String> rerankedResults = rerankingService.rerank(request.getQuery(), results, 5);

        return ResponseEntity.ok(rerankedResults);
    }
    // ****

}
