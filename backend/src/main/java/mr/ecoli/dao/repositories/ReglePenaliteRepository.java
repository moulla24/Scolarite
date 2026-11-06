package mr.ecoli.dao.repositories;

import java.util.List;
import java.util.Optional;
import mr.ecoli.dao.entities.ReglePenalite;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReglePenaliteRepository extends JpaRepository<ReglePenalite, Long> {

    List<ReglePenalite> findByEcoleIdAndActifTrueOrderByJoursRetardMinAsc(Long ecoleId);
}
