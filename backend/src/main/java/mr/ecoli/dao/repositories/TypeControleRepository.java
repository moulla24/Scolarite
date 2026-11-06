package mr.ecoli.dao.repositories;

import java.util.List;
import mr.ecoli.dao.entities.TypeControle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TypeControleRepository extends JpaRepository<TypeControle, Long> {

    @Query("""
            SELECT t FROM TypeControle t
            JOIN FETCH t.niveau n
            JOIN FETCH n.ecole
            WHERE (:niveauId IS NULL OR t.niveau.id = :niveauId)
            ORDER BY t.ordre ASC, t.libelle ASC
            """)
    List<TypeControle> findAllFiltered(@Param("niveauId") Long niveauId);

    boolean existsByNiveauIdAndCodeIgnoreCase(Long niveauId, String code);
}
