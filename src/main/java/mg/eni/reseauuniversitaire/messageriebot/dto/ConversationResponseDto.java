package mg.eni.reseauuniversitaire.messageriebot.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import mg.eni.reseauuniversitaire.messageriebot.entity.Conversation;

import java.time.LocalDateTime;

public record ConversationResponseDto(
        Long id,
        String type,
        String nom,
        Long groupeLieId,
        @JsonFormat(shape = JsonFormat.Shape.STRING) LocalDateTime dateCreation,
        MessageResponseDto dernierMessage,
        int nombreNonLus
) {
    public static ConversationResponseDto depuis(Conversation c, String nomAffiche,
                                                   MessageResponseDto dernierMessage, int nonLus) {
        return new ConversationResponseDto(
                c.getId(),
                c.getType().name(),
                nomAffiche,
                c.getGroupeLieId(),
                c.getDateCreation(),
                dernierMessage,
                nonLus
        );
    }
}