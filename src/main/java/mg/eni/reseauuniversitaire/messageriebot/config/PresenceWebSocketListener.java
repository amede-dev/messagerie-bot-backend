package mg.eni.reseauuniversitaire.messageriebot.config;

import lombok.RequiredArgsConstructor;
import mg.eni.reseauuniversitaire.messageriebot.entity.User;
import mg.eni.reseauuniversitaire.messageriebot.service.PresenceService;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class PresenceWebSocketListener {

    private final PresenceService presenceService;

    // Un même compte peut être ouvert sur plusieurs appareils ou onglets.
    // Il reste en ligne tant qu'au moins une de ses sessions STOMP est active.
    private final ConcurrentHashMap<Long, Set<String>> sessionsParUtilisateur =
            new ConcurrentHashMap<>();

    // ============================================================
    // CONNEXION
    // ============================================================

    @EventListener
    public void utilisateurConnecte(
            SessionConnectedEvent event
    ) {

        StompHeaderAccessor accessor =
                StompHeaderAccessor.wrap(
                        event.getMessage()
                );

        Principal principal =
                accessor.getUser();

        if (principal instanceof Authentication authentication
                && authentication.getPrincipal() instanceof User user) {
            String sessionId = accessor.getSessionId();
            if (sessionId != null && sessionsParUtilisateur
                    .computeIfAbsent(user.getId(), ignored -> ConcurrentHashMap.newKeySet())
                    .add(sessionId)) {
                presenceService.connecter(user);
            }
        }
    }

    // ============================================================
    // DÉCONNEXION
    // ============================================================

    @EventListener
    public void utilisateurDeconnecte(
            SessionDisconnectEvent event
    ) {

        StompHeaderAccessor accessor =
                StompHeaderAccessor.wrap(
                        event.getMessage()
                );

        Principal principal =
                accessor.getUser();

        if (principal instanceof Authentication authentication
                && authentication.getPrincipal() instanceof User user) {
            String sessionId = accessor.getSessionId();
            Set<String> sessions = sessionsParUtilisateur.get(user.getId());

            if (sessions != null && sessionId != null && sessions.remove(sessionId)
                    && sessions.isEmpty()) {
                sessionsParUtilisateur.remove(user.getId(), sessions);
                presenceService.deconnecter(user);
            }
        }
    }
}
