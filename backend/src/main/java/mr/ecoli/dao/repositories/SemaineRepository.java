package mr.ecoli.dao.repositories;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import mr.ecoli.dao.entities.Semaine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SemaineRepository extends JpaRepository<Semaine, Long> {

    List<Semaine> findByTrimestreIdOrderByNumeroAsc(Long trimestreId);

    boolean existsByTrimestreId(Long trimestreId);

    @Query("""
            SELECT s FROM Semaine s
            JOIN s.trimestre tr
            WHERE tr.id = :trimestreId
              AND :date BETWEEN s.dateDebut AND s.dateFin
            ORDER BY s.numero
            """)
    Optional<Semaine> findByTrimestreIdAndDate(
            @Param("trimestreId") Long trimestreId,
            @Param("date") LocalDate date);

    @Query("""
            SELECT s FROM Semaine s
            JOIN s.trimestre tr
            WHERE :date BETWEEN tr.dateDebut AND tr.dateFin
              AND :date BETWEEN s.dateDebut AND s.dateFin
            ORDER BY tr.numero, s.numero
            """)
    Optional<Semaine> findCurrentForDate(@Param("date") LocalDate date);

    long countByTrimestreId(Long trimestreId);
}
