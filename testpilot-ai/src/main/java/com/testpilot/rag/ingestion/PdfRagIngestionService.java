package com.testpilot.rag.ingestion;

import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.testpilot.rag.chunking.RecursiveTextChunker;
import com.testpilot.repository.VectorRepository;
import com.testpilot.service.EmbeddingService;

@Service
public class PdfRagIngestionService {

    private final RecursiveTextChunker chunker;
    private final EmbeddingService embeddingService;
    private final VectorRepository vectorRepository;

    public PdfRagIngestionService(
            RecursiveTextChunker chunker,
            EmbeddingService embeddingService,
            VectorRepository vectorRepository) {
        this.chunker = chunker;
        this.embeddingService = embeddingService;
        this.vectorRepository = vectorRepository;
    }

    public void ingestPdf(MultipartFile file, String sourceId) {

        try (PDDocument document = PDDocument.load(file.getInputStream())) {

            // 1. Save Document Metadata
            Long docId = vectorRepository.saveDocument(file.getOriginalFilename());

            PDFTextStripper stripper = new PDFTextStripper();
            String fullText = stripper.getText(document);

            // 2. recursive chunking
            List<String> chunks = chunker.chunk(fullText);

            // 3. Generate embeddings in batch
            List<List<Double>> embeddings = embeddingService.generateEmbeddings(chunks);

            // 4. Batch Save
            vectorRepository.batchSaveChunks(docId, chunks, embeddings);

        } catch (Exception e) {
            throw new RuntimeException("PDF ingestion failed", e);
        }
    }
}
