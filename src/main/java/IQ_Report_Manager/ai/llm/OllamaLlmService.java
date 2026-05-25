package IQ_Report_Manager.ai.llm;
//Implementation using Ollama models.

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

/**
 * Ollama implementation of LlmService.
 *
 * Supports:
 * - qwen2.5:7b
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class OllamaLlmService implements LlmService {

    private final ChatClient chatClient;

    /**
     * Sends prompts to Ollama model.
     *
     * @param systemPrompt system-level instructions
     * @param userPrompt user request
     * @return AI-generated response
     */
    @Override
    public String generate(String systemPrompt, String userPrompt) {

        try {

            log.info("Sending request to Ollama");

            String response = chatClient.prompt()
                    .system(systemPrompt)
                    .user(userPrompt)
                    .call()
                    .content();

            log.info("Received response from Ollama");

            return response == null
                    ? ""
                    : response.trim();

        } catch (Exception ex) {

            log.error("Error while calling Ollama", ex);

            throw new RuntimeException(
                    "Failed to generate AI response",
                    ex
            );
        }
    }
}