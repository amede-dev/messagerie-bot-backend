package mg.eni.reseauuniversitaire.messageriebot.controller;

import mg.eni.reseauuniversitaire.messageriebot.dto.UserSummaryDto;
import mg.eni.reseauuniversitaire.messageriebot.entity.User;
import mg.eni.reseauuniversitaire.messageriebot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Liste minimale des utilisateurs, pour choisir des participants a une
// conversation. En attendant le vrai annuaire du module Gp6-4.
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @GetMapping
    public List<UserSummaryDto> lister(@AuthenticationPrincipal User utilisateurConnecte) {
        return userRepository.findAll().stream()
                .filter(u -> !u.getId().equals(utilisateurConnecte.getId()))
                .map(u -> new UserSummaryDto(u.getId(), u.getNom(), u.getPrenom(), u.getEmail()))
                .toList();
    }
}