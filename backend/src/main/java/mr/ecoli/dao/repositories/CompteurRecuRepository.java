package mr.ecoli.dao.repositories;

import java.util.List;
import java.util.Optional;
import mr.ecoli.dao.entities.CompteurRecu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

public interface CompteurRecuRepository extends JpaRepository<CompteurRecu, Integer> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM CompteurRecu c WHERE c.annee = :annee")
    Optional<CompteurRecu> findForUpdate(@Param("annee") int annee);
}
