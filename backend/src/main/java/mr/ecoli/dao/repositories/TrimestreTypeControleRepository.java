package mr.ecoli.dao.repositories;

import java.util.List;
import java.util.Optional;
import mr.ecoli.dao.entities.TrimestreTypeControle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TrimestreTypeControleRepository extends JpaRepository<TrimestreTypeControle, Long> {

    @Query("""
            SELECT q FROM TrimestreTypeControle q
            JOIN FETCH q.typeControle t
            JOIN FETCH t.niveau
            WHERE q.trimestre.id = :trimestreId
              AND t.niveau.id = :niveauId
            ORDER BY t.ordre ASC, t.libelle ASC
            """)
    List<TrimestreTypeControle> findByTrimestreAndNiveau(
            @Param("trimestreId") Long trimestreId, @Param("niveauId") Long niveauId);

    Optional<TrimestreTypeControle> findByTrimestreIdAndTypeControleId(Long trimestreId, Long typeControleId);
}
