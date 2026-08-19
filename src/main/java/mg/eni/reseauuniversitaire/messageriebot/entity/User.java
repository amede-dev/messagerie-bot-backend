package mg.eni.reseauuniversitaire.messageriebot.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

// Utilisateur de la plateforme. Dans le projet final du Groupe 6, cette
// entite sera probablement geree par le module d'authentification global
// -- elle est incluse ici pour que le module Bot/Messagerie soit

@Entity
@Table(name = "app_user")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    public enum Role { ETUDIANT, ENSEIGNANT, ADMIN }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String prenom;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String motDePasse; // haché (BCrypt)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.ETUDIANT;

    private String photoUrl;

    @Column(nullable = false)
    private LocalDateTime dateCreation = LocalDateTime.now();

     // ============================================================
    // PRÉSENCE
    // ============================================================

    @Column(nullable = false)
    private boolean enLigne = false;

    private LocalDateTime derniereConnexion;
}
