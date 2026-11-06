package mr.ecoli.dao.repositories;

import java.util.List;
import java.util.Optional;
import mr.ecoli.dao.entities.MaquetteMatiere;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MaquetteMatiereRepository extends JpaRepository<MaquetteMatiere, Long> {

    @Query("""
            SELECT mm FROM MaquetteMatiere mm
            JOIN FETCH mm.matiere
            WHERE mm.maquetteClasse.id = :maquetteClasseId
            ORDER BY mm.matiere.nom
            """)
    List<MaquetteMatiere> findByMaquetteClasseId(@Param("maquetteClasseId") Long maquetteClasseId);

    @Query("""
            SELECT mm FROM MaquetteMatiere mm
            JOIN FETCH mm.matiere
            JOIN FETCH mm.maquetteClasse mc
            JOIN FETCH mc.classe c
            JOIN FETCH c.niveau
            WHERE mm.id = :id
            """)
    Optional<MaquetteMatiere> findByIdWithDetails(@Param("id") Long id);

    boolean existsByMaquetteClasse_IdAndMatiere_Id(Long maquetteClasseId, Long matiereId);
}
