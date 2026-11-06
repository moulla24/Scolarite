package mr.ecoli.dao.repositories;

import java.util.List;
import java.util.Optional;
import mr.ecoli.dao.entities.NotificationParent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationParentRepository extends JpaRepository<NotificationParent, Long> {

    @Query("""
            SELECT n FROM NotificationParent n
            LEFT JOIN FETCH n.eleve
            WHERE n.tuteur.id = :tuteurId
            ORDER BY n.dateCreation DESC
            """)
    List<NotificationParent> findByTuteurId(@Param("tuteurId") Long tuteurId);

    long countByTuteurIdAndLuFalse(Long tuteurId);

    Optional<NotificationParent> findByIdAndTuteurId(Long id, Long tuteurId);

    @Modifying
    @Query("UPDATE NotificationParent n SET n.lu = true WHERE n.tuteur.id = :tuteurId AND n.lu = false")
    int markAllRead(@Param("tuteurId") Long tuteurId);
}
