package com.testpilot.rag.ingestion;

import java.io.Reader;
import java.io.StringReader;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.testpilot.rag.chunking.PdfTextChunker;
//import com.testpilot.rag.chunking.TextChunker;
import com.testpilot.repository.VectorRepository;
import com.testpilot.service.EmbeddingService;

@Service
public class PdfRagIngestionService {

    private final PdfTextChunker chunker;
    private final EmbeddingService embeddingService;
    private final VectorRepository vectorRepository;

    public PdfRagIngestionService(
            PdfTextChunker chunker,
            EmbeddingService embeddingService,
            VectorRepository vectorRepository
    ) {
        this.chunker = chunker;
        this.embeddingService = embeddingService;
        this.vectorRepository = vectorRepository;
    }

    public void ingestPdf(MultipartFile file, String sourceId) {

        try (PDDocument document = PDDocument.load(file.getInputStream())) {

            PDFTextStripper stripper = new PDFTextStripper();
            int totalPages = document.getNumberOfPages();

            for (int page = 1; page <= totalPages; page++) {

                stripper.setStartPage(page);
                stripper.setEndPage(page);

                try (Reader reader = new StringReader(stripper.getText(document))) {

                    List<String> chunks = chunker.chunk(reader);
                    int index = 0;

                    for (String chunk : chunks) {
                        List<Double> embedding = embeddingService.generateEmbedding(chunk);

                        vectorRepository.saveChunk(
                                sourceId,
                                "PDF",
                                sourceId,
                                page * 1000 + index,
                                chunk,
                                embedding
                        );
                        index++;
                    }
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("PDF ingestion failed", e);
        }
    }
}

