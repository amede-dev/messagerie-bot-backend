package mg.eni.reseauuniversitaire.messageriebot.controller;

import mg.eni.reseauuniversitaire.messageriebot.entity.Notification;
import mg.eni.reseauuniversitaire.messageriebot.entity.User;
import mg.eni.reseauuniversitaire.messageriebot.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.ResponseEntity;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationRepository notificationRepository;

    @GetMapping
    public List<NotificationResponse> lister(@AuthenticationPrincipal User utilisateurConnecte) {
        return notificationRepository.findByUserIdOrderByDateCreationDesc(utilisateurConnecte.getId())
                .stream().map(NotificationResponse::depuis).toList();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(
            @PathVariable Long id,
            @AuthenticationPrincipal User utilisateurConnecte
    ) {
        notificationRepository.supprimerPourUtilisateur(id, utilisateurConnecte.getId());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> supprimerToutes(
            @AuthenticationPrincipal User utilisateurConnecte
    ) {
        notificationRepository.supprimerToutesPourUtilisateur(utilisateurConnecte.getId());
        return ResponseEntity.noContent().build();
    }

    public record NotificationResponse(Long id, String type, String contenu, boolean lu,
                                       Long referenceId, String dateCreation) {
        static NotificationResponse depuis(Notification notification) {
            return new NotificationResponse(notification.getId(), notification.getType(),
                    notification.getContenu(), notification.isLu(), notification.getReferenceId(),
                    notification.getDateCreation().toString());
        }
    }
}
