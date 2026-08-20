package mg.eni.reseauuniversitaire.messageriebot.repository;

import mg.eni.reseauuniversitaire.messageriebot.entity.ConversationParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConversationParticipantRepository extends JpaRepository<ConversationParticipant, Long> {

    boolean existsByConversationIdAndUserId(Long conversationId, Long userId);

    Optional<ConversationParticipant> findByConversationIdAndUserId(Long conversationId, Long userId);

    List<ConversationParticipant> findByConversationId(Long conversationId);

    void deleteByConversationId(Long conversationId);
}
