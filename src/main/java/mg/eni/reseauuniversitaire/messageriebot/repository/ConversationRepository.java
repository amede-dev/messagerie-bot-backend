package mg.eni.reseauuniversitaire.messageriebot.repository;

import mg.eni.reseauuniversitaire.messageriebot.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    @Query("""
            SELECT DISTINCT c FROM Conversation c
            JOIN c.participants p
            WHERE p.user.id = :userId AND c.archivee = false
            ORDER BY c.dateCreation DESC
            """)
    List<Conversation> findConversationsDeLUtilisateur(@Param("userId") Long userId);
}
