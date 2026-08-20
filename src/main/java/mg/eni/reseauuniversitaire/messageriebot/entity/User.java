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

    // Parcours et niveau ENI sélectionnés lors de la création du compte.
    // Nullable pour permettre la mise à jour des utilisateurs déjà existants.
    @Column(length = 120)
    private String parcours;

    @Column(length = 20)
    private String niveau;

    private String photoUrl;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "photo_data", columnDefinition = "bytea")
    private byte[] photoData;

    @Column(name = "photo_content_type", length = 100)
    private String photoContentType;

    @Column(nullable = false)
    private LocalDateTime dateCreation = LocalDateTime.now();

    
    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean enLigne = false;

    private LocalDateTime derniereConnexion;
}
