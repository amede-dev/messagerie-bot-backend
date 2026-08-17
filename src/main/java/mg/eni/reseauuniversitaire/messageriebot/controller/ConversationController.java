package mg.eni.reseauuniversitaire.messageriebot.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import mg.eni.reseauuniversitaire.messageriebot.dto.ConversationRequestDto;
import mg.eni.reseauuniversitaire.messageriebot.dto.ConversationResponseDto;
import mg.eni.reseauuniversitaire.messageriebot.dto.UserSummaryDto;
import mg.eni.reseauuniversitaire.messageriebot.entity.User;
import mg.eni.reseauuniversitaire.messageriebot.service.ConversationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;

    @GetMapping
    public List<ConversationResponseDto> lister(
            @AuthenticationPrincipal User utilisateur
    ) {
        return conversationService.listerConversationsDe(utilisateur.getId());
    }

    @PostMapping
    public ResponseEntity<ConversationResponseDto> creer(
            @Valid @RequestBody ConversationRequestDto requete,
            @AuthenticationPrincipal User utilisateur
    ) {
        return ResponseEntity.ok(
                conversationService.creer(requete, utilisateur.getId())
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> quitter(
            @PathVariable Long id,
            @AuthenticationPrincipal User utilisateur
    ) {
        conversationService.quitter(id, utilisateur.getId());
        return ResponseEntity.noContent().build();
    }

    // Affiche les vrais participants du groupe.
    @GetMapping("/{id}/participants")
    public List<UserSummaryDto> listerParticipants(
            @PathVariable Long id,
            @AuthenticationPrincipal User utilisateur
    ) {
        return conversationService.listerParticipants(id, utilisateur.getId());
    }

    // Ajoute un utilisateur au groupe.
    @PostMapping("/{id}/participants")
    public ResponseEntity<Void> ajouterParticipant(
            @PathVariable Long id,
            @Valid @RequestBody AjouterParticipantRequest requete,
            @AuthenticationPrincipal User utilisateur
    ) {
        conversationService.ajouterParticipant(
                id,
                requete.userId(),
                utilisateur.getId()
        );
        return ResponseEntity.noContent().build();
    }

    public record AjouterParticipantRequest(
            @NotNull Long userId
    ) {}
}