package mr.ecoli.dao.repositories;

import java.util.List;
import java.util.Optional;
import mr.ecoli.dao.entities.DemandePaiement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DemandePaiementRepository extends JpaRepository<DemandePaiement, Long> {

    boolean existsByFactureIdAndStatut(Long factureId, String statut);

    @Query("""
            SELECT d FROM DemandePaiement d
            JOIN FETCH d.facture f
            JOIN FETCH f.eleve e
            WHERE d.id = :id AND d.tuteur.id = :tuteurId
            """)
    java.util.Optional<DemandePaiement> findByIdAndTuteurId(
            @Param("id") Long id, @Param("tuteurId") Long tuteurId);

    @Query("""
            SELECT d FROM DemandePaiement d
            JOIN FETCH d.facture f
            JOIN FETCH f.eleve e
            JOIN FETCH e.tuteur
            WHERE d.tuteur.id = :tuteurId
            ORDER BY d.dateCreation DESC
            """)
    List<DemandePaiement> findByTuteurId(@Param("tuteurId") Long tuteurId);

    @Query("""
            SELECT d FROM DemandePaiement d
            JOIN FETCH d.facture f
            JOIN FETCH f.eleve e
            JOIN FETCH e.tuteur
            WHERE (:statut IS NULL OR :statut = '' OR d.statut = :statut)
            ORDER BY d.dateCreation DESC
            """)
    List<DemandePaiement> findFiltered(@Param("statut") String statut);

    @Query("""
            SELECT d FROM DemandePaiement d
            JOIN FETCH d.facture f
            JOIN FETCH f.eleve e
            JOIN FETCH e.tuteur
            WHERE d.id = :id
            """)
    Optional<DemandePaiement> findByIdWithDetails(@Param("id") Long id);

    long countByStatut(String statut);
}
