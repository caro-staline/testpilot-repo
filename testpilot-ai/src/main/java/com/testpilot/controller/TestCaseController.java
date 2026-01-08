package com.testpilot.controller;

import com.testpilot.model.TestCase;
import com.testpilot.model.TestCaseRequest;
import com.testpilot.service.LlmService;
import com.testpilot.util.ExcelWriter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/testcases")
public class TestCaseController {

    private final LlmService service;

    public TestCaseController(LlmService service) {
        this.service = service;
    }

    @PostMapping("/json")
    public List<TestCase> generateFromJson(@RequestBody TestCaseRequest request) throws Exception {
        return service.generateTestCasesFromJson(request.getUserStory());
    }
    
    @PostMapping(value = "/text", consumes = "text/plain")
    public List<TestCase> generateFromText(@RequestBody String userStory) throws Exception { 
        return service.generateTestCasesFromText(userStory);
    } 

    @PostMapping("/excel")
    public ResponseEntity<byte[]> generateExcel(@RequestBody TestCaseRequest request) throws Exception {
        List<TestCase> cases = service.generateTestCasesFromJson(request.getUserStory());
        byte[] excel = ExcelWriter.write(cases);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=testcases.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(excel);
    }
}
