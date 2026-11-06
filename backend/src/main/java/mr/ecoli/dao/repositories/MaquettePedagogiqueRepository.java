package mr.ecoli.dao.repositories;

import java.util.List;
import mr.ecoli.dao.entities.MaquettePedagogique;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MaquettePedagogiqueRepository extends JpaRepository<MaquettePedagogique, Long> {

    @Query("""
            SELECT m FROM MaquettePedagogique m
            JOIN FETCH m.matiere
            JOIN FETCH m.niveau
            WHERE (:search IS NULL OR :search = '' OR
                   LOWER(m.titre) LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(m.description) LIKE LOWER(CONCAT('%', :search, '%')))
            AND (:matiereId IS NULL OR m.matiere.id = :matiereId)
            AND (:niveauId IS NULL OR m.niveau.id = :niveauId)
            ORDER BY m.dateMaj DESC
            """)
    List<MaquettePedagogique> search(
            @Param("search") String search,
            @Param("matiereId") Long matiereId,
            @Param("niveauId") Long niveauId);
}
