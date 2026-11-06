package mr.ecoli.dao.repositories;

import java.util.List;
import mr.ecoli.dao.entities.Tuteur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TuteurRepository extends JpaRepository<Tuteur, Long> {

    java.util.Optional<Tuteur> findByUtilisateurId(Long utilisateurId);

    java.util.Optional<Tuteur> findByNomAndTelephone(String nom, String telephone);

    @Query("""
            SELECT t FROM Tuteur t
            WHERE (:search IS NULL OR :search = '' OR
                   LOWER(t.nom) LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(t.prenom) LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(t.email) LIKE LOWER(CONCAT('%', :search, '%')) OR
                   t.telephone LIKE CONCAT('%', :search, '%'))
            ORDER BY t.nom, t.prenom
            """)
    List<Tuteur> search(@Param("search") String search);
}
