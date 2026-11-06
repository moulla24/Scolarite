package mr.ecoli.dao.repositories;

import java.util.List;
import mr.ecoli.dao.entities.Niveau;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NiveauRepository extends JpaRepository<Niveau, Long> {

    List<Niveau> findAllByOrderByNomAsc();

    boolean existsByEcole_Id(Long ecoleId);
}
