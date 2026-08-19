package mg.eni.reseauuniversitaire.messageriebot.service;

import lombok.RequiredArgsConstructor;
import mg.eni.reseauuniversitaire.messageriebot.dto.PresenceResponseDto;
import mg.eni.reseauuniversitaire.messageriebot.entity.User;
import mg.eni.reseauuniversitaire.messageriebot.repository.UserRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PresenceService {

    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    // ============================================================
    // UTILISATEUR EN LIGNE
    // ============================================================

    @Transactional
    public void connecter(User user) {

        User utilisateur =
                userRepository
                        .findById(user.getId())
                        .orElse(null);

        if (utilisateur == null) {
            return;
        }

        utilisateur.setEnLigne(true);

        userRepository.save(utilisateur);

        PresenceResponseDto presence =
                new PresenceResponseDto(
                        utilisateur.getId(),
                        true,
                        utilisateur.getDerniereConnexion()
                );

        // Informer tous les utilisateurs connectés.
        messagingTemplate.convertAndSend(
                "/topic/presence",
                presence
        );
    }

    // ============================================================
    // UTILISATEUR HORS LIGNE
    // ============================================================

    @Transactional
    public void deconnecter(User user) {

        User utilisateur =
                userRepository
                        .findById(user.getId())
                        .orElse(null);

        if (utilisateur == null) {
            return;
        }

        LocalDateTime maintenant =
                LocalDateTime.now();

        utilisateur.setEnLigne(false);
        utilisateur.setDerniereConnexion(
                maintenant
        );

        userRepository.save(utilisateur);

        PresenceResponseDto presence =
                new PresenceResponseDto(
                        utilisateur.getId(),
                        false,
                        maintenant
                );

        messagingTemplate.convertAndSend(
                "/topic/presence",
                presence
        );
    }
}