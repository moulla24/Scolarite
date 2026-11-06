package mr.ecoli.dao.repositories;

import java.util.List;
import mr.ecoli.dao.entities.Comptable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ComptableRepository extends JpaRepository<Comptable, Long> {

    @Query("""
            SELECT c FROM Comptable c
            JOIN FETCH c.utilisateur u
            WHERE (:search IS NULL OR :search = '' OR
                   LOWER(u.nom) LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(u.prenom) LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(c.matricule) LIKE LOWER(CONCAT('%', :search, '%')))
            ORDER BY u.nom, u.prenom
            """)
    List<Comptable> search(@Param("search") String search);

    java.util.Optional<Comptable> findByUtilisateurId(Long utilisateurId);
}
