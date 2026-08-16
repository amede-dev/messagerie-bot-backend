package mg.eni.reseauuniversitaire.messageriebot.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "bot_session")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BotSession {

    public enum Statut { ACTIVE, TRANSFEREE_HUMAIN, FERMEE }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /// Contexte libre (ex. derniere intention detectee) stocke en JSON texte.
    @Column(columnDefinition = "TEXT")
    private String contexteConversation;

    @Column(nullable = false)
    private LocalDateTime dateDebut = LocalDateTime.now();

    private LocalDateTime dateFin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Statut statut = Statut.ACTIVE;
}
