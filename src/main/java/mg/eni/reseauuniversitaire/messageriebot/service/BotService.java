package mg.eni.reseauuniversitaire.messageriebot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import mg.eni.reseauuniversitaire.messageriebot.dto.BotResponseDto;
import mg.eni.reseauuniversitaire.messageriebot.entity.BotIntent;
import mg.eni.reseauuniversitaire.messageriebot.entity.BotSession;
import mg.eni.reseauuniversitaire.messageriebot.entity.User;
import mg.eni.reseauuniversitaire.messageriebot.repository.BotIntentRepository;
import mg.eni.reseauuniversitaire.messageriebot.repository.BotSessionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.ArrayList;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BotService {

    private static final String GEMINI_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/"
                    + "%s:generateContent";

    private final BotIntentRepository botIntentRepository;
    private final BotSessionRepository botSessionRepository;
    private final mg.eni.reseauuniversitaire.messageriebot.repository.UserRepository userRepository;
    private final ObjectMapper objectMapper;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Value("${gemini.api-key:}")
    private String geminiApiKey;

    @Value("${gemini.model:gemini-2.5-flash}")
    private String geminiModel;

    public BotResponseDto repondre(String texteUtilisateur, Long utilisateurId) {
        String texteNormalise = normaliser(texteUtilisateur);
        BotSession session = sessionActive(utilisateurId);
        List<Map<String, String>> historique = lireContexte(session);
        historique.add(Map.of("role", "user", "text", texteUtilisateur));
        limiterHistorique(historique);

        // Les réponses spécifiques de l'université restent prioritaires.
        for (BotIntent intent : botIntentRepository.findByActifTrue()) {
            boolean correspond = intent.getMotsClesDeclencheurs()
                    .stream()
                    .map(this::normaliser)
                    .anyMatch(texteNormalise::contains);

            if (correspond) {
                BotResponseDto reponse = new BotResponseDto(
                        intent.getReponseTexte(),
                        intent.getSuggestions(),
                        false
                );
                enregistrerEchange(session, historique, reponse.texte());
                return reponse;
            }
        }

        // Conversation naturelle avec Gemini.
        if (geminiApiKey != null && !geminiApiKey.isBlank()) {
            try {
                String reponse = repondreAvecGemini(historique);

                BotResponseDto resultat = new BotResponseDto(
                        reponse,
                        List.of(
                                "Emploi du temps",
                                "Horaires de la bibliothèque",
                                "Contacter un responsable"
                        ),
                        false
                );
                enregistrerEchange(session, historique, resultat.texte());
                return resultat;
            } catch (Exception exception) {
                System.err.println(
                        "Erreur Gemini : " + exception.getMessage()
                );
            }
        }

        // Secours si la clé est absente ou si OpenAI est indisponible.
        BotResponseDto resultat = new BotResponseDto(
                "Je ne peux pas répondre pour le moment. "
                        + "Réessaie dans quelques instants ou contacte un responsable.",
                List.of(
                        "Emploi du temps",
                        "Horaires",
                        "Contacter un responsable"
                ),
                true
        );
        enregistrerEchange(session, historique, resultat.texte());
        return resultat;
    }

    private String repondreAvecGemini(List<Map<String, String>> historique)
            throws Exception {
        Map<String, Object> requete = Map.of(
                "systemInstruction", Map.of("parts", List.of(Map.of(
                        "text", "Tu es Uni AI, assistant officiel d'un réseau "
                                + "universitaire à Madagascar. Réponds en français "
                                + "ou en malgache selon la langue de l'étudiant. "
                                + "Sois concis, utile et naturel. N'invente jamais "
                                + "une information officielle inconnue ; conseille "
                                + "alors de contacter un responsable."
                ))),
                "contents", historique.stream()
                        .map(echange -> Map.of(
                                "role", echange.get("role"),
                                "parts", List.of(Map.of("text", echange.get("text")))
                        ))
                        .toList()
        );

        String corps = objectMapper.writeValueAsString(requete);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(GEMINI_URL.formatted(geminiModel)))
                .header("Content-Type", "application/json")
                .header("x-goog-api-key", geminiApiKey)
                .POST(HttpRequest.BodyPublishers.ofString(corps))
                .build();

        HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException(
                    "Gemini a retourné le statut "
                            + response.statusCode()
                            + " : "
                            + response.body()
            );
        }

        String texte = extraireTexteGemini(response.body());

        if (texte.isBlank()) {
            throw new IllegalStateException(
                "Gemini n'a retourné aucun texte"
            );
        }

        return texte;
    }

    private String extraireTexteGemini(String reponseJson) throws Exception {
        JsonNode racine = objectMapper.readTree(reponseJson);
        for (JsonNode candidate : racine.path("candidates")) {
            for (JsonNode part : candidate.path("content").path("parts")) {
                String texte = part.path("text").asText("");
                if (!texte.isBlank()) return texte;
            }
        }
        return "";
    }

    private BotSession sessionActive(Long utilisateurId) {
        return botSessionRepository
                .findFirstByUserIdAndStatutOrderByDateDebutDesc(
                        utilisateurId, BotSession.Statut.ACTIVE
                )
                .orElseGet(() -> {
                    User user = userRepository.findById(utilisateurId)
                            .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));
                    BotSession session = new BotSession();
                    session.setUser(user);
                    session.setContexteConversation("[]");
                    return botSessionRepository.save(session);
                });
    }

    private List<Map<String, String>> lireContexte(BotSession session) {
        try {
            JsonNode json = objectMapper.readTree(session.getContexteConversation());
            List<Map<String, String>> resultat = new ArrayList<>();
            json.forEach(element -> resultat.add(Map.of(
                    "role", element.path("role").asText(),
                    "text", element.path("text").asText()
            )));
            return resultat;
        } catch (Exception ignored) {
            return new ArrayList<>();
        }
    }

    private void limiterHistorique(List<Map<String, String>> historique) {
        while (historique.size() > 12) historique.remove(0);
    }

    private void enregistrerEchange(
            BotSession session,
            List<Map<String, String>> historique,
            String reponse
    ) {
        historique.add(Map.of("role", "model", "text", reponse));
        limiterHistorique(historique);
        try {
            session.setContexteConversation(objectMapper.writeValueAsString(historique));
            botSessionRepository.save(session);
        } catch (Exception exception) {
            System.err.println("Impossible d'enregistrer le contexte du bot : "
                    + exception.getMessage());
        }
    }

    private String normaliser(String texte) {
        String sansAccents = Normalizer
                .normalize(texte, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");

        return sansAccents.toLowerCase(Locale.FRENCH).trim();
    }
}
