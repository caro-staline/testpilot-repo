package com.testpilot.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.testpilot.model.TestCase;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.List;
import java.util.Map;
import com.testpilot.repository.VectorRepository;
import com.testpilot.rag.retrieval.RerankingService;

/**
 * Service for interacting with Large Language Models (LLMs) to generate test
 * cases.
 * Utilizes Retrieval-Augmented Generation (RAG) by combining user input with
 * retrieved context.
 */
@Service
public class LlmService {

	private final WebClient webClient;
	private final ObjectMapper mapper = new ObjectMapper();
	private final VectorRepository vectorRepository;
	private final EmbeddingService embeddingService;
	private final RerankingService rerankingService;

	/**
	 * Constructor for LlmService.
	 * 
	 * @param ollamaWebClient  WebClient configured for Ollama API.
	 * @param embeddingService Service for vector embeddings.
	 * @param vectorRepository Repository for vector storage and search.
	 * @param rerankingService Service for refining search results.
	 */
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

	/**
	 * Constructs a prompt for the LLM using RAG techniques.
	 * 
	 * @param userStory The input user story.
	 * @param ocrText   Optional extracted UI text from a screenshot.
	 * @return A formatted prompt string containing relevant context and
	 *         instructions.
	 */
	private String buildRagPrompt(String userStory, String ocrText) {

		// 1. Generate a vector embedding for the current user input to enable
		// similarity search
		String combinedInput = userStory +
				(ocrText != null && !ocrText.isBlank() ? "\n" + ocrText : "");
		List<Double> queryVector = embeddingService.generateEmbedding(combinedInput);

		// 2. Retrieve the top 20 most similar past contexts from the vector repository
		List<String> retrievedContext = vectorRepository.findSimilarContent(queryVector, 10);

		// 3. Re-rank the retrieved results to find the most relevant top 5 segments
		List<String> rerankedResults = rerankingService.rerank(userStory, retrievedContext, 3);

		// 4. Aggregate the reranked results into a single context block
		String contextBlock = rerankedResults.isEmpty()
				? "No prior relevant context available."
				: String.join("\n---\n", rerankedResults);
		System.out.println("Retrived from DB: "+contextBlock);

		// 5. Build the final structured prompt for the LLM
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

	/**
	 * Generates test cases based on a user story (overloaded for simple input).
	 * 
	 * @param userStory The user story.
	 * @return List of TestCase objects.
	 * @throws Exception if generation fails.
	 */
	public List<TestCase> generateTestCasesFromJson(String userStory) throws Exception {
		return generateTestCasesFromJson(userStory, null);
	}

	/**
	 * Core function to generate test cases using LLM and combined inputs.
	 * 
	 * @param userStory The user story string.
	 * @param ocrText   Optional text extracted from UI images.
	 * @return List of TestCase objects.
	 * @throws Exception if generation fails.
	 */
	public List<TestCase> generateTestCasesFromJson(String userStory, String ocrText) throws Exception {

		// 1. Assemble the RAG-enhanced prompt
		String prompt = buildRagPrompt(userStory, ocrText);

		// 2. Prepare the request payload for the Ollama API
		// We use llama3 and specify options for context window and prediction length.
		Map<String, Object> request = Map.of(
				"model", "llama3",
				"prompt", prompt,
				"stream", false,
				"options", Map.of(
						"num_ctx", 8192,
						"num_predict", 2048));

		// 3. Synchronously call the Ollama API via WebClient
		String rawResponse = webClient.post().uri("/api/generate").bodyValue(request).retrieve()
				.bodyToMono(String.class).block();

		// 4. Parse the raw JSON response from Ollama to extract the LLM's text output
		String llmOutput = mapper.readTree(rawResponse).path("response").asText().trim();

		// 5. Parse the extracted LLM output (expected to be a JSON array) into a list
		// of TestCase objects
		List<TestCase> testCases;
		try {
			testCases = mapper.readValue(llmOutput, new TypeReference<List<TestCase>>() {
			});
		} catch (Exception e) {
			throw new IllegalStateException("Invalid JSON returned by LLM:\n" + llmOutput, e);
		}

		// 6. Enrich each test case with a generated unique ID
		int counter = 1;
		for (TestCase tc : testCases) {
			tc.setId(generateTestCaseId(counter++));
		}

		return testCases;
	}

	/**
	 * Generates test cases from an informal plain text user story by first
	 * converting it to a structured format.
	 * 
	 * @param userStory Informal text description.
	 * @return List of generated TestCase objects.
	 * @throws Exception if generation fails.
	 */
	public List<TestCase> generateTestCasesFromText(String userStory) throws Exception {
		return generateTestCasesFromJson(convertUserstoryTextToJson(userStory));
	}

	/**
	 * Wraps a plain text user story into a simple JSON structure.
	 * 
	 * @param userStory The user story text.
	 * @return A JSON string containing the user story.
	 * @throws Exception if serialization fails.
	 */
	private String convertUserstoryTextToJson(String userStory) throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		Map<String, String> jsonMap = Map.of("userStory", userStory);
		String formattedUserstory = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonMap);
		return formattedUserstory;
	}

	/**
	 * Generates a formatted ID for a test case.
	 * 
	 * @param index The sequence number.
	 * @return A string ID (e.g., "Testcase-001").
	 */
	private String generateTestCaseId(int index) {
		return String.format("Testcase-%03d", index);
	}
}
