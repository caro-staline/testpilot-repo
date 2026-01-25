package com.testpilot.controller;

import com.testpilot.model.TestCase;
import com.testpilot.model.TestCaseRequest;
import com.testpilot.service.EmbeddingService;
import com.testpilot.service.LlmService;
import com.testpilot.service.OcrService;
import com.testpilot.util.ExcelWriter;
import com.testpilot.util.FileUtil;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

@RestController
@RequestMapping("/api/testcases")
public class TestCaseController {

    private final LlmService service;
    private final OcrService ocrService; 
    private final EmbeddingService embeddingService;
   
    public TestCaseController(OcrService ocrService, LlmService service, EmbeddingService embeddingService) {
        this.ocrService = ocrService;
        this.service = service;
        this.embeddingService = embeddingService;
    }

    @PostMapping("/json")
    public List<TestCase> generateFromJson(@RequestBody TestCaseRequest request) throws Exception {
        return service.generateTestCasesFromJson(request.getUserStory());
    }
    
    @PostMapping(value = "/text", consumes = "text/plain")
    public List<TestCase> generateFromText(@RequestBody String userStory) throws Exception { 
        return service.generateTestCasesFromText(userStory);
    } 
    
    @PostMapping(value = "/generate", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    	public List<TestCase> generateFromImage(
    	        @RequestPart("userStory") String userStory,
    	        @RequestPart("screenshot") MultipartFile screenshot
    	) throws Exception {

    	    File imageFile = FileUtil.toFile(screenshot);

    	    String ocrText = ocrService.extractText(imageFile);

//    	    String combinedInput = """
//    	    USER STORY:
//    	    %s
//
//    	    UI TEXT (extracted from screenshot):
//    	    %s
//    	    """.formatted(userStory, ocrText);

    	    return service.generateTestCasesFromJson(userStory, ocrText);
    	}

//    @PostMapping("/excel")
//    public ResponseEntity<byte[]> generateExcel(@RequestBody TestCaseRequest request) throws Exception {
//        List<TestCase> cases = service.generateTestCasesFromJson(request.getUserStory());
//        byte[] excel = ExcelWriter.write(cases);
//
//        return ResponseEntity.ok()
//                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=testcases.xlsx")
//                .contentType(MediaType.APPLICATION_OCTET_STREAM)
//                .body(excel);
//    }
    
    
    @GetMapping("/embedding-test")
    public String testEmbedding(@RequestBody String text) {
        List<Double> vector = embeddingService.generateEmbedding(text);
        return "Vector : \n" + vector;
    }
    
}
