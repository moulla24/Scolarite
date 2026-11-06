package mr.ecoli.dao.repositories;

import java.util.List;
import mr.ecoli.dao.entities.Enseignant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EnseignantRepository extends JpaRepository<Enseignant, Long> {

    @Query("""
            SELECT e FROM Enseignant e
            JOIN FETCH e.utilisateur u
            WHERE (:search IS NULL OR :search = '' OR
                   LOWER(u.nom) LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(u.prenom) LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')))
            ORDER BY u.nom, u.prenom
            """)
    List<Enseignant> search(@Param("search") String search);
}
