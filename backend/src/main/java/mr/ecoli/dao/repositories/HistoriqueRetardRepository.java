package mr.ecoli.dao.repositories;

import java.util.List;
import java.util.Optional;
import mr.ecoli.dao.entities.HistoriqueRetard;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HistoriqueRetardRepository extends JpaRepository<HistoriqueRetard, Long> {

    List<HistoriqueRetard> findByEleveIdOrderByDateConstatDesc(Long eleveId);

    Optional<HistoriqueRetard> findByEleveIdAndLibelleEcheanceAndDateEcheance(
            Long eleveId, String libelleEcheance, java.time.LocalDate dateEcheance);
}
