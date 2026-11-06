package mr.ecoli.dao.repositories;

import java.util.List;
import mr.ecoli.dao.entities.EcheanceMensuelle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EcheanceMensuelleRepository extends JpaRepository<EcheanceMensuelle, Long> {

    List<EcheanceMensuelle> findByEleveIdAndAnneeScolaireIdOrderByMoisAsc(Long eleveId, Long anneeScolaireId);

    List<EcheanceMensuelle> findByFactureIdOrderByDateEcheanceAsc(Long factureId);

    List<EcheanceMensuelle> findByEleveIdOrderByDateEcheanceDesc(Long eleveId);

    boolean existsByEleveIdAndMoisAndTypeFrais(Long eleveId, java.time.LocalDate mois, String typeFrais);
}
