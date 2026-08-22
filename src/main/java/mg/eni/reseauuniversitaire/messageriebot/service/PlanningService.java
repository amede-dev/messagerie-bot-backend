package mg.eni.reseauuniversitaire.messageriebot.service;

import lombok.RequiredArgsConstructor;
import mg.eni.reseauuniversitaire.messageriebot.dto.PlanningRequestDto;
import mg.eni.reseauuniversitaire.messageriebot.entity.Planning;
import mg.eni.reseauuniversitaire.messageriebot.repository.PlanningRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class PlanningService {

    private static final Pattern NIVEAU = Pattern.compile("\\bL[1-5]\\b", Pattern.CASE_INSENSITIVE);
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final PlanningRepository planningRepository;

    @Value("${eni.facebook.group-url:https://www.facebook.com/groups/162416697294083/}")
    private String sourceFacebook;

    public Planning enregistrer(PlanningRequestDto requete) {
        Planning planning = new Planning();
        planning.setFiliere(normaliser(requete.filiere()).toUpperCase(Locale.ROOT));
        planning.setNiveau(normaliser(requete.niveau()).toUpperCase(Locale.ROOT));
        planning.setType(requete.type());
        planning.setMatiere(requete.matiere().trim());
        planning.setDatePlanning(requete.datePlanning());
        planning.setHeureDebut(requete.heureDebut());
        planning.setHeureFin(requete.heureFin());
        planning.setSalle(requete.salle());
        planning.setSemestre(requete.semestre());
        planning.setStatut(requete.statut() == null ? Planning.Statut.BROUILLON : requete.statut());
        planning.setSourceUrl(requete.sourceUrl() == null ? sourceFacebook : requete.sourceUrl());
        planning.setContenuSource(requete.contenuSource());
        return planningRepository.save(planning);
    }

    public String repondreSiPlanning(String question) {
        String texte = normaliser(question);
        if (!contientMotPlanning(texte)) return null;

        Matcher niveauMatcher = NIVEAU.matcher(question);
        if (!niveauMatcher.find()) {
            return "Pour rechercher le planning, indiquez votre niveau et votre filière, "
                    + "par exemple : « examen de L2 Info cette semaine » .";
        }

        String niveau = niveauMatcher.group().toUpperCase(Locale.ROOT);
        String filiere = extraireFiliere(texte);
        if (filiere == null) {
            return "Pour rechercher le planning, indiquez la filière, par exemple Info ou GB.";
        }

        LocalDate aujourdHui = LocalDate.now();
        LocalDate debut = debutPeriode(texte, aujourdHui);
        LocalDate fin = finPeriode(texte, debut);
        List<Planning> resultats = planningRepository.rechercherPublies(
                filiere, niveau, Planning.Statut.PUBLIE, debut, fin
        );

        if (resultats.isEmpty()) {
            return "Aucun planning officiel publié n'est disponible pour " + niveau + " "
                    + filiere + " sur la période demandée.";
        }

        StringBuilder reponse = new StringBuilder("Planning officiel de ")
                .append(niveau).append(' ').append(filiere).append(" :\n");
        for (Planning planning : resultats) {
            reponse.append("- ").append(planning.getDatePlanning());
            if (planning.getHeureDebut() != null) {
                reponse.append(" ").append(planning.getHeureDebut());
                if (planning.getHeureFin() != null) reponse.append('-').append(planning.getHeureFin());
            }
            reponse.append(" : ").append(planning.getMatiere());
            if (planning.getSalle() != null && !planning.getSalle().isBlank()) {
                reponse.append(" (").append(planning.getSalle()).append(')');
            }
            reponse.append('\n');
        }
        return reponse.toString().trim();
    }

    private boolean contientMotPlanning(String texte) {
        return texte.contains("emploi du temps") || texte.contains("planning")
                || texte.contains("examen") || texte.contains("rattrapage")
                || texte.contains("calendrier");
    }

    private String extraireFiliere(String texte) {
        if (texte.contains("info") || texte.contains("informatique")) return "INFO";
        if (texte.contains("gb") || texte.contains("gestion")) return "GB";
        return null;
    }

    private LocalDate debutPeriode(String texte, LocalDate aujourdHui) {
        Matcher dateMatcher = Pattern.compile("\\b(\\d{2}/\\d{2}/\\d{4})\\b").matcher(texte);
        if (dateMatcher.find()) {
            try { return LocalDate.parse(dateMatcher.group(1), DATE); }
            catch (DateTimeParseException ignored) { }
        }
        if (texte.contains("demain")) return aujourdHui.plusDays(1);
        if (texte.contains("hier")) return aujourdHui.minusDays(1);
        return aujourdHui;
    }

    private LocalDate finPeriode(String texte, LocalDate debut) {
        return texte.contains("semaine") ? debut.plusDays(6) : debut;
    }

    private String normaliser(String valeur) {
        return valeur.trim().toLowerCase(Locale.ROOT);
    }
}
