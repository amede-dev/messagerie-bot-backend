package mg.eni.reseauuniversitaire.messageriebot.repository;

import mg.eni.reseauuniversitaire.messageriebot.entity.BotSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BotSessionRepository extends JpaRepository<BotSession, Long> {
    Optional<BotSession> findFirstByUserIdAndStatutOrderByDateDebutDesc(Long userId, BotSession.Statut statut);
}
