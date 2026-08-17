package mg.eni.reseauuniversitaire.messageriebot.config;

import lombok.RequiredArgsConstructor;
import mg.eni.reseauuniversitaire.messageriebot.entity.User;
import mg.eni.reseauuniversitaire.messageriebot.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class AnnuaireDataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        var fichier = new ClassPathResource("data/listebase.txt");

        try (var lecteur = new BufferedReader(
                new InputStreamReader(fichier.getInputStream(), StandardCharsets.UTF_8))) {

            String ligne;
            int numero = 1;
            int ajoutes = 0;

            while ((ligne = lecteur.readLine()) != null) {
                ligne = ligne.trim();
                if (ligne.isEmpty()) continue;

                String[] morceaux = ligne.split("\\s+");
                String nom = morceaux[0];
                String prenom = morceaux.length > 1
                        ? String.join(" ", java.util.Arrays.copyOfRange(morceaux, 1, morceaux.length))
                        : "Étudiant";

                String email = "etudiant." + numero++ + "@eni.mg";

                if (userRepository.findByEmail(email).isPresent()) {
                    continue;
                }

                User utilisateur = new User();
                utilisateur.setNom(nom);
                utilisateur.setPrenom(prenom);
                utilisateur.setEmail(email);
                utilisateur.setMotDePasse(
                        passwordEncoder.encode("ChangerMoi2026!")
                );
                utilisateur.setRole(User.Role.ETUDIANT);

                userRepository.save(utilisateur);
                ajoutes++;
            }

            System.out.println(">>> Annuaire importé : " + ajoutes + " utilisateurs ajoutés.");
        }
    }
}