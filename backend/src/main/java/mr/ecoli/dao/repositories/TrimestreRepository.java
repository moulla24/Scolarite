package mr.ecoli.dao.repositories;

import java.util.List;
import mr.ecoli.dao.entities.Trimestre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TrimestreRepository extends JpaRepository<Trimestre, Long> {

    @Query("""
            SELECT t FROM Trimestre t
            JOIN FETCH t.anneeScolaire a
            JOIN FETCH a.ecole
            WHERE (:anneeScolaireId IS NULL OR t.anneeScolaire.id = :anneeScolaireId)
            ORDER BY t.anneeScolaire.id DESC, t.numero ASC
            """)
    List<Trimestre> findAllFiltered(@Param("anneeScolaireId") Long anneeScolaireId);

    boolean existsByAnneeScolaireIdAndNumero(Long anneeScolaireId, Short numero);

    java.util.Optional<Trimestre> findByAnneeScolaireIdAndNumero(Long anneeScolaireId, Short numero);
}
