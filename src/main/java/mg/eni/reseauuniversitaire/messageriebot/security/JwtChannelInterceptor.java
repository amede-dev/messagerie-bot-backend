package mg.eni.reseauuniversitaire.messageriebot.security;

import mg.eni.reseauuniversitaire.messageriebot.entity.User;
import mg.eni.reseauuniversitaire.messageriebot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

// Valide le JWT au moment du CONNECT STOMP.
@Component
@RequiredArgsConstructor
public class JwtChannelInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new AccessDeniedException("Token JWT manquant pour la connexion WebSocket");
            }

            String token = authHeader.substring(7);

            if (!jwtService.estValide(token)) {
                throw new AccessDeniedException("Token JWT invalide ou expire");
            }

            String email = jwtService.extraireEmail(token);
            Optional<User> utilisateur = userRepository.findByEmail(email);

            if (utilisateur.isEmpty()) {
                throw new AccessDeniedException("Utilisateur introuvable");
            }

            var authToken = new UsernamePasswordAuthenticationToken(
                    utilisateur.get(), null, List.of()
            );
            accessor.setUser(authToken);
        }

        return message;
    }
}
