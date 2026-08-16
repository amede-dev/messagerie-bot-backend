package mg.eni.reseauuniversitaire.messageriebot.service;

import mg.eni.reseauuniversitaire.messageriebot.dto.BotResponseDto;
import mg.eni.reseauuniversitaire.messageriebot.entity.BotIntent;
import mg.eni.reseauuniversitaire.messageriebot.repository.BotIntentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;

/// Moteur a regles simple : normalise le texte recu, cherche l'intent dont
/// un mot-cle apparait dans le message, et renvoie sa reponse. Si aucun
/// intent ne correspond, propose une escalade vers un humain (RF16).
/// Cf. conception section 10 "Bot Engine".
@Service
@RequiredArgsConstructor
public class BotService {

    private final BotIntentRepository botIntentRepository;

    public BotResponseDto repondre(String texteUtilisateur) {
        String texteNormalise = normaliser(texteUtilisateur);

        List<BotIntent> intents = botIntentRepository.findByActifTrue();

        for (BotIntent intent : intents) {
            boolean correspond = intent.getMotsClesDeclencheurs().stream()
                    .map(this::normaliser)
                    .anyMatch(texteNormalise::contains);

            if (correspond) {
                return new BotResponseDto(intent.getReponseTexte(), intent.getSuggestions(), false);
            }
        }

        // Aucun intent trouve : reponse par defaut + escalade proposee
        return new BotResponseDto(
                "Je n'ai pas bien compris ta demande. Veux-tu que je te mette en contact avec un responsable ?",
                List.of("Contacter un humain", "Reformuler ma question"),
                true
        );
    }

    private String normaliser(String texte) {
        String sansAccents = Normalizer.normalize(texte, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return sansAccents.toLowerCase(Locale.FRENCH).trim();
    }
}
