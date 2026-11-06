package mr.ecoli.dao.repositories;

import java.util.Optional;
import mr.ecoli.dao.entities.ParentDeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParentDeviceTokenRepository extends JpaRepository<ParentDeviceToken, Long> {

    Optional<ParentDeviceToken> findByTuteurIdAndToken(Long tuteurId, String token);
}
