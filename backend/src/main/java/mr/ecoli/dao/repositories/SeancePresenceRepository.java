package mr.ecoli.dao.repositories;

import java.util.List;
import mr.ecoli.dao.entities.SeancePresence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SeancePresenceRepository extends JpaRepository<SeancePresence, Long> {

    @Query("""
            SELECT s FROM SeancePresence s
            JOIN FETCH s.classe c
            JOIN FETCH c.niveau
            JOIN FETCH s.matiere
            JOIN FETCH s.enseignant e
            JOIN FETCH e.utilisateur
            JOIN FETCH s.semaine
            WHERE (:classeId IS NULL OR c.id = :classeId)
              AND s.semaine.id = :semaineId
            ORDER BY s.jourSemaine, s.heureDebut
            """)
    List<SeancePresence> findByFilters(
            @Param("classeId") Long classeId,
            @Param("semaineId") Long semaineId);

    boolean existsBySemaineIdAndClasseIdAndJourSemaineAndHeureDebut(
            Long semaineId, Long classeId, Short jourSemaine, java.time.LocalTime heureDebut);

    @Query("""
            SELECT s FROM SeancePresence s
            JOIN FETCH s.matiere
            JOIN FETCH s.enseignant e
            JOIN FETCH e.utilisateur
            WHERE s.classe.id = :classeId AND s.jourSemaine = :jourSemaine
            ORDER BY s.heureDebut
            """)
    List<SeancePresence> findByClasseAndJourSemaine(
            @Param("classeId") Long classeId,
            @Param("jourSemaine") Short jourSemaine);

    @Query("""
            SELECT COUNT(s) FROM SeancePresence s
            WHERE (:niveauId IS NULL OR s.classe.niveau.id = :niveauId)
            """)
    long countByNiveau(@Param("niveauId") Long niveauId);
}
