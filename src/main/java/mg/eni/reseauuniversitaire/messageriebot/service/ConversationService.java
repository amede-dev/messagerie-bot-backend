package mg.eni.reseauuniversitaire.messageriebot.service;

import lombok.RequiredArgsConstructor;
import mg.eni.reseauuniversitaire.messageriebot.dto.ConversationRequestDto;
import mg.eni.reseauuniversitaire.messageriebot.dto.ConversationResponseDto;
import mg.eni.reseauuniversitaire.messageriebot.dto.MessageResponseDto;
import mg.eni.reseauuniversitaire.messageriebot.dto.UserSummaryDto;
import mg.eni.reseauuniversitaire.messageriebot.entity.Conversation;
import mg.eni.reseauuniversitaire.messageriebot.entity.ConversationParticipant;
import mg.eni.reseauuniversitaire.messageriebot.entity.Message;
import mg.eni.reseauuniversitaire.messageriebot.entity.User;
import mg.eni.reseauuniversitaire.messageriebot.repository.ConversationParticipantRepository;
import mg.eni.reseauuniversitaire.messageriebot.repository.ConversationRepository;
import mg.eni.reseauuniversitaire.messageriebot.repository.MessageRepository;
import mg.eni.reseauuniversitaire.messageriebot.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
        return conversationRepository.findConversationsDeLUtilisateur(userId)
                .stream()
                .map(conversation -> versDto(conversation, userId))
                .toList();
    }

    @Transactional
    public ConversationResponseDto creer(
            ConversationRequestDto requete,
            Long createurId
    ) {
        User createur = userRepository.findById(createurId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Utilisateur créateur introuvable"));

        Conversation.Type type = Conversation.Type.valueOf(requete.type());

        // Une seule conversation privée entre les deux mêmes utilisateurs.
        if (type == Conversation.Type.PRIVEE) {
            if (requete.participantIds() == null
                    || requete.participantIds().size() != 1) {
                throw new IllegalArgumentException(
                        "Une conversation privée doit avoir exactement un autre participant");
            }

            Long autreUtilisateurId = requete.participantIds().get(0);

            Optional<Conversation> existante =
                    conversationRepository.findConversationPriveeEntre(
                            type,
                            createurId,
                            autreUtilisateurId
                    );

            if (existante.isPresent()) {
                return versDto(existante.get(), createurId);
            }
        }

        Conversation conversation = new Conversation();
        conversation.setType(type);
        conversation.setNom(requete.nom());
        conversation.setGroupeLieId(requete.groupeLieId());
        conversation.setCreateur(createur);

        conversation = conversationRepository.save(conversation);

        // Le créateur devient administrateur.
        ajouterParticipant(
                conversation,
                createur,
                ConversationParticipant.Role.ADMIN
        );

        for (Long userId : requete.participantIds()) {
            if (userId.equals(createurId)) {
                continue;
            }

            User utilisateur = userRepository.findById(userId)
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Participant introuvable : " + userId));

            ajouterParticipant(
                    conversation,
                    utilisateur,
                    ConversationParticipant.Role.MEMBRE
            );
        }

        return versDto(conversation, createurId);
    }

    @Transactional(readOnly = true)
    public List<UserSummaryDto> listerParticipants(
            Long conversationId,
            Long utilisateurCourantId
    ) {
        verifierMembre(conversationId, utilisateurCourantId);

        return participantRepository.findByConversationId(conversationId)
                .stream()
                .map(participant -> {
                    User utilisateur = participant.getUser();

                    return new UserSummaryDto(
                            utilisateur.getId(),
                            utilisateur.getNom(),
                            utilisateur.getPrenom(),
                            utilisateur.getEmail()
                    );
                })
                .toList();
    }

    @Transactional
    public void ajouterParticipant(
            Long conversationId,
            Long nouvelUtilisateurId,
            Long demandeurId
    ) {
        Conversation conversation = conversationRepository
                .findById(conversationId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Conversation introuvable"));

        if (conversation.getType() != Conversation.Type.GROUPE) {
            throw new IllegalArgumentException(
                    "Un participant peut seulement être ajouté à un groupe");
        }

        ConversationParticipant demandeur =
                participantRepository.findByConversationIdAndUserId(
                        conversationId,
                        demandeurId
                ).orElseThrow(() ->
                        new IllegalArgumentException(
                                "Vous ne faites pas partie de ce groupe"));

        if (demandeur.getRole() != ConversationParticipant.Role.ADMIN) {
            throw new IllegalArgumentException(
                    "Seul un administrateur peut ajouter des participants");
        }

        User nouvelUtilisateur = userRepository
                .findById(nouvelUtilisateurId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Utilisateur introuvable"));

        // Évite les doublons grâce à la vérification dans la méthode privée.
        ajouterParticipant(
                conversation,
                nouvelUtilisateur,
                ConversationParticipant.Role.MEMBRE
        );
    }

    @Transactional
    public void quitter(Long conversationId, Long userId) {
        Optional<ConversationParticipant> participant =
                participantRepository.findByConversationIdAndUserId(
                        conversationId,
                        userId
                );

        participant.ifPresent(participantRepository::delete);
    }

    private ConversationParticipant verifierMembre(
            Long conversationId,
            Long utilisateurId
    ) {
        return participantRepository
                .findByConversationIdAndUserId(conversationId, utilisateurId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Vous ne faites pas partie de cette conversation"));
    }

    private void ajouterParticipant(
            Conversation conversation,
            User utilisateur,
            ConversationParticipant.Role role
    ) {
        boolean existe = participantRepository
                .existsByConversationIdAndUserId(
                        conversation.getId(),
                        utilisateur.getId()
                );

        if (existe) {
            return;
        }

        ConversationParticipant participant = new ConversationParticipant();
        participant.setConversation(conversation);
        participant.setUser(utilisateur);
        participant.setRole(role);

        participantRepository.save(participant);
    }

    private ConversationResponseDto versDto(
        Conversation conversation,
        Long utilisateurCourantId
) {

    Optional<Message> dernierMessage =
            messageRepository
                    .findByConversationIdOrderByDateEnvoiDesc(
                            conversation.getId(),
                            PageRequest.of(
                                    0,
                                    1,
                                    Sort.unsorted()
                            )
                    )
                    .stream()
                    .findFirst();

    MessageResponseDto dernierMessageDto =
            dernierMessage
                    .map(MessageResponseDto::depuis)
                    .orElse(null);

    String nomAffiche = conversation.getNom();

    // ============================================================
    // PRÉSENCE + IDENTITÉ DE L'AUTRE UTILISATEUR (discussion privée)
    // ============================================================

    boolean enLigne = false;
    LocalDateTime derniereConnexion = null;
    Long utilisateurId = null; // id de l'AUTRE participant, uniquement pour une conversation PRIVEE

    // ============================================================
    // DISCUSSION PRIVÉE
    // ============================================================

    if (conversation.getType() ==
            Conversation.Type.PRIVEE) {

        Optional<User> autreUtilisateur =
                participantRepository
                        .findByConversationId(
                                conversation.getId()
                        )
                        .stream()
                        .map(ConversationParticipant::getUser)
                        .filter(user ->
                                !user.getId()
                                        .equals(
                                                utilisateurCourantId
                                        )
                        )
                        .findFirst();

        if (autreUtilisateur.isPresent()) {

            User autre =
                    autreUtilisateur.get();

            nomAffiche =
                    autre.getPrenom()
                            + " "
                            + autre.getNom();

            enLigne =
                    autre.isEnLigne();

            derniereConnexion =
                    autre.getDerniereConnexion();

            utilisateurId =
                    autre.getId();
        }
    }

    // ============================================================
    // NON-LUS
    // ============================================================

    int nombreNonLus = 0;

    // ============================================================
    // DTO
    // ============================================================

    return new ConversationResponseDto(
            conversation.getId(),
            conversation.getType().name(),
            nomAffiche,
            conversation.getGroupeLieId(),
            conversation.getDateCreation(),
            dernierMessageDto,
            nombreNonLus,
            utilisateurId,
            enLigne,
            derniereConnexion
    );
}
}