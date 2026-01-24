package com.testpilot.service;

import net.sourceforge.tess4j.Tesseract;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class OcrService {

    private final Tesseract tesseract;

    public OcrService() {
        tesseract = new Tesseract();
        tesseract.setDatapath("C:/Program Files/Tesseract-OCR/tessdata");
        tesseract.setLanguage("eng");
    }

    public String extractText(File imageFile) throws Exception {
        String text = tesseract.doOCR(imageFile);
        return clean(text);
    }

    // 🔹 Important: clean noisy OCR output
    private String clean(String text) {
        return text
                .replaceAll("[^\\x20-\\x7E]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}
