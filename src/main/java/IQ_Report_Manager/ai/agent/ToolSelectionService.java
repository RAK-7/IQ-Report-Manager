package IQ_Report_Manager.ai.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import IQ_Report_Manager.ai.dto.ToolSelectionResponse;
import IQ_Report_Manager.ai.llm.LlmService;
import IQ_Report_Manager.ai.prompt.PromptTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Uses AI to select tools dynamically.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ToolSelectionService {

    private final LlmService llmService;

    private final ObjectMapper objectMapper;

    /**
     * Uses LLM to determine:
     * - which tool to execute
     * - which parameters to extract
     */
    public ToolSelectionResponse selectTool(
            String userMessage
    ) {

        try {

            String response =
                    llmService.generate(
                            PromptTemplate.TOOL_SELECTION_PROMPT,
                            userMessage
                    );

            log.info(
                    "AI Tool Selection Response: {}",
                    response
            );

            return objectMapper.readValue(
                    response,
                    ToolSelectionResponse.class
            );

        } catch (Exception ex) {

            log.error(
                    "Failed to parse AI tool selection",
                    ex
            );

            throw new RuntimeException(
                    "Tool selection failed",
                    ex
            );
        }
    }
}