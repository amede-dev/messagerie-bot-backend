package mg.eni.reseauuniversitaire.messageriebot.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "conversation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Conversation {

    public enum Type { PRIVEE, GROUPE }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Type type;

    private String nom; // null pour les conversations privees

    /// Reference optionnelle vers un groupe/classe/club du module Gp6-4.
    private Long groupeLieId;

    @ManyToOne
    @JoinColumn(name = "createur_id", nullable = false)
    private User createur;

    @Column(nullable = false)
    private LocalDateTime dateCreation = LocalDateTime.now();

    private boolean archivee = false;

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ConversationParticipant> participants = new ArrayList<>();
}
