package mg.eni.reseauuniversitaire.messageriebot.service;

import lombok.RequiredArgsConstructor;
import mg.eni.reseauuniversitaire.messageriebot.dto.MessageRequestDto;
import mg.eni.reseauuniversitaire.messageriebot.dto.MessageResponseDto;
import mg.eni.reseauuniversitaire.messageriebot.entity.Conversation;
import mg.eni.reseauuniversitaire.messageriebot.entity.ConversationParticipant;
import mg.eni.reseauuniversitaire.messageriebot.entity.Message;
import mg.eni.reseauuniversitaire.messageriebot.entity.Notification;
import mg.eni.reseauuniversitaire.messageriebot.entity.User;
import mg.eni.reseauuniversitaire.messageriebot.repository.ConversationRepository;
import mg.eni.reseauuniversitaire.messageriebot.repository.ConversationParticipantRepository;
import mg.eni.reseauuniversitaire.messageriebot.repository.MessageRepository;
import mg.eni.reseauuniversitaire.messageriebot.repository.NotificationRepository;
import mg.eni.reseauuniversitaire.messageriebot.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final ConversationParticipantRepository participantRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;

    public Page<MessageResponseDto> historique(
            Long conversationId,
            Long utilisateurId,
            int page
    ) {
        verifierMembre(conversationId, utilisateurId);
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

        verifierMembre(conversationId, expediteurId);

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
                        ? Message.Type.valueOf(
                                requete.type().trim().toUpperCase(Locale.ROOT)
                        )
                        : Message.Type.TEXTE
        );
        message.setMessageParentId(requete.messageParentId());
        message.setStatut(Message.Statut.ENVOYE);

        message = messageRepository.save(message);
        creerNotificationsPourDestinataires(message, expediteur);

        return MessageResponseDto.depuis(message);
    }

    private void creerNotificationsPourDestinataires(
            Message message,
            User expediteur
    ) {
        final String contenu = expediteur.getNom()
                + " "
                + expediteur.getPrenom()
                + " vous a envoyé un message";

        final List<Notification> notifications = participantRepository
                .findByConversationId(message.getConversation().getId())
                .stream()
                .map(ConversationParticipant::getUser)
                .filter(destinataire -> !destinataire.getId().equals(expediteur.getId()))
                .map(destinataire -> {
                    Notification notification = new Notification();
                    notification.setUser(destinataire);
                    notification.setType("MESSAGE");
                    notification.setContenu(contenu);
                    notification.setLu(false);
                    notification.setReferenceId(message.getId());
                    return notification;
                })
                .toList();

        notificationRepository.saveAll(notifications);
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
    public MessageResponseDto modifier(Long messageId, Long utilisateurId, String contenu) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Message introuvable"));

        if (!message.getExpediteur().getId().equals(utilisateurId)) {
            throw new AccessDeniedException("Vous ne pouvez modifier que vos propres messages");
        }

        message.setContenu(contenu.trim());
        return MessageResponseDto.depuis(messageRepository.save(message));
    }

    public List<String> emailsDestinataires(Long conversationId, Long expediteurId) {
        return participantRepository.findByConversationId(conversationId).stream()
                .map(participant -> participant.getUser())
                .filter(user -> !user.getId().equals(expediteurId))
                .map(User::getEmail)
                .toList();
    }

    private void verifierMembre(Long conversationId, Long utilisateurId) {
        if (!participantRepository.existsByConversationIdAndUserId(
                conversationId, utilisateurId
        )) {
            throw new AccessDeniedException(
                    "Vous ne faites pas partie de cette conversation"
            );
        }
    }

    @Transactional
    public Long supprimerPourTous(Long messageId, Long utilisateurId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Message introuvable"));

        if (!message.getExpediteur().getId().equals(utilisateurId)) {
            throw new AccessDeniedException(
                    "Vous ne pouvez supprimer que vos propres messages"
            );
        }

        Long conversationId = message.getConversation().getId();
        messageRepository.delete(message);
        return conversationId;
    }
}
