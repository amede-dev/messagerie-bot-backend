package mg.eni.reseauuniversitaire.messageriebot.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bot_intent")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BotIntent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ElementCollection
    @CollectionTable(name = "bot_intent_mots_cles", joinColumns = @JoinColumn(name = "bot_intent_id"))
    @Column(name = "mot_cle")
    private List<String> motsClesDeclencheurs = new ArrayList<>();

    @Column(columnDefinition = "TEXT", nullable = false)
    private String reponseTexte;

    private String actionAssociee;

    // Suggestions de reponse rapide (chips) affichees sous la reponse du bot.
    @ElementCollection
    @CollectionTable(name = "bot_intent_suggestions", joinColumns = @JoinColumn(name = "bot_intent_id"))
    @Column(name = "suggestion")
    private List<String> suggestions = new ArrayList<>();

    private boolean actif = true;
}
