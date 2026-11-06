package mr.ecoli.dao.repositories;

import mr.ecoli.dao.entities.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {

    boolean existsByEmail(String email);

    java.util.Optional<Utilisateur> findByEmailIgnoreCase(String email);
}
