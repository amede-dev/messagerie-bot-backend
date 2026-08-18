package mg.eni.reseauuniversitaire.messageriebot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import mg.eni.reseauuniversitaire.messageriebot.dto.BotResponseDto;
import mg.eni.reseauuniversitaire.messageriebot.entity.BotIntent;
import mg.eni.reseauuniversitaire.messageriebot.repository.BotIntentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BotService {

    private static final String OPENAI_URL =
            "https://api.openai.com/v1/responses";

    private final BotIntentRepository botIntentRepository;
    private final ObjectMapper objectMapper;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Value("${openai.api-key:}")
    private String openAiApiKey;

    @Value("${openai.model:gpt-5.6}")
    private String openAiModel;

    public BotResponseDto repondre(String texteUtilisateur) {
        String texteNormalise = normaliser(texteUtilisateur);

        // Les réponses spécifiques de l'université restent prioritaires.
        for (BotIntent intent : botIntentRepository.findByActifTrue()) {
            boolean correspond = intent.getMotsClesDeclencheurs()
                    .stream()
                    .map(this::normaliser)
                    .anyMatch(texteNormalise::contains);

            if (correspond) {
                return new BotResponseDto(
                        intent.getReponseTexte(),
                        intent.getSuggestions(),
                        false
                );
            }
        }

        // Conversation naturelle avec OpenAI.
        if (openAiApiKey != null && !openAiApiKey.isBlank()) {
            try {
                String reponse = repondreAvecOpenAi(texteUtilisateur);

                return new BotResponseDto(
                        reponse,
                        List.of(
                                "Emploi du temps",
                                "Horaires de la bibliothèque",
                                "Contacter un responsable"
                        ),
                        false
                );
            } catch (Exception exception) {
                System.err.println(
                        "Erreur OpenAI : " + exception.getMessage()
                );
            }
        }

        // Secours si la clé est absente ou si OpenAI est indisponible.
        return new BotResponseDto(
                "Je ne peux pas répondre pour le moment. "
                        + "Réessaie dans quelques instants ou contacte un responsable.",
                List.of(
                        "Emploi du temps",
                        "Horaires",
                        "Contacter un responsable"
                ),
                true
        );
    }

    private String repondreAvecOpenAi(String texteUtilisateur) throws Exception {
        Map<String, Object> requete = Map.of(
                "model", openAiModel,
                "store", false,
                "instructions", """
                        Tu es Uni AI, l'assistant officiel d'un réseau
                        universitaire à Madagascar.

                        Réponds en français ou en malgache selon la langue
                        utilisée par l'étudiant. Sois chaleureux, concis,
                        utile et naturel.

                        Tu peux aider sur la vie universitaire, les cours,
                        l'organisation, la communication et les questions
                        générales. N'invente jamais un emploi du temps,
                        une note, un règlement ou une information officielle
                        si tu ne la connais pas. Dans ce cas, conseille à
                        l'étudiant de contacter un responsable.
                        """,
                "input", texteUtilisateur
        );

        String corps = objectMapper.writeValueAsString(requete);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(OPENAI_URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + openAiApiKey)
                .POST(HttpRequest.BodyPublishers.ofString(corps))
                .build();

        HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException(
                    "OpenAI a retourné le statut "
                            + response.statusCode()
                            + " : "
                            + response.body()
            );
        }

        String texte = extraireTexteOpenAi(response.body());

        if (texte.isBlank()) {
            throw new IllegalStateException(
                    "OpenAI n'a retourné aucun texte"
            );
        }

        return texte;
    }

    private String extraireTexteOpenAi(String reponseJson) throws Exception {
        JsonNode racine = objectMapper.readTree(reponseJson);

        // Compatible avec les réponses qui fournissent output_text.
        JsonNode outputText = racine.path("output_text");
        if (outputText.isTextual() && !outputText.asText().isBlank()) {
            return outputText.asText();
        }

        // Extraction standard du contenu de la Responses API.
        for (JsonNode output : racine.path("output")) {
            for (JsonNode contenu : output.path("content")) {
                if ("output_text".equals(contenu.path("type").asText())) {
                    String texte = contenu.path("text").asText("");
                    if (!texte.isBlank()) {
                        return texte;
                    }
                }
            }
        }

        return "";
    }

    private String normaliser(String texte) {
        String sansAccents = Normalizer
                .normalize(texte, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");

        return sansAccents.toLowerCase(Locale.FRENCH).trim();
    }
}