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
    // nomAffiche : pour une conversation PRIVEE, c'est le nom de l'AUTRE
    // participant (calcule cote service) ; pour un GROUPE, le nom stocke.
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