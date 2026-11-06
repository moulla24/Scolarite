package mr.ecoli.dao.repositories;

import java.util.List;
import mr.ecoli.dao.entities.RemiseEleve;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RemiseEleveRepository extends JpaRepository<RemiseEleve, Long> {

    List<RemiseEleve> findByEleveIdAndActifTrue(Long eleveId);
}
