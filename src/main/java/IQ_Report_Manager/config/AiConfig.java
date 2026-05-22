package IQ_Report_Manager.config;

//Configures ChatClient, model beans, and prompts.

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Central AI configuration.
 *
 * Responsible for:
 * - Ollama model configuration
 * - ChatClient creation
 * - Future extensibility for multiple providers
 */
@Configuration
public class AiConfig {

    /**
     * Creates reusable ChatClient bean.
     *
     * @param ollamaChatModel injected by Spring AI
     * @return configured ChatClient
     */
    @Bean
    public ChatClient chatClient(OllamaChatModel ollamaChatModel) {

        return ChatClient.builder(ollamaChatModel)
                .build();
    }
}
