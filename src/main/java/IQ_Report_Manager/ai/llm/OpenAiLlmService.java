package IQ_Report_Manager.ai.llm;

//Implementation using OpenAI models.
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

/**
 * OpenAI implementation of LlmService.
 */
@Slf4j
@Service
@RequiredArgsConstructor

public class OpenAiLlmService {
    private final ChatClient chatClient;

    @Override
    public String generate(String systemPrompt, String userPrompt) {
        try {
            String response = chatClient.prompt()
                    .system(systemPrompt)
                    .user(userPrompt)
                    .call()
                    .content();

            return response == null ? "" : response.trim();

        } catch (Exception ex) {
            log.error("Error while calling OpenAI", ex);
            throw new RuntimeException("Failed to generate AI response", ex);
        }
    }
}
