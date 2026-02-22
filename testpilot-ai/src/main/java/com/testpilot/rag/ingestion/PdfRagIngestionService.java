package com.testpilot.rag.ingestion;

import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.testpilot.rag.chunking.RecursiveTextChunker;
import com.testpilot.repository.VectorRepository;
import com.testpilot.service.EmbeddingService;

/**
 * Service for ingesting PDF documents into the RAG (Retrieval-Augmented
 * Generation) system.
 * Handles loading, text extraction, chunking, embedding, and storage.
 */
@Service
public class PdfRagIngestionService {

    private final RecursiveTextChunker chunker;
    private final EmbeddingService embeddingService;
    private final VectorRepository vectorRepository;

    /**
     * Constructor for PdfRagIngestionService.
     * 
     * @param chunker          Service for splitting text into manageable chunks.
     * @param embeddingService Service for generating vector embeddings.
     * @param vectorRepository Repository for database operations.
     */
    public PdfRagIngestionService(
            RecursiveTextChunker chunker,
            EmbeddingService embeddingService,
            VectorRepository vectorRepository) {
        this.chunker = chunker;
        this.embeddingService = embeddingService;
        this.vectorRepository = vectorRepository;
    }

    /**
     * Processes a PDF file and stores its content in the vector database.
     * 
     * @param file     The PDF file from a multipart request.
     */
    public void ingestPdf(MultipartFile file) {

        try (PDDocument document = PDDocument.load(file.getInputStream())) {

            // 1. Record document metadata in the database and get a unique document ID
            Long docId = vectorRepository.saveDocument(file.getOriginalFilename());

            // 2. Extract full text from the PDF document
            PDFTextStripper stripper = new PDFTextStripper();
            String fullText = stripper.getText(document);

            // 3. Split the full text into smaller, meaningful chunks for embedding
            // Recursive chunking tries to preserve semantic structure (paragraphs,
            // sentences).
            List<String> chunks = chunker.chunk(fullText);

            // 4. Generate vector embeddings for all text chunks in a batch
            List<List<Double>> embeddings = embeddingService.generateEmbeddings(chunks);

            // 5. Save chunks and their corresponding embeddings to the vector database
            vectorRepository.batchSaveChunks(docId, chunks, embeddings);

        } catch (Exception e) {
            throw new RuntimeException("PDF ingestion failed", e);
        }
    }
}
