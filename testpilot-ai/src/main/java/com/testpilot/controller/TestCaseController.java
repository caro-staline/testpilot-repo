package com.testpilot.controller;

import com.testpilot.model.TestCase;
import com.testpilot.model.TestCaseRequest;
import com.testpilot.service.EmbeddingService;
import com.testpilot.service.LlmService;
import com.testpilot.service.OcrService;
import com.testpilot.util.FileUtil;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

/**
 * REST Controller for test case generation operations.
 * Provides endpoints for generating test cases from various input formats
 * (JSON, Text, Image).
 */
@RestController
@RequestMapping("/api")
public class TestCaseController {

    private final LlmService service;
    private final OcrService ocrService;
    private final EmbeddingService embeddingService;

    /**
     * Constructor for TestCaseController.
     * 
     * @param ocrService       Service for Optical Character Recognition.
     * @param service          Service for interacting with LLM for test case
     *                         generation.
     * @param embeddingService Service for generating vector embeddings.
     */
    public TestCaseController(OcrService ocrService, LlmService service, EmbeddingService embeddingService) {
        this.ocrService = ocrService;
        this.service = service;
        this.embeddingService = embeddingService;
    }

    /**
     * Generates test cases from a JSON-style user story request.
     * 
     * @param request Object containing the user story string.
     * @return List of generated TestCase objects.
     * @throws Exception if generation fails.
     */
    @PostMapping("/generatetestcase/json")
    public List<TestCase> generateFromJson(@RequestBody TestCaseRequest request) throws Exception {

        return service.generateTestCasesFromJson(request.getUserStory());
    }

    /**
     * Generates test cases from plain text input.
     * 
     * @param userStory The user story in plain text.
     * @return List of generated TestCase objects.
     * @throws Exception if generation fails.
     */
    @PostMapping(value = "/generatetestcase/text", consumes = "text/plain")
    public List<TestCase> generateFromText(@RequestBody String userStory) throws Exception {
        return service.generateTestCasesFromText(userStory);
    }

    /**
     * Generates test cases using a combination of a user story and a UI screenshot.
     * Performs OCR on the screenshot and uses the extracted text as additional
     * context.
     * 
     * @param userStory  The user story string.
     * @param screenshot The image file of the UI.
     * @return List of generated TestCase objects.
     * @throws Exception if generation fails.
     */
    @PostMapping(value = "/generatetestcase/multimodel", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public List<TestCase> generateFromImage(
            @RequestPart("userStory") String userStory,
            @RequestPart("screenshot") MultipartFile screenshot) throws Exception {
        // 1. Convert MultipartFile to a temporary local File for processing
        File imageFile = FileUtil.toFile(screenshot);

        // 2. Extract text from the image using OCR (Tesseract)
        String ocrText = ocrService.extractText(imageFile);

        // 3. Generate test cases using both the User Story and the UI Text
        return service.generateTestCasesFromJson(userStory, ocrText);
    }

    /**
     * Endpoint to test functionality of the embedding service.
     * 
     * @param text The text to be embedded.
     * @return String representation of the generated vector.
     */
    @GetMapping("/get-embedding")
    public String testEmbedding(@RequestBody String text) {
        List<Double> vector = embeddingService.generateEmbedding(text);
        return "Vector : \n" + vector;
    }

}
