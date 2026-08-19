package mg.eni.reseauuniversitaire.messageriebot.config;

import lombok.RequiredArgsConstructor;
import mg.eni.reseauuniversitaire.messageriebot.entity.User;
import mg.eni.reseauuniversitaire.messageriebot.service.PresenceService;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;

@Component
@RequiredArgsConstructor
public class PresenceWebSocketListener {

    private final PresenceService presenceService;

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

        if (principal instanceof User user) {
            presenceService.connecter(user);
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

        if (principal instanceof User user) {
            presenceService.deconnecter(user);
        }
    }
}