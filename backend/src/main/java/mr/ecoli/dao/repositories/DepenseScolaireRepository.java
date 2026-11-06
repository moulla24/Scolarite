package mr.ecoli.dao.repositories;

import java.util.List;
import mr.ecoli.dao.entities.DepenseScolaire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DepenseScolaireRepository extends JpaRepository<DepenseScolaire, Long> {

    @Query("""
            SELECT d FROM DepenseScolaire d
            LEFT JOIN FETCH d.comptable c
            LEFT JOIN FETCH c.utilisateur
            WHERE (:categorie IS NULL OR :categorie = '' OR d.categorie = :categorie)
            ORDER BY d.dateDepense DESC
            """)
    List<DepenseScolaire> findFiltered(@Param("categorie") String categorie);
}
