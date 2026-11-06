package mr.ecoli.dao.repositories;

import java.util.Optional;
import mr.ecoli.dao.entities.Matiere;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatiereRepository extends JpaRepository<Matiere, Long> {

    Optional<Matiere> findByNomIgnoreCase(String nom);

    Optional<Matiere> findByCodeIgnoreCase(String code);
}
