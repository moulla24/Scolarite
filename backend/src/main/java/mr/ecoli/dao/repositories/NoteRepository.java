package mr.ecoli.dao.repositories;

import java.util.List;
import java.util.Optional;
import mr.ecoli.dao.entities.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NoteRepository extends JpaRepository<Note, Long> {

    @Query("""
            SELECT n FROM Note n
            JOIN FETCH n.eleve
            JOIN FETCH n.evaluation ev
            JOIN FETCH ev.enseignement en
            JOIN FETCH en.matiere
            JOIN FETCH en.classe c
            JOIN FETCH c.niveau
            WHERE (:classeId IS NULL OR c.id = :classeId)
            AND (:niveauId IS NULL OR c.niveau.id = :niveauId)
            """)
    List<Note> findBulletinNotes(
            @Param("classeId") Long classeId,
            @Param("niveauId") Long niveauId);

    List<Note> findByEvaluationId(Long evaluationId);

    @Query("""
            SELECT n FROM Note n
            JOIN FETCH n.eleve
            JOIN FETCH n.evaluation ev
            JOIN FETCH ev.enseignement en
            JOIN FETCH en.matiere
            WHERE n.eleve.id = :eleveId
            ORDER BY en.matiere.nom, ev.dateEvaluation DESC
            """)
    List<Note> findByEleveId(@Param("eleveId") Long eleveId);

    @Query("""
            SELECT n FROM Note n
            JOIN FETCH n.eleve
            JOIN FETCH n.evaluation ev
            WHERE ev.id IN :evaluationIds
            """)
    List<Note> findByEvaluationIdIn(@Param("evaluationIds") List<Long> evaluationIds);

    @Query("""
            SELECT n FROM Note n
            WHERE n.evaluation.id = :evaluationId AND n.eleve.id = :eleveId
            """)
    Optional<Note> findByEvaluationIdAndEleveId(
            @Param("evaluationId") Long evaluationId,
            @Param("eleveId") Long eleveId);
}
