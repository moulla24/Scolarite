package mr.ecoli.dao.repositories;

import java.util.List;
import mr.ecoli.dao.entities.LigneFacture;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LigneFactureRepository extends JpaRepository<LigneFacture, Long> {
    List<LigneFacture> findByFactureId(Long factureId);
}
