package mr.ecoli.dao.repositories;

import java.util.Optional;
import mr.ecoli.dao.entities.MaquetteClasse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MaquetteClasseRepository extends JpaRepository<MaquetteClasse, Long> {

    @Query("""
            SELECT m FROM MaquetteClasse m
            JOIN FETCH m.classe c
            JOIN FETCH c.niveau
            WHERE c.id = :classeId
            """)
    Optional<MaquetteClasse> findByClasseId(@Param("classeId") Long classeId);
}
