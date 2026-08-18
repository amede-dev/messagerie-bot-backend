package mg.eni.reseauuniversitaire.messageriebot.config;

import mg.eni.reseauuniversitaire.messageriebot.entity.BotIntent;
import mg.eni.reseauuniversitaire.messageriebot.repository.BotIntentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DemoDataLoader implements CommandLineRunner {

    private final BotIntentRepository botIntentRepository;

    @Override
    public void run(String... args) {
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