package mg.eni.reseauuniversitaire.messageriebot.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "planning")
@Getter
@Setter
@NoArgsConstructor
public class Planning {

    public enum Type { COURS, EXAMEN, RATTRAPAGE, ANNONCE }
    public enum Statut { BROUILLON, PUBLIE, ARCHIVE }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String filiere;

    @Column(nullable = false, length = 20)
    private String niveau;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Type type;

    @Column(nullable = false, length = 150)
    private String matiere;

    @Column(name = "date_planning", nullable = false)
    private LocalDate datePlanning;

    private LocalTime heureDebut;
    private LocalTime heureFin;

    @Column(length = 100)
    private String salle;

    @Column(length = 30)
    private String semestre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Statut statut = Statut.BROUILLON;

    @Column(length = 500)
    private String sourceUrl;

    @Column(columnDefinition = "TEXT")
    private String contenuSource;

    @Column(nullable = false)
    private LocalDateTime dateMiseAJour = LocalDateTime.now();
}
