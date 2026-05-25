package IQ_Report_Manager.ai.memory;

//Stores and retrieves conversation context.


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
public class ConversationMemoryService {

    /**
     * Temporary in-memory storage.
     */
    private final Map<String, MemoryContext> memoryStore =
            new ConcurrentHashMap<>();

    /**
     * Retrieves or creates memory context.
     */
    public MemoryContext getOrCreateContext(
            String conversationId,
            String userId
    ) {

        return memoryStore.computeIfAbsent(
                conversationId,
                id -> MemoryContext.builder()
                        .conversationId(id)
                        .userId(userId)
                        .lastInteraction(LocalDateTime.now())
                        .build()
        );
    }

    /**
     * Updates conversation memory.
     */
    public void updateContext(
            MemoryContext context,
            String message
    ) {

        context.addMessage(message);
        context.setLastInteraction(LocalDateTime.now());

        memoryStore.put(
                context.getConversationId(),
                context
        );
    }

    /**
     * Clears conversation memory.
     */
    public void clearContext(String conversationId) {

        memoryStore.remove(conversationId);

        log.info(
                "Cleared memory for conversation: {}",
                conversationId
        );
    }
}
