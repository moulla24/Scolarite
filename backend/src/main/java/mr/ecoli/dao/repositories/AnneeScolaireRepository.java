package mr.ecoli.dao.repositories;

import mr.ecoli.dao.entities.AnneeScolaire;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnneeScolaireRepository extends JpaRepository<AnneeScolaire, Long> {

    Optional<AnneeScolaire> findFirstByActiveTrue();

    boolean existsByEcole_Id(Long ecoleId);

    List<AnneeScolaire> findAllByOrderByDateDebutDesc();

    List<AnneeScolaire> findByEcole_IdAndActiveTrue(Long ecoleId);
}
