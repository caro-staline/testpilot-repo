# TestPilot AI: Next-Gen Test Case Generation
## Demo Presentation Outline

---

### Slide 1: Title Slide
**Title**: TestPilot AI
**Subtitle**: Grounded & Reliable Test Case Generation with RAG and Multimodal AI
**Presenter**: [Your Name/Team]
**Visual**: Modern, tech-focused background (refer to the GUI aesthetics).

---

### Slide 2: The Challenge
**Title**: The Industry Pain Point
**Content**:
- **Manual Toil**: Writing test cases from complex requirements is slow and error-prone.
- **Context Gap**: Standard LLMs often hallucinate or miss project-specific constraints.
- **Multimodal Gap**: UI-heavy features are hard to describe with text alone.

---

### Slide 3: Our Solution
**Title**: TestPilot AI Architecture
**Content**:
- **Grounded AI**: Uses Retrieval-Augmented Generation (RAG) to anchor test cases in *actual* project documentation.
- **Multimodal Support**: Understands both User Stories (text) and UI Screenshots (vision).
- **Automation Ready**: Results are verified, structured, and ready for Excel export.

---

### Slide 4: Core Capabilities - RAG
**Title**: RAG-Powered Intelligence
**Content**:
- **Knowledge Ingestion**: Upload PDFs (Technical Specs, Design Docs).
- **Semantic Search**: Uses `pgvector` and `Ollama` to find relevant context shards.
- **Accuracy**: Eliminates hallucinations by providing the LLM with "Ground Truth" data.

---

### Slide 5: Core Capabilities - Multimodal
**Title**: Seeing the Feature
**Content**:
- **Text + Image**: Upload a UI screenshot alongside a user story.
- **OCR & Vision**: TestPilot extracts UI elements and verifies alignment with the requirements.
- **Complex Scenarios**: Generates edge cases based on visible buttons, fields, and layouts.

---

### Slide 6: Demo Walkthrough
**Title**: Live Demo: The Workflow
**Content**:
1. **Ingest**: Dashboard shows existing documentation repository.
2. **Generate**: Input a User Story and (optional) UI Screenshot.
3. **Audit**: Inspect the "Chunk Audit" to see exactly which document shards were used.
4. **Finalize**: View results and Export to Excel for Jira/TestRail.

---

### Slide 7: Technical Stack
**Title**: Built for Performance
**Content**:
- **Frontend**: NextJS 14+, Tailwind CSS v4, Glassmorphism UI.
- **Backend**: Spring Boot, Java 21, Tesseract OCR.
- **Database**: PostgreSQL with `pgvector` for high-dimensional embeddings.
- **Models**: State-of-the-art LLMs via Ollama.

---

### Slide 8: Future Roadmap
**Title**: Where We Are Headed
**Content**:
- **Direct Integration**: Export to Jira, Azure DevOps, and TestRail.
- **Auto-Update**: Automatically re-generate test cases when PDFs are updated.
- **Code Generation**: Transform test cases into Playwright or Selenium scripts.

---

### Slide 9: Conclusion
**Title**: Thank You!
**Content**:
- **Questions?**
- **Website**: http://localhost:3000
- **Build Quality**: From hours of work to seconds of generation.
