package mg.eni.reseauuniversitaire.messageriebot.service;

import lombok.RequiredArgsConstructor;
import mg.eni.reseauuniversitaire.messageriebot.dto.MessageRequestDto;
import mg.eni.reseauuniversitaire.messageriebot.dto.MessageResponseDto;
import mg.eni.reseauuniversitaire.messageriebot.entity.Conversation;
import mg.eni.reseauuniversitaire.messageriebot.entity.Message;
import mg.eni.reseauuniversitaire.messageriebot.entity.User;
import mg.eni.reseauuniversitaire.messageriebot.repository.ConversationRepository;
import mg.eni.reseauuniversitaire.messageriebot.repository.MessageRepository;
import mg.eni.reseauuniversitaire.messageriebot.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;

    public Page<MessageResponseDto> historique(Long conversationId, int page) {
        Page<Message> messages = messageRepository
                .findByConversationIdOrderByDateEnvoiDesc(
                        conversationId,
                        PageRequest.of(page, 30, Sort.unsorted())
                );

        return messages.map(MessageResponseDto::depuis);
    }

    @Transactional
    public MessageResponseDto envoyer(
            Long conversationId,
            Long expediteurId,
            MessageRequestDto requete
    ) {
        Conversation conversation = conversationRepository
                .findById(conversationId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Conversation introuvable"));

        User expediteur = userRepository
                .findById(expediteurId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Expéditeur introuvable"));

        Message message = new Message();
        message.setConversation(conversation);
        message.setExpediteur(expediteur);
        message.setContenu(requete.contenu());
        message.setType(
                requete.type() != null
                        ? Message.Type.valueOf(requete.type())
                        : Message.Type.TEXTE
        );
        message.setMessageParentId(requete.messageParentId());
        message.setStatut(Message.Statut.ENVOYE);

        message = messageRepository.save(message);
        return MessageResponseDto.depuis(message);
    }

    @Transactional
    public MessageResponseDto marquerStatut(Long messageId, String statut) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Message introuvable"));

        message.setStatut(Message.Statut.valueOf(statut));
        return MessageResponseDto.depuis(messageRepository.save(message));
    }

    @Transactional
    public void supprimerPourTous(Long messageId, Long utilisateurId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Message introuvable"));

        if (!message.getExpediteur().getId().equals(utilisateurId)) {
            throw new AccessDeniedException(
                    "Vous ne pouvez supprimer que vos propres messages"
            );
        }

        messageRepository.delete(message);
    }
}
