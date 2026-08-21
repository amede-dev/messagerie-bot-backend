package mg.eni.reseauuniversitaire.messageriebot.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ConversationRequestDto(
        @NotNull String type, // "PRIVEE"
        String nom,
        @NotEmpty List<Long> participantIds
) {
}
