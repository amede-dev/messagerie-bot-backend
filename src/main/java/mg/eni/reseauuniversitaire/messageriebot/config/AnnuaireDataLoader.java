package mg.eni.reseauuniversitaire.messageriebot.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import mg.eni.reseauuniversitaire.messageriebot.entity.User;
import mg.eni.reseauuniversitaire.messageriebot.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AnnuaireDataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;

    @Value("${app.annuaire-data.enabled:false}")
    private boolean annuaireDataEnabled;

    @Override
    public void run(String... args) throws Exception {
        if (!annuaireDataEnabled) {
            System.out.println(">>> Import automatique de l'annuaire désactivé.");
            return;
        }

        var fichier = new ClassPathResource("data/listebase.json");

        List<UtilisateurAnnuaire> utilisateurs = objectMapper.readValue(
                fichier.getInputStream(),
                new TypeReference<List<UtilisateurAnnuaire>>() {}
        );

        int ajoutes = 0;

        for (UtilisateurAnnuaire donnees : utilisateurs) {
            if (userRepository.findByEmail(donnees.email()).isPresent()) {
                continue;
            }

            User utilisateur = new User();
            utilisateur.setNom(donnees.nom());
            utilisateur.setPrenom(donnees.prenom());
            utilisateur.setEmail(donnees.email());
            utilisateur.setRole(
                    donnees.role() == null
                            ? User.Role.ETUDIANT
                            : User.Role.valueOf(donnees.role())
            );

            // Mot de passe temporaire : à changer après la première connexion.
            utilisateur.setMotDePasse(
                    passwordEncoder.encode("ChangerMoi2026!")
            );

            userRepository.save(utilisateur);
            ajoutes++;
        }

        System.out.println(">>> Annuaire JSON importé : "
                + ajoutes + " utilisateurs ajoutés.");
    }

    private record UtilisateurAnnuaire(
            String nom,
            String prenom,
            String email,
            String role
    ) {}
}
