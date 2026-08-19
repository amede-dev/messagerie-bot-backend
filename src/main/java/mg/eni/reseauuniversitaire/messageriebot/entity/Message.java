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

    // ========================================================================
    // TYPE DU MESSAGE
    // ========================================================================

    public enum Type {
        TEXTE,
        IMAGE,
        DOCUMENT,
        AUDIO,
        VIDEO,
        SYSTEME
    }

    // ========================================================================
    // STATUT
    // ========================================================================

    public enum Statut {
        ENVOYE,
        RECU,
        LU
    }

    // ========================================================================
    // ID
    // ========================================================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ========================================================================
    // CONVERSATION
    // ========================================================================

    @ManyToOne
    @JoinColumn(
            name = "conversation_id",
            nullable = false
    )
    private Conversation conversation;

    // ========================================================================
    // EXPEDITEUR
    // ========================================================================

    @ManyToOne
    @JoinColumn(
            name = "expediteur_id",
            nullable = false
    )
    private User expediteur;

   

    @Column(columnDefinition = "TEXT")
    private String contenu;

    // ========================================================================
    // TYPE
    // ========================================================================

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Type type = Type.TEXTE;

    // ========================================================================
    // STATUT
    // ========================================================================

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Statut statut = Statut.ENVOYE;

    // ========================================================================
    // MESSAGE PARENT
    // ========================================================================

    private Long messageParentId;

    // ========================================================================
    // DATE D'ENVOI
    // ========================================================================

    @Column(nullable = false)
    private LocalDateTime dateEnvoi = LocalDateTime.now();
}