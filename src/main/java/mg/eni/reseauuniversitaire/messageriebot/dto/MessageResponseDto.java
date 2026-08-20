package mg.eni.reseauuniversitaire.messageriebot.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import mg.eni.reseauuniversitaire.messageriebot.entity.Message;

import java.time.LocalDateTime;

public record MessageResponseDto(

        Long id,

        Long conversationId,

        Long expediteurId,

        String expediteurNom,

        String contenu,

        String type,

        String statut,

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        LocalDateTime dateEnvoi,

        Long messageParentId

) {

    public static MessageResponseDto depuis(
            Message message
    ) {

        return new MessageResponseDto(

                message.getId(),

                message.getConversation().getId(),

                message.getExpediteur().getId(),

                message.getExpediteur().getNom()
                        + " "
                        + message.getExpediteur().getPrenom(),

                message.getContenu(),

                message.getType().name(),

                message.getStatut().name(),

                message.getDateEnvoi(),

                message.getMessageParentId()
        );
    }
}
