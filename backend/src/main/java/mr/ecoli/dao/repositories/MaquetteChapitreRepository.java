package mr.ecoli.dao.repositories;

import java.util.List;
import mr.ecoli.dao.entities.MaquetteChapitre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MaquetteChapitreRepository extends JpaRepository<MaquetteChapitre, Long> {

    @Query("""
            SELECT c FROM MaquetteChapitre c
            WHERE c.maquetteMatiere.id = :maquetteMatiereId
            ORDER BY c.ordre, c.id
            """)
    List<MaquetteChapitre> findByMaquetteMatiereId(@Param("maquetteMatiereId") Long maquetteMatiereId);

    void deleteByMaquetteMatiere_Id(Long maquetteMatiereId);
}
