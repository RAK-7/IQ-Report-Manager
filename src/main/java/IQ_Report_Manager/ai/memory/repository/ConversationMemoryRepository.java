package IQ_Report_Manager.ai.memory.repository;

import IQ_Report_Manager.ai.memory.model.ConversationMemory;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

/**
 * Mongo repository for conversation memory.
 */
public interface ConversationMemoryRepository
        extends MongoRepository<ConversationMemory, String> {

    Optional<ConversationMemory> findByConversationId(
            String conversationId
    );
}