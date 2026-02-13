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

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestPart;

@RestController
@RequestMapping("/rag")
public class RagController {

    private final PdfRagIngestionService ingestionService;
    private final EmbeddingService embeddingService;
    private final VectorRepository vectorRepository;
    
    public RagController(PdfRagIngestionService ingestionService,
    		EmbeddingService embeddingService,
            VectorRepository vectorRepository) {
        this.ingestionService = ingestionService;
        this.embeddingService = embeddingService;
        this.vectorRepository = vectorRepository;
    }

    
    @PostMapping(
    	    value = "/upload/pdf",
    	    consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    	)
    	public ResponseEntity<String> uploadPdf(
    	        @RequestPart("file") MultipartFile file,
    	        @RequestPart("sourceId") String sourceId
    	) {
    	    ingestionService.ingestPdf(file, sourceId);
    	    return ResponseEntity.ok("PDF ingested into RAG successfully");
    	}
    
    // **** below one for testing purpose only ****** 
    @PostMapping("/getchunks")
    public ResponseEntity<List<String>> getRelavantChunks(
            @RequestBody RagQueryRequest request
    ) {
        
    	// 1. Embed query
        List<Double> queryEmbedding =
                embeddingService.generateEmbedding(request.getQuery());

        // 2. Retrieve similar chunks
        List<String> results = vectorRepository.findSimilarContent(queryEmbedding, 5);

        return ResponseEntity.ok(results);
    }
    // **** 
    
}
