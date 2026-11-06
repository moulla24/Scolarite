package mr.ecoli.dao.repositories;

import java.util.List;
import mr.ecoli.dao.entities.NotificationComptable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationComptableRepository extends JpaRepository<NotificationComptable, Long> {

    List<NotificationComptable> findByUtilisateurIdOrderByDateCreationDesc(Long utilisateurId);

    long countByUtilisateurIdAndLuFalse(Long utilisateurId);

    @Modifying
    @Query("UPDATE NotificationComptable n SET n.lu = true WHERE n.id = :id AND n.utilisateur.id = :userId")
    int markAsRead(@Param("id") Long id, @Param("userId") Long userId);

    @Modifying
    @Query("UPDATE NotificationComptable n SET n.lu = true WHERE n.utilisateur.id = :userId AND n.lu = false")
    int markAllAsRead(@Param("userId") Long userId);
}
