package mg.eni.reseauuniversitaire.messageriebot.controller;

import mg.eni.reseauuniversitaire.messageriebot.dto.MessageRequestDto;
import mg.eni.reseauuniversitaire.messageriebot.dto.MessageResponseDto;
import mg.eni.reseauuniversitaire.messageriebot.entity.User;
import mg.eni.reseauuniversitaire.messageriebot.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @GetMapping("/api/conversations/{conversationId}/messages")
    public Page<MessageResponseDto> historique(
            @PathVariable Long conversationId,
            @RequestParam(defaultValue = "0") int page
    ) {
        return messageService.historique(conversationId, page);
    }

    // Envoi via REST (fallback si le WebSocket n'est pas disponible).
    // L'envoi normal en production passe plutot par /app/chat.send (voir ChatWebSocketController).
    @PostMapping("/api/conversations/{conversationId}/messages")
    public MessageResponseDto envoyer(
            @PathVariable Long conversationId,
            @Valid @RequestBody MessageRequestDto requete,
            @AuthenticationPrincipal User utilisateur
    ) {
        return messageService.envoyer(conversationId, utilisateur.getId(), requete);
    }

    @PutMapping("/api/messages/{id}/status")
    public void marquerStatut(@PathVariable Long id, @RequestBody StatutRequest requete) {
        messageService.marquerStatut(id, requete.statut());
    }

    public record StatutRequest(String statut) {
    }
}
