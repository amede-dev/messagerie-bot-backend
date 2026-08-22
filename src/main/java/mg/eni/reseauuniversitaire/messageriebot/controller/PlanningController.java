package mg.eni.reseauuniversitaire.messageriebot.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mg.eni.reseauuniversitaire.messageriebot.dto.PlanningRequestDto;
import mg.eni.reseauuniversitaire.messageriebot.entity.Planning;
import mg.eni.reseauuniversitaire.messageriebot.entity.User;
import mg.eni.reseauuniversitaire.messageriebot.service.PlanningService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/plannings")
@RequiredArgsConstructor
public class PlanningController {

    private final PlanningService planningService;

    /**
     * Import contrôlé d'une ligne officielle. L'interface d'administration
     * pourra appeler cet endpoint après validation d'une publication ENI.
     */
    @PostMapping
    public Planning creer(
            @Valid @RequestBody PlanningRequestDto requete,
            @AuthenticationPrincipal User utilisateur
    ) {
        if (utilisateur == null || utilisateur.getRole() != User.Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Seul un administrateur peut publier un planning.");
        }
        return planningService.enregistrer(requete);
    }
}
