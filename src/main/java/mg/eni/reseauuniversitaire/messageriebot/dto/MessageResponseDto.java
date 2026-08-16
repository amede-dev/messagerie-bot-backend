package mg.eni.reseauuniversitaire.messageriebot.dto;

import mg.eni.reseauuniversitaire.messageriebot.entity.Message;

import java.time.LocalDateTime;

/// Ce que le frontend Flutter recoit et attend (voir MessageModel.fromJson
/// cote Flutter) : les noms de champs doivent correspondre exactement.
public record MessageResponseDto(
        Long id,
        Long conversationId,
        Long expediteurId,
        String expediteurNom,
        String contenu,
        String type,
        String statut,
        LocalDateTime dateEnvoi,
        Long messageParentId
) {
    public static MessageResponseDto depuis(Message message) {
        return new MessageResponseDto(
                message.getId(),
                message.getConversation().getId(),
                message.getExpediteur().getId(),
                message.getExpediteur().getPrenom() + " " + message.getExpediteur().getNom(),
                message.getContenu(),
                message.getType().name(),
                message.getStatut().name(),
                message.getDateEnvoi(),
                message.getMessageParentId()
        );
    }
}
