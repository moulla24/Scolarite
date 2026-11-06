package mr.ecoli.dao.repositories;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import mr.ecoli.dao.entities.PresenceEleve;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PresenceEleveRepository extends JpaRepository<PresenceEleve, Long> {

    @Query("""
            SELECT p FROM PresenceEleve p
            JOIN FETCH p.eleve
            WHERE p.classe.id = :classeId
              AND p.dateSeance = :date
              AND p.plage IS NOT NULL
            """)
    List<PresenceEleve> findByClasseIdAndDate(
            @Param("classeId") Long classeId,
            @Param("date") LocalDate date);

    Optional<PresenceEleve> findByClasse_IdAndEleve_IdAndDateSeanceAndPlage(
            Long classeId, Long eleveId, LocalDate dateSeance, String plage);

    void deleteByClasse_IdAndEleve_IdAndDateSeanceAndPlage(
            Long classeId, Long eleveId, LocalDate dateSeance, String plage);

    @Query("""
            SELECT p FROM PresenceEleve p
            LEFT JOIN FETCH p.seance s
            LEFT JOIN FETCH s.matiere
            LEFT JOIN FETCH p.classe
            WHERE p.eleve.id = :eleveId
            ORDER BY p.dateSeance DESC, p.plage
            """)
    List<PresenceEleve> findByEleveIdOrderByDateDesc(@Param("eleveId") Long eleveId);
}
