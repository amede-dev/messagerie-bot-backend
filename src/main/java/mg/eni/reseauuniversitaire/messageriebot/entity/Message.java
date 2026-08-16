package mg.eni.reseauuniversitaire.messageriebot.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "message")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Message {

    public enum Type { TEXTE, IMAGE, DOCUMENT, SYSTEME }
    public enum Statut { ENVOYE, RECU, LU }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @ManyToOne
    @JoinColumn(name = "expediteur_id", nullable = false)
    private User expediteur;

    @Column(columnDefinition = "TEXT")
    private String contenu;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Type type = Type.TEXTE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Statut statut = Statut.ENVOYE;

    private Long messageParentId;

    @Column(nullable = false)
    private LocalDateTime dateEnvoi = LocalDateTime.now();
}
