package IQ_Report_Manager.ai.llm;

//Common interface for all model providers.
/**
 * Abstraction over any Large Language Model provider.
 */

public interface LlmService {
    /**
     * Sends a system prompt and user prompt to the LLM.
     *
     * @param systemPrompt instructions defining assistant behavior
     * @param userPrompt user input
     * @return model response
     */
    String generate(String systemPrompt, String userPrompt);
}
