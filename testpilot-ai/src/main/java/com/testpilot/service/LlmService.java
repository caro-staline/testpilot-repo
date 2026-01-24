package com.testpilot.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import com.testpilot.model.TestCase;
import com.testpilot.util.FileUtil;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.List;
import java.util.Map;

@Service
public class LlmService {

	private final WebClient webClient;
	private final ObjectMapper mapper = new ObjectMapper();

	public LlmService() {
		this.webClient = WebClient.builder().baseUrl("http://localhost:11434").build();
	}

	public List<TestCase> generateTestCasesFromJson(String userStory) throws Exception {

//        String prompt = """
//        You are a Senior QA who generates test cases with better coverage.
//
//        Return ONLY a valid JSON array.
//        Do NOT include explanations, markdown, backticks, or extra text.
//        Do NOT wrap the JSON in quotes.
//        Do NOT escape characters.
//
//        The JSON MUST start with '[' and end with ']'.
//
//        Each test case MUST follow this structure exactly:
//        [
//          {
//            "id": "",
//            "scenario": "string",
//            "title": "string",
//            "preconditions": ["string"],
//            "steps": ["string"],
//            "expectedResult": "string"
//          }
//        ]        
//       
//        
//        IMPORTANT:
//        - Do NOT generate the "id" field.
//        - Leave "id" as an empty string ""
//		- "steps" MUST be an array of plain strings
//		- Do NOT use objects inside "steps"		  
//		- scenario - a little elobrate description of the context for which testcases are written
//		
//		CRITICAL RULES:
//		- You MUST close every object with '}'
//		- You MUST close the array with ']'
//		- Do NOT stop mid-response
//		- Ensure the JSON is syntactically COMPLETE and VALID
//		- If unsure, generate FEWER test cases instead of truncating
//		
//        Generate test cases for the following user story including negative scenario:
//        """ + userStory;

//		String prompt = """
//					You are a Senior QA Engineer.
//
//					TASK:
//					Generate test cases with strong positive and negative coverage.
//
//					OUTPUT RULES (MANDATORY):
//				- Return ONLY a valid JSON array
//					- No explanations, markdown, backticks, or extra text
//					- Do NOT wrap JSON in quotes
//					- JSON must be syntactically complete and valid
//				  		- JSON MUST start with '[' and end with ']'.
//
//				FORMAT:
//					[
//					  {
//					    "id": "",
//					    "scenario": "string",
//					    "title": "string",
//				    "preconditions": ["string"],
//					    "steps": ["string"],
//					    "expectedResult": "string"
//					  }
//				]
//
//					FIELD RULES:
//					- "id" MUST be an empty string ""
//					- "steps" MUST be an array of plain strings (no objects)
//					- "scenario" MUST describe the context in slightly more detail
//
//
//					QUALITY RULES:
//				- Include both positive and negative test cases
//					- Prefer clarity and correctness over quantity
//				- If unsure, generate fewer test cases instead of incomplete output
//
//				Before responding, mentally validate the JSON structure and completeness.
//					Generate test cases for the following user story:
//				""" + userStory;
		
		String prompt = """
				You are a Senior QA engineer.

				Generate test cases using:
				1) the user story
				2) UI text extracted from a screenshot

				Rules:
				- Output ONLY a valid JSON array
				- No explanations or markdown
				- JSON must start with '[' and end with ']'

				Each item structure:
				{
				  "id": "",
				  "scenario": "string",
				  "title": "string",
				  "preconditions": ["string"],
				  "steps": ["string"],
				  "expectedResult": "string"
				}

				Constraints:
				- Do NOT generate id values
				- steps must be plain strings
				- Cover positive and negative scenarios

				Input:
				""" + userStory;


		Map<String, Object> request = Map.of("model", "llama3", "prompt", prompt, "stream", false);

		// Call Ollama
		String rawResponse = webClient.post().uri("/api/generate").bodyValue(request).retrieve()
				.bodyToMono(String.class).block();

		// DEBUG (recommended during development)
		// System.out.println("RAW OLLAMA RESPONSE:\n" + rawResponse);

		// Extract the "response" field from Ollama JSON
		String llmOutput = mapper.readTree(rawResponse).path("response").asText().trim();

		// Guard clause: ensure RAW JSON
//		if (!llmOutput.startsWith("[") || !llmOutput.endsWith("]")) {
//			throw new IllegalStateException("LLM did not return raw JSON array:\n" + llmOutput);
//		}

//		List<TestCase> testCases = mapper.readValue(llmOutput, new TypeReference<List<TestCase>>() {
//		});

		List<TestCase> testCases;
		try {
			testCases = mapper.readValue(llmOutput, new TypeReference<List<TestCase>>() {
			});
		} catch (Exception e) {
			throw new IllegalStateException("Invalid JSON returned by LLM:\n" + llmOutput, e);
		}

		// Generate deterministic IDs

		int counter = 1;
		for (TestCase tc : testCases) {
			tc.setId(generateTestCaseId(counter++));
		}

		return testCases;

	}

	public List<TestCase> generateTestCasesFromText(String userStory) throws Exception {

		return generateTestCasesFromJson(convertUserstoryTextToJson(userStory));
	}

	// Convert User story in plain text to Json format
	private String convertUserstoryTextToJson(String userStory) throws Exception {

		ObjectMapper mapper = new ObjectMapper();

		Map<String, String> jsonMap = Map.of("userStory", userStory);

		String formattedUserstory = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonMap);

		return formattedUserstory;
	}

	// Centralized ID generation logic
	private String generateTestCaseId(int index) {
		return String.format("Testcase-%03d", index);
	}
	
	

}
