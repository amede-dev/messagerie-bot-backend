package mg.eni.reseauuniversitaire.messageriebot.repository;

import mg.eni.reseauuniversitaire.messageriebot.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    @Query("""
            SELECT COUNT(DISTINCT p.user.id) FROM ConversationParticipant p
            WHERE p.conversation.type = mg.eni.reseauuniversitaire.messageriebot.entity.Conversation$Type.PRIVEE
            AND p.user.id <> :userId
            AND EXISTS (
                SELECT 1 FROM ConversationParticipant moi
                WHERE moi.conversation = p.conversation AND moi.user.id = :userId
            )
            AND p.conversation.archivee = false
            """)
    long compterContactsPrives(@Param("userId") Long userId);

    @Query("""
            SELECT DISTINCT c FROM Conversation c
            JOIN c.participants p
            WHERE p.user.id = :userId AND c.archivee = false
            ORDER BY c.dateCreation DESC
            """)
    List<Conversation> findConversationsDeLUtilisateur(@Param("userId") Long userId);

    // Recherche une conversation PRIVEE (1 a 1) deja existante entre deux
    // utilisateurs precis, pour eviter de creer un doublon quand on
    // selectionne plusieurs fois le meme contact dans l'annuaire
    // (ecran "Nouvelle discussion" cote Flutter).
    @Query("""
            SELECT c FROM Conversation c
            WHERE c.type = :type
            AND c.archivee = false
            AND EXISTS (
                SELECT 1 FROM ConversationParticipant p2
                WHERE p2.conversation = c AND p2.user.id = :userB
            )
            """)
    Optional<Conversation> findConversationPriveeEntre(
            @Param("type") Conversation.Type type,
            @Param("userB") Long userB
    );
}
