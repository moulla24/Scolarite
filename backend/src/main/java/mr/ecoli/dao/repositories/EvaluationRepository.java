package mr.ecoli.dao.repositories;

import java.util.List;
import java.util.Optional;
import mr.ecoli.dao.entities.Evaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EvaluationRepository extends JpaRepository<Evaluation, Long> {

    @Query("""
            SELECT e FROM Evaluation e
            JOIN FETCH e.enseignement en
            JOIN FETCH en.classe c
            JOIN FETCH c.niveau
            JOIN FETCH en.matiere
            WHERE (:classeId IS NULL OR c.id = :classeId)
            """)
    List<Evaluation> findByClasseId(@Param("classeId") Long classeId);

    @Query("""
            SELECT e FROM Evaluation e
            JOIN FETCH e.enseignement en
            JOIN FETCH en.classe c
            JOIN FETCH c.niveau
            JOIN FETCH en.matiere
            WHERE c.id = :classeId
            AND (:matiereId IS NULL OR en.matiere.id = :matiereId)
            ORDER BY en.matiere.nom, e.dateEvaluation DESC, e.type
            """)
    List<Evaluation> findByClasseAndMatiere(
            @Param("classeId") Long classeId,
            @Param("matiereId") Long matiereId);

    boolean existsByEnseignement_Id(Long enseignementId);

    @Query("""
            SELECT e FROM Evaluation e
            JOIN FETCH e.enseignement en
            WHERE en.id = :enseignementId
            AND e.type = :type
            AND e.dateEvaluation = :dateEvaluation
            """)
    Optional<Evaluation> findByEnseignementAndTypeAndDate(
            @Param("enseignementId") Long enseignementId,
            @Param("type") String type,
            @Param("dateEvaluation") java.time.LocalDate dateEvaluation);

    @Query("""
            SELECT e FROM Evaluation e
            JOIN FETCH e.enseignement en
            JOIN FETCH en.matiere
            WHERE en.id = :enseignementId
            AND e.type = :type
            AND e.trimestre = :trimestre
            """)
    Optional<Evaluation> findByEnseignementAndTypeAndTrimestre(
            @Param("enseignementId") Long enseignementId,
            @Param("type") String type,
            @Param("trimestre") short trimestre);

    @Query("""
            SELECT e FROM Evaluation e
            JOIN FETCH e.enseignement en
            JOIN FETCH en.classe c
            JOIN FETCH en.matiere
            WHERE c.id = :classeId
            AND en.matiere.id = :matiereId
            AND e.trimestre = :trimestre
            AND e.type IN :types
            """)
    List<Evaluation> findByClasseMatiereTrimestreAndTypes(
            @Param("classeId") Long classeId,
            @Param("matiereId") Long matiereId,
            @Param("trimestre") short trimestre,
            @Param("types") List<String> types);
}
