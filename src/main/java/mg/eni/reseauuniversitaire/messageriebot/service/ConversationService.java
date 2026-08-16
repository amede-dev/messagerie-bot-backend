package mg.eni.reseauuniversitaire.messageriebot.service;

import mg.eni.reseauuniversitaire.messageriebot.dto.ConversationRequestDto;
import mg.eni.reseauuniversitaire.messageriebot.dto.ConversationResponseDto;
import mg.eni.reseauuniversitaire.messageriebot.dto.MessageResponseDto;
import mg.eni.reseauuniversitaire.messageriebot.entity.Conversation;
import mg.eni.reseauuniversitaire.messageriebot.entity.ConversationParticipant;
import mg.eni.reseauuniversitaire.messageriebot.entity.Message;
import mg.eni.reseauuniversitaire.messageriebot.entity.User;
import mg.eni.reseauuniversitaire.messageriebot.repository.ConversationParticipantRepository;
import mg.eni.reseauuniversitaire.messageriebot.repository.ConversationRepository;
import mg.eni.reseauuniversitaire.messageriebot.repository.MessageRepository;
import mg.eni.reseauuniversitaire.messageriebot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final ConversationParticipantRepository participantRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    public List<ConversationResponseDto> listerConversationsDe(Long userId) {
        return conversationRepository.findConversationsDeLUtilisateur(userId).stream()
                .map(c -> versDto(c, userId))
                .toList();
    }

    @Transactional
    public ConversationResponseDto creer(ConversationRequestDto requete, Long createurId) {
        User createur = userRepository.findById(createurId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur createur introuvable"));

        Conversation conversation = new Conversation();
        conversation.setType(Conversation.Type.valueOf(requete.type()));
        conversation.setNom(requete.nom());
        conversation.setGroupeLieId(requete.groupeLieId());
        conversation.setCreateur(createur);
        conversation = conversationRepository.save(conversation);

        ajouterParticipant(conversation, createur, ConversationParticipant.Role.ADMIN);
        for (Long userId : requete.participantIds()) {
            if (userId.equals(createurId)) continue;
            User u = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("Participant introuvable : " + userId));
            ajouterParticipant(conversation, u, ConversationParticipant.Role.MEMBRE);
        }

        return versDto(conversation, createurId);
    }

    private void ajouterParticipant(Conversation conversation, User user, ConversationParticipant.Role role) {
        if (participantRepository.existsByConversationIdAndUserId(conversation.getId(), user.getId())) return;
        ConversationParticipant participant = new ConversationParticipant();
        participant.setConversation(conversation);
        participant.setUser(user);
        participant.setRole(role);
        participantRepository.save(participant);
    }

    @Transactional
    public void quitter(Long conversationId, Long userId) {
        Optional<ConversationParticipant> participant =
                participantRepository.findByConversationIdAndUserId(conversationId, userId);
        participant.ifPresent(participantRepository::delete);
    }

    // utilisateurCourantId : necessaire pour savoir, dans une conversation
    // PRIVEE, QUI est "l'autre" personne a afficher comme nom.
    private ConversationResponseDto versDto(Conversation conversation, Long utilisateurCourantId) {
        Optional<Message> dernier = messageRepository
                .findByConversationIdOrderByDateEnvoiDesc(conversation.getId(), PageRequest.of(0, 1, Sort.unsorted()))
                .stream().findFirst();

        MessageResponseDto dernierDto = dernier.map(MessageResponseDto::depuis).orElse(null);

        String nomAffiche = conversation.getNom();
        if (conversation.getType() == Conversation.Type.PRIVEE) {
            nomAffiche = participantRepository.findByConversationId(conversation.getId()).stream()
                    .map(ConversationParticipant::getUser)
                    .filter(u -> !u.getId().equals(utilisateurCourantId))
                    .findFirst()
                    .map(u -> u.getPrenom() + " " + u.getNom())
                    .orElse("Discussion privée");
        }

        // TODO: calculer le vrai nombre de non-lus (comparer dateDernierMessageLu du participant)
        return ConversationResponseDto.depuis(conversation, nomAffiche, dernierDto, 0);
    }
}