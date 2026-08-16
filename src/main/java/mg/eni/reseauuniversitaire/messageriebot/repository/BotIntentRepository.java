package mg.eni.reseauuniversitaire.messageriebot.repository;

import mg.eni.reseauuniversitaire.messageriebot.entity.BotIntent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BotIntentRepository extends JpaRepository<BotIntent, Long> {
    List<BotIntent> findByActifTrue();
}
