package IQ_Report_Manager.ai.memory.service;

//Stores and retrieves conversation context.


import IQ_Report_Manager.ai.executor.ReportExecutionResult;
import IQ_Report_Manager.ai.memory.MemoryContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import IQ_Report_Manager.ai.memory.model.ConversationMemory;
import IQ_Report_Manager.ai.memory.repository.ConversationMemoryRepository;

/**
 * In-memory conversation storage.
 *
 * Later:
 * - Redis
 * - MongoDB
 * - Vector DB
 * can replace this.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationMemoryService {

    private final ConversationMemoryRepository conversationMemoryRepository;

    public MemoryContext getContext(
            String conversationId
    ) {

        return memoryStore.get(
                conversationId
        );
    }

    /**
     * Save Last Report
     */
    public void saveLastReport(
            String conversationId,
            String reportName
    ) {

        conversationMemoryRepository.findByConversationId(
                        conversationId
                )
                .ifPresent(memory -> {

                    memory.setLastReportName(
                            reportName
                    );

                    conversationMemoryRepository.save(memory);
                });
    }

    /**
     * Save Execution Result
     */
    public void saveExecutionResult(
            String conversationId,
            ReportExecutionResult result
    ) {

        conversationMemoryRepository.findByConversationId(
                        conversationId
                )
                .ifPresent(memory -> {

                    memory.setLastExecutionResult(
                            result
                    );

                    conversationMemoryRepository.save(memory);
                });
    }

    /**
     * Temporary in-memory storage.
     */
    private final Map<String, MemoryContext> memoryStore = new ConcurrentHashMap<>();

    /**
     * Retrieves or creates memory context.
     */
    public MemoryContext getOrCreateContext(
            String conversationId,
            String userId
    ) {

        ConversationMemory memory =
                conversationMemoryRepository.findByConversationId(
                                conversationId
                        )
                        .orElseGet(() -> {

                            ConversationMemory newMemory =
                                    ConversationMemory.builder()
                                            .conversationId(
                                                    conversationId
                                            )
                                            .userId(userId)
                                            .lastInteraction(
                                                    LocalDateTime.now()
                                            )
                                            .build();

                            return conversationMemoryRepository.save(
                                    newMemory
                            );
                        });

        return MemoryContext.builder()
                .conversationId(
                        memory.getConversationId()
                )
                .userId(
                        memory.getUserId()
                )
                .messages(
                        memory.getMessages()
                )
                .lastReportName(
                        memory.getLastReportName()
                )
                .lastConfigName(
                        memory.getLastConfigName()
                )
                .lastIntent(
                        memory.getLastIntent()
                )
                .lastExecutionResult(
                        memory.getLastExecutionResult()
                )
                .lastInteraction(
                        memory.getLastInteraction()
                )
                .build();
    }
    /**
     * Updates conversation memory.
     */
    public void updateContext(
            MemoryContext context,
            String message
    ) {

        context.addMessage(message);

        context.setLastInteraction(
                LocalDateTime.now()
        );

        ConversationMemory memory =
                conversationMemoryRepository.findByConversationId(
                                context.getConversationId()
                        )
                        .orElse(
                                new ConversationMemory()
                        );

        memory.setConversationId(
                context.getConversationId()
        );

        memory.setUserId(
                context.getUserId()
        );

        memory.setMessages(
                context.getMessages()
        );

        memory.setLastReportName(
                context.getLastReportName()
        );

        memory.setLastConfigName(
                context.getLastConfigName()
        );

        memory.setLastIntent(
                context.getLastIntent()
        );

        memory.setLastExecutionResult(
                context.getLastExecutionResult()
        );

        memory.setLastInteraction(
                context.getLastInteraction()
        );

        conversationMemoryRepository.save(memory);
    }
    /**
     * Clears conversation memory.
     */
    public void clearContext(
            String conversationId
    ) {

        conversationMemoryRepository.findByConversationId(
                        conversationId
                )
                .ifPresent(conversationMemoryRepository::delete);

        log.info(
                "Cleared memory for conversation {}",
                conversationId
        );
    }
}
