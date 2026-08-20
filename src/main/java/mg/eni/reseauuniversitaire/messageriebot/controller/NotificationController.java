package mg.eni.reseauuniversitaire.messageriebot.controller;

import mg.eni.reseauuniversitaire.messageriebot.entity.Notification;
import mg.eni.reseauuniversitaire.messageriebot.entity.User;
import mg.eni.reseauuniversitaire.messageriebot.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    public record NotificationResponse(Long id, String type, String contenu, boolean lu,
                                       Long referenceId, String dateCreation) {
        static NotificationResponse depuis(Notification notification) {
            return new NotificationResponse(notification.getId(), notification.getType(),
                    notification.getContenu(), notification.isLu(), notification.getReferenceId(),
                    notification.getDateCreation().toString());
        }
    }
}
