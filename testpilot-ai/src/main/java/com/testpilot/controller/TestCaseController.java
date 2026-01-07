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
    public List<TestCase> generateJson(@RequestBody TestCaseRequest request) throws Exception {
        return service.generateTestCases(request.getUserStory());
    }

    @PostMapping("/excel")
    public ResponseEntity<byte[]> generateExcel(@RequestBody TestCaseRequest request) throws Exception {
        List<TestCase> cases = service.generateTestCases(request.getUserStory());
        byte[] excel = ExcelWriter.write(cases);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=testcases.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(excel);
    }
}
