package mg.eni.reseauuniversitaire.messageriebot.config;

import mg.eni.reseauuniversitaire.messageriebot.entity.BotIntent;
import mg.eni.reseauuniversitaire.messageriebot.entity.User;
import mg.eni.reseauuniversitaire.messageriebot.repository.BotIntentRepository;
import mg.eni.reseauuniversitaire.messageriebot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

// Insere quelques donnees de test au demarrage SI la base est vide, pour
// pouvoir tester l'API immediatement sans tout creer a la main.

@Component
@RequiredArgsConstructor
public class DemoDataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final BotIntentRepository botIntentRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            User rina = new User();
            rina.setNom("Rakoto");
            rina.setPrenom("Rina");
            rina.setEmail("rina@univ.mg");
            rina.setMotDePasse(passwordEncoder.encode("password123"));
            rina.setRole(User.Role.ETUDIANT);
            userRepository.save(rina);

            User hery = new User();
            hery.setNom("Rakoto");
            hery.setPrenom("Hery");
            hery.setEmail("hery@univ.mg");
            hery.setMotDePasse(passwordEncoder.encode("password123"));
            hery.setRole(User.Role.ETUDIANT);
            userRepository.save(hery);

            System.out.println(">>> Utilisateurs de demo crees : rina@univ.mg / hery@univ.mg (mdp: password123)");
        }

        if (botIntentRepository.count() == 0) {
            BotIntent emploiDuTemps = new BotIntent();
            emploiDuTemps.setMotsClesDeclencheurs(List.of("emploi du temps", "cours", "planning"));
            emploiDuTemps.setReponseTexte("Voici tes cours du jour : 08h-10h Algo (salle B2), 10h30-12h30 Base de donnees (salle A1).");
            emploiDuTemps.setSuggestions(List.of("Emploi du temps demain", "Contacter un humain"));
            botIntentRepository.save(emploiDuTemps);

            BotIntent horaires = new BotIntent();
            horaires.setMotsClesDeclencheurs(List.of("horaire", "ouverture", "bibliotheque"));
            horaires.setReponseTexte("La bibliotheque universitaire est ouverte de 8h a 18h, du lundi au vendredi.");
            horaires.setSuggestions(List.of("Emploi du temps", "Contacter un humain"));
            botIntentRepository.save(horaires);

            System.out.println(">>> Intents de demo crees pour le bot (mots-cles : emploi du temps, horaire...)");
        }
    }
}
