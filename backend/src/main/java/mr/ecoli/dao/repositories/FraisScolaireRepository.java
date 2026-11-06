package mr.ecoli.dao.repositories;

import java.util.List;
import mr.ecoli.dao.entities.FraisScolaire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FraisScolaireRepository extends JpaRepository<FraisScolaire, Long> {

    List<FraisScolaire> findByEcoleIdAndAnneeScolaireIdAndActifTrue(Long ecoleId, Long anneeScolaireId);

    @Query("""
            SELECT f FROM FraisScolaire f
            WHERE f.ecole.id = :ecoleId
              AND f.anneeScolaire.id = :anneeId
              AND f.actif = true
              AND (f.classe IS NULL OR f.classe.id = :classeId)
              AND (f.niveau IS NULL OR f.niveau.id = :niveauId)
            ORDER BY f.typeFrais
            """)
    List<FraisScolaire> findApplicable(
            @Param("ecoleId") Long ecoleId,
            @Param("anneeId") Long anneeId,
            @Param("niveauId") Long niveauId,
            @Param("classeId") Long classeId);
}
