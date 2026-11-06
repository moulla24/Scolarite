package mr.ecoli.dao.repositories;

import java.util.List;
import java.util.Optional;
import mr.ecoli.dao.entities.Personnel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PersonnelRepository extends JpaRepository<Personnel, Long> {

    @Query("""
            SELECT p FROM Personnel p
            LEFT JOIN FETCH p.enseignant
            WHERE (:categorie IS NULL OR :categorie = '' OR p.categorie = :categorie)
              AND (:statut IS NULL OR :statut = '' OR p.statut = :statut)
              AND (:search IS NULL OR :search = '' OR
                   LOWER(p.nom) LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(p.prenom) LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(p.matricule) LIKE LOWER(CONCAT('%', :search, '%')))
            ORDER BY p.categorie, p.nom, p.prenom
            """)
    List<Personnel> search(
            @Param("categorie") String categorie,
            @Param("statut") String statut,
            @Param("search") String search);

    Optional<Personnel> findByEnseignantId(Long enseignantId);

    long countByStatut(String statut);
}
