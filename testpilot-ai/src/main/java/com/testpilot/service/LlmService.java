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
import com.testpilot.repository.VectorRepository;
import com.testpilot.rag.retrieval.RerankingService;

@Service
public class LlmService {

	private final WebClient webClient;
	private final ObjectMapper mapper = new ObjectMapper();
	private final VectorRepository vectorRepository;
	private final EmbeddingService embeddingService;
	private final RerankingService rerankingService;

	// Constructor
	public LlmService(
			WebClient ollamaWebClient,
			EmbeddingService embeddingService,
			VectorRepository vectorRepository,
			RerankingService rerankingService) {
		this.webClient = ollamaWebClient;
		this.embeddingService = embeddingService;
		this.vectorRepository = vectorRepository;
		this.rerankingService = rerankingService;
	}

	private String buildRagPrompt(String userStory, String ocrText) {

		// 1️. Generate embedding for current input
		String combinedInput = userStory +
				(ocrText != null && !ocrText.isBlank() ? "\n" + ocrText : "");

		List<Double> queryVector = embeddingService.generateEmbedding(combinedInput);

		// 2️. Retrieve similar past context
		List<String> retrievedContext = vectorRepository.findSimilarContent(queryVector, 20);

		// 3. Rerank
		List<String> rerankedResults = rerankingService.rerank(userStory, retrievedContext, 5);

		// 4. Prepare context for LLM
		String contextBlock = rerankedResults.isEmpty()
				? "No prior relevant context available."
				: String.join("\n---\n", rerankedResults);

		// ️5. Build final RAG prompt (YOUR PROMPT + CONTEXT)
		return """
				You are a Senior QA engineer.

				CONTEXT (previously seen requirements, UI behavior, or test cases):
				%s

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
				- Use retrieved context ONLY if it is clearly relevant. Ignore any unrelated information.

				USER STORY:
				%s

				UI TEXT (if available):
				%s
				""".formatted(
				contextBlock,
				userStory,
				ocrText != null ? ocrText : "N/A");
	}

	public List<TestCase> generateTestCasesFromJson(String userStory) throws Exception {
		return generateTestCasesFromJson(userStory, null);
	}

	// Function Used for all types of input - test case conversion
	public List<TestCase> generateTestCasesFromJson(String userStory, String ocrText) throws Exception {

		// 1.Bulid prompt
		String prompt = buildRagPrompt(userStory, ocrText);
		// Map<String, Object> request = Map.of("model", "llama3", "prompt", prompt,
		// "stream", false);
		// To handle truncation when more volume of data is generated - "num_ctx", 8192,
		// "num_predict", 2048
		Map<String, Object> request = Map.of(
				"model", "llama3",
				"prompt", prompt,
				"stream", false,
				"options", Map.of(
						"num_ctx", 8192,
						"num_predict", 2048));

		// 2.Call Ollama
		String rawResponse = webClient.post().uri("/api/generate").bodyValue(request).retrieve()
				.bodyToMono(String.class).block();
		// DEBUG purpose
		// System.out.println("RAW OLLAMA RESPONSE:\n" + rawResponse);

		// 3.Extract the "response" field from Ollama JSON
		String llmOutput = mapper.readTree(rawResponse).path("response").asText().trim();

		// 4. Guard clause: ensure RAW JSON
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
		// Store New Knowledge After Generation
		// After parsing test cases, store user input so future calls get smarter.
		// List<Double> embedding = embeddingService.generateEmbedding(userStory);
		// vectorRepository.saveEmbedding(
		// "USER_STORY",
		// userStory,
		// embedding
		// );
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
