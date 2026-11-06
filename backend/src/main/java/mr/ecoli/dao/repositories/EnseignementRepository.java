package mr.ecoli.dao.repositories;

import java.util.List;
import mr.ecoli.dao.entities.Enseignement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EnseignementRepository extends JpaRepository<Enseignement, Long> {

    @Query("""
            SELECT en FROM Enseignement en
            JOIN FETCH en.classe c
            JOIN FETCH c.niveau
            JOIN FETCH en.matiere
            JOIN FETCH en.enseignant e
            JOIN FETCH e.utilisateur
            WHERE en.enseignant.id = :enseignantId
            """)
    List<Enseignement> findByEnseignantId(@Param("enseignantId") Long enseignantId);

    @Query("""
            SELECT en FROM Enseignement en
            JOIN FETCH en.enseignant e
            JOIN FETCH e.utilisateur
            JOIN FETCH en.matiere
            WHERE en.classe.id = :classeId
            ORDER BY en.matiere.nom
            """)
    List<Enseignement> findByClasseId(@Param("classeId") Long classeId);

    @Query("""
            SELECT en FROM Enseignement en
            JOIN FETCH en.classe c
            JOIN FETCH c.niveau
            JOIN FETCH en.matiere
            JOIN FETCH en.enseignant e
            JOIN FETCH e.utilisateur
            WHERE (:classeId IS NULL OR en.classe.id = :classeId)
            AND (:matiereId IS NULL OR en.matiere.id = :matiereId)
            ORDER BY c.nom, en.matiere.nom
            """)
    List<Enseignement> search(@Param("classeId") Long classeId, @Param("matiereId") Long matiereId);

    @Query("""
            SELECT en FROM Enseignement en
            JOIN FETCH en.classe c
            JOIN FETCH c.niveau
            JOIN FETCH en.matiere
            JOIN FETCH en.enseignant e
            JOIN FETCH e.utilisateur
            WHERE en.id = :id
            """)
    java.util.Optional<Enseignement> findByIdWithDetails(@Param("id") Long id);

    boolean existsByClasseIdAndMatiereId(Long classeId, Long matiereId);

    boolean existsByClasseIdAndMatiereIdAndEnseignantId(
            Long classeId, Long matiereId, Long enseignantId);

    boolean existsByClasseIdAndMatiereIdAndIdNot(Long classeId, Long matiereId, Long id);

    @Query("""
            SELECT COUNT(DISTINCT en.enseignant.id) FROM Enseignement en
            WHERE (:niveauId IS NULL OR en.classe.niveau.id = :niveauId)
            """)
    long countDistinctEnseignants(@Param("niveauId") Long niveauId);
}
