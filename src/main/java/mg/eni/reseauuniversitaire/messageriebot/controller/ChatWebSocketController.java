package mg.eni.reseauuniversitaire.messageriebot.controller;

import mg.eni.reseauuniversitaire.messageriebot.dto.MessageRequestDto;
import mg.eni.reseauuniversitaire.messageriebot.dto.MessageResponseDto;
import mg.eni.reseauuniversitaire.messageriebot.entity.User;
import mg.eni.reseauuniversitaire.messageriebot.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;

    public record EnvoiMessageRequest(Long conversationId, String contenu) {
    }

    public record TypingRequest(Long conversationId) {
    }

    @MessageMapping("/chat.send")
    public void envoyer(EnvoiMessageRequest requete, Principal principal) {
        User expediteur = extraireUtilisateur(principal);

        MessageResponseDto message = messageService.envoyer(
                requete.conversationId(),
                expediteur.getId(),
                new MessageRequestDto(requete.contenu(), "TEXTE", null)
        );

        messagingTemplate.convertAndSend("/topic/conversation." + requete.conversationId(), message);
    }

    @MessageMapping("/chat.typing")
    public void notifierFrappe(TypingRequest requete, Principal principal) {
        User expediteur = extraireUtilisateur(principal);
        messagingTemplate.convertAndSend(
                "/topic/conversation." + requete.conversationId() + ".typing",
                new TypingNotification(expediteur.getPrenom())
        );
    }

    private User extraireUtilisateur(Principal principal) {
        if (principal instanceof Authentication auth && auth.getPrincipal() instanceof User user) {
            return user;
        }
        throw new AccessDeniedException("Utilisateur non authentifie sur cette connexion WebSocket");
    }

    public record TypingNotification(String utilisateur) {
    }
}