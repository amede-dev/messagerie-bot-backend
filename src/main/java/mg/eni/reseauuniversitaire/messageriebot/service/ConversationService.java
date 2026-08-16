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
                .map(this::versDto)
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

        // Ajoute le createur + tous les participants demandes
        ajouterParticipant(conversation, createur, ConversationParticipant.Role.ADMIN);
        for (Long userId : requete.participantIds()) {
            if (userId.equals(createurId)) continue;
            User u = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("Participant introuvable : " + userId));
            ajouterParticipant(conversation, u, ConversationParticipant.Role.MEMBRE);
        }

        return versDto(conversation);
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

    private ConversationResponseDto versDto(Conversation conversation) {
        Optional<Message> dernier = messageRepository
                .findByConversationIdOrderByDateEnvoiDesc(conversation.getId(), PageRequest.of(0, 1, Sort.unsorted()))
                .stream().findFirst();

        MessageResponseDto dernierDto = dernier.map(MessageResponseDto::depuis).orElse(null);
        // TODO: calculer le vrai nombre de non-lus (comparer dateDernierMessageLu du participant)
        return ConversationResponseDto.depuis(conversation, dernierDto, 0);
    }
}
