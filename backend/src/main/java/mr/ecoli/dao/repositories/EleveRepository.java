package mr.ecoli.dao.repositories;

import mr.ecoli.dao.entities.Inscription;

import java.util.List;
import mr.ecoli.dao.entities.Eleve;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EleveRepository extends JpaRepository<Eleve, Long> {

    @Query("""
            SELECT DISTINCT e FROM Eleve e
            JOIN FETCH e.tuteur t
            LEFT JOIN Inscription i ON i.eleve = e
            LEFT JOIN i.classe c
            LEFT JOIN c.niveau n
            WHERE (:search IS NULL OR :search = '' OR
                   LOWER(e.nom) LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(e.prenom) LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(e.matricule) LIKE LOWER(CONCAT('%', :search, '%')))
            AND (:niveauId IS NULL OR n.id = :niveauId)
            AND (:classeId IS NULL OR c.id = :classeId)
            ORDER BY e.nom, e.prenom
            """)
    List<Eleve> search(
            @Param("search") String search,
            @Param("niveauId") Long niveauId,
            @Param("classeId") Long classeId);

    long countByTuteurId(Long tuteurId);

    boolean existsByMatricule(String matricule);

    java.util.Optional<Eleve> findByMatricule(String matricule);

    boolean existsByEcole_Id(Long ecoleId);

    @Query("""
            SELECT e FROM Eleve e
            JOIN FETCH e.ecole
            JOIN FETCH e.tuteur
            WHERE e.tuteur.id = :tuteurId
            ORDER BY e.nom, e.prenom
            """)
    List<Eleve> findByTuteurId(@Param("tuteurId") Long tuteurId);

    boolean existsByIdAndTuteurId(Long id, Long tuteurId);
}
