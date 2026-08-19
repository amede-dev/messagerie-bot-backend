package mg.eni.reseauuniversitaire.messageriebot.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record PresenceResponseDto(
        Long utilisateurId,
        boolean enLigne,

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        LocalDateTime derniereConnexion
) {
}