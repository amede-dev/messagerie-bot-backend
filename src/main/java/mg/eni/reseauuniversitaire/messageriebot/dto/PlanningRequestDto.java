package mg.eni.reseauuniversitaire.messageriebot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import mg.eni.reseauuniversitaire.messageriebot.entity.Planning;

import java.time.LocalDate;
import java.time.LocalTime;

public record PlanningRequestDto(
        @NotBlank String filiere,
        @NotBlank String niveau,
        @NotNull Planning.Type type,
        @NotBlank String matiere,
        @NotNull LocalDate datePlanning,
        LocalTime heureDebut,
        LocalTime heureFin,
        String salle,
        String semestre,
        Planning.Statut statut,
        String sourceUrl,
        String contenuSource
) {}
