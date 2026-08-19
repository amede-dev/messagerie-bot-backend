package mg.eni.reseauuniversitaire.messageriebot.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record ConversationResponseDto(

        Long id,

        String type,

        String nom,

        Long groupeLieId,

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        LocalDateTime dateCreation,

        MessageResponseDto dernierMessage,

        int nombreNonLus,

        // ==========================================================
        // UTILISATEUR DE LA CONVERSATION PRIVÉE
        // ==========================================================

        Long utilisateurId,

        // ==========================================================
        // ÉTAT DE CONNEXION
        // ==========================================================

        boolean enLigne,

        // ==========================================================
        // DERNIÈRE CONNEXION
        // ==========================================================

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        LocalDateTime derniereConnexion

) {
}