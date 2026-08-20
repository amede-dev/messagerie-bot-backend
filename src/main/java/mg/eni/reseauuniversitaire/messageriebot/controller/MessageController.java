package mg.eni.reseauuniversitaire.messageriebot.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mg.eni.reseauuniversitaire.messageriebot.dto.MessageRequestDto;
import mg.eni.reseauuniversitaire.messageriebot.dto.MessageResponseDto;
import mg.eni.reseauuniversitaire.messageriebot.entity.User;
import mg.eni.reseauuniversitaire.messageriebot.service.MessageService;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class MessageController {

    private static final Logger log =
            LoggerFactory.getLogger(MessageController.class);

    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;

    @GetMapping("/api/conversations/{conversationId}/messages")
    public Page<MessageResponseDto> historique(
            @PathVariable Long conversationId,
            @RequestParam(defaultValue = "0") int page
    ) {
        return messageService.historique(conversationId, page);
    }

    @PostMapping("/api/conversations/{conversationId}/messages")
    public MessageResponseDto envoyer(
            @PathVariable Long conversationId,
            @Valid @RequestBody MessageRequestDto requete,
            @AuthenticationPrincipal User utilisateur
    ) {
        MessageResponseDto message = messageService.envoyer(
                conversationId,
                utilisateur.getId(),
                requete
        );

        try {
            messagingTemplate.convertAndSend(
                    "/topic/conversation." + message.conversationId(),
                    message
            );
        } catch (Exception diffusionException) {
            // La diffusion temps réel ne doit pas annuler l'enregistrement
            // du message ni provoquer un HTTP 500 côté mobile.
            log.warn(
                    "Diffusion WebSocket impossible pour le message {}",
                    message.id(),
                    diffusionException
            );
        }

        return message;
    }

    @PutMapping("/api/messages/{id}/status")
    public void marquerStatut(
            @PathVariable Long id,
            @RequestBody StatutRequest requete
    ) {
        MessageResponseDto message = messageService.marquerStatut(id, requete.statut());
        messagingTemplate.convertAndSend(
                "/topic/conversation." + message.conversationId(),
                message
        );
    }

    @DeleteMapping("/api/messages/{id}")
    public void supprimer(
            @PathVariable Long id,
            @AuthenticationPrincipal User utilisateur
    ) {
        messageService.supprimerPourTous(id, utilisateur.getId());
    }

    public record StatutRequest(String statut) {
    }
}
