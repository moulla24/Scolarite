package mr.ecoli.dao.repositories;

import java.util.List;
import mr.ecoli.dao.entities.Ecole;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EcoleRepository extends JpaRepository<Ecole, Long> {

    List<Ecole> findAllByOrderByNomAsc();

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);
}
