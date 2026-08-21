package mg.eni.reseauuniversitaire.messageriebot.controller;

import mg.eni.reseauuniversitaire.messageriebot.dto.UserSummaryDto;
import mg.eni.reseauuniversitaire.messageriebot.dto.UserProfileDto;
import mg.eni.reseauuniversitaire.messageriebot.entity.User;
import mg.eni.reseauuniversitaire.messageriebot.repository.ConversationRepository;
import mg.eni.reseauuniversitaire.messageriebot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

// Liste minimale des utilisateurs, pour choisir des participants a une
// conversation. En attendant le vrai annuaire du module Gp6-4.
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final ConversationRepository conversationRepository;

    @GetMapping("/me/profile")
    public UserProfileDto profil(@AuthenticationPrincipal User utilisateurConnecte) {
        String photoUrl = utilisateurConnecte.getPhotoData() != null
                && utilisateurConnecte.getPhotoData().length > 0
                ? "/api/users/" + utilisateurConnecte.getId() + "/photo"
                : null;
        return new UserProfileDto(
                utilisateurConnecte.getId(),
                utilisateurConnecte.getNom(),
                utilisateurConnecte.getPrenom(),
                utilisateurConnecte.getEmail(),
                utilisateurConnecte.getParcours(),
                utilisateurConnecte.getNiveau(),
                photoUrl,
                conversationRepository.compterContactsPrives(utilisateurConnecte.getId()),
                conversationRepository.compterGroupesDe(utilisateurConnecte.getId())
        );
    }

    @GetMapping("/{id}/photo")
    public ResponseEntity<byte[]> photo(@PathVariable Long id) {
        User utilisateur = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));

        if (utilisateur.getPhotoData() == null || utilisateur.getPhotoData().length == 0) {
            return ResponseEntity.notFound().build();
        }

        MediaType type;
        try {
            type = MediaType.parseMediaType(
                    utilisateur.getPhotoContentType() == null
                            ? MediaType.APPLICATION_OCTET_STREAM_VALUE
                            : utilisateur.getPhotoContentType()
            );
        } catch (IllegalArgumentException e) {
            type = MediaType.APPLICATION_OCTET_STREAM;
        }

        return ResponseEntity.ok()
                .contentType(type)
                .body(utilisateur.getPhotoData());
    }

    @GetMapping
    public List<UserSummaryDto> lister(@AuthenticationPrincipal User utilisateurConnecte) {
        return userRepository.findAll().stream()
                .filter(u -> !u.getId().equals(utilisateurConnecte.getId()))
                .map(u -> new UserSummaryDto(u.getId(), u.getNom(), u.getPrenom(), u.getEmail()))
                .toList();
    }
}
