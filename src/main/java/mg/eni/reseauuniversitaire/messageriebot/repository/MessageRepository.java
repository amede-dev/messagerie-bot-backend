package mg.eni.reseauuniversitaire.messageriebot.repository;

import mg.eni.reseauuniversitaire.messageriebot.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, Long> {

    Page<Message> findByConversationIdOrderByDateEnvoiDesc(Long conversationId, Pageable pageable);

    long countByConversationIdAndExpediteurIdNotAndStatutNot(
            Long conversationId,
            Long expediteurId,
            Message.Statut statut
    );
}
