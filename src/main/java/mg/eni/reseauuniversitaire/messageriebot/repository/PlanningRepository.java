package mg.eni.reseauuniversitaire.messageriebot.repository;

import mg.eni.reseauuniversitaire.messageriebot.entity.Planning;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface PlanningRepository extends JpaRepository<Planning, Long> {

    @Query("select p from Planning p where upper(p.filiere) = upper(:filiere) "
            + "and upper(p.niveau) = upper(:niveau) "
            + "and p.statut = :statut "
            + "and p.datePlanning between :debut and :fin "
            + "order by p.datePlanning, p.heureDebut")
    List<Planning> rechercherPublies(
            @Param("filiere") String filiere,
            @Param("niveau") String niveau,
            @Param("statut") Planning.Statut statut,
            @Param("debut") LocalDate debut,
            @Param("fin") LocalDate fin
    );
}
