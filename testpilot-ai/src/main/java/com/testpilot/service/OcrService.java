package com.testpilot.service;

import net.sourceforge.tess4j.Tesseract;
import org.springframework.stereotype.Service;

import java.io.File;

/**
 * Service for extracting text from images using the Tesseract OCR engine.
 */
@Service
public class OcrService {

    private final Tesseract tesseract;

    /**
     * Initializes Tesseract with specific data path and language settings.
     */
    public OcrService() {
        tesseract = new Tesseract();
        // Path to Tesseract training data on the local system
        tesseract.setDatapath("C:/Program Files/Tesseract-OCR/tessdata");
        tesseract.setLanguage("eng");
    }

    /**
     * Extracts and cleans text from the provided image file.
     * 
     * @param imageFile The image to process.
     * @return Cleaned, extracted text.
     * @throws Exception if OCR processing fails.
     */
    public String extractText(File imageFile) throws Exception {
        String text = tesseract.doOCR(imageFile);
        return clean(text);
    }

    /**
     * Post-processes noisy OCR output by removing non-printable characters and
     * normalizing whitespace.
     * 
     * @param text Raw OCR output.
     * @return Sanitized text.
     */
    private String clean(String text) {
        return text
                .replaceAll("[^\\x20-\\x7E]", " ") // Remove non-ASCII/non-printable characters
                .replaceAll("\\s+", " ") // Collapse multiple spaces into one
                .trim();
    }
}
