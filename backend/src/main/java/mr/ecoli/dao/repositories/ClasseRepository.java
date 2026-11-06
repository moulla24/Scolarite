package mr.ecoli.dao.repositories;

import mr.ecoli.dao.entities.Inscription;

import java.util.List;
import mr.ecoli.dao.entities.Classe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClasseRepository extends JpaRepository<Classe, Long> {

    @Query("""
            SELECT c FROM Classe c
            JOIN FETCH c.niveau n
            WHERE (:niveauId IS NULL OR n.id = :niveauId)
            AND (:search IS NULL OR :search = '' OR
                 LOWER(c.nom) LIKE LOWER(CONCAT('%', :search, '%')) OR
                 LOWER(n.nom) LIKE LOWER(CONCAT('%', :search, '%')))
            ORDER BY n.nom, c.nom
            """)
    List<Classe> search(@Param("search") String search, @Param("niveauId") Long niveauId);

    @Query("SELECT COUNT(i) FROM Inscription i WHERE i.classe.id = :classeId AND i.statut = 'ACTIF'")
    long countActiveStudents(@Param("classeId") Long classeId);

    @Query("""
            SELECT COUNT(c) FROM Classe c
            WHERE (:niveauId IS NULL OR c.niveau.id = :niveauId)
            """)
    long countByNiveau(@Param("niveauId") Long niveauId);
}
