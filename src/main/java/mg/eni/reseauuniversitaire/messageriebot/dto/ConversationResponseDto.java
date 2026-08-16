package mg.eni.reseauuniversitaire.messageriebot.dto;

import mg.eni.reseauuniversitaire.messageriebot.entity.Conversation;

import java.time.LocalDateTime;

public record ConversationResponseDto(
        Long id,
        String type,
        String nom,
        Long groupeLieId,
        LocalDateTime dateCreation,
        MessageResponseDto dernierMessage,
        int nombreNonLus
) {
    public static ConversationResponseDto depuis(Conversation c, MessageResponseDto dernierMessage, int nonLus) {
        return new ConversationResponseDto(
                c.getId(),
                c.getType().name(),
                c.getNom(),
                c.getGroupeLieId(),
                c.getDateCreation(),
                dernierMessage,
                nonLus
        );
    }
}
