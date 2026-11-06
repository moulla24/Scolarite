package mr.ecoli.dao.repositories;

import java.time.LocalDate;
import java.util.List;
import mr.ecoli.dao.entities.Paiement;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaiementRepository extends JpaRepository<Paiement, Long> {

    @Query("""
            SELECT p FROM Paiement p
            JOIN FETCH p.facture f
            JOIN FETCH f.eleve e
            JOIN FETCH e.tuteur
            ORDER BY p.datePaiement DESC, p.id DESC
            """)
    List<Paiement> findRecent(Pageable pageable);

    @Query("""
            SELECT p FROM Paiement p
            JOIN FETCH p.facture f
            JOIN FETCH f.eleve e
            JOIN FETCH e.tuteur
            WHERE (:niveauId IS NULL OR EXISTS (
                SELECT 1 FROM Inscription i
                WHERE i.eleve.id = e.id
                  AND i.statut = 'ACTIF'
                  AND i.classe.niveau.id = :niveauId
            ))
            ORDER BY p.datePaiement DESC, p.id DESC
            """)
    List<Paiement> findRecentByNiveau(@Param("niveauId") Long niveauId, Pageable pageable);

    @Query("""
            SELECT p FROM Paiement p
            JOIN FETCH p.facture f
            JOIN FETCH f.eleve e
            JOIN FETCH e.tuteur
            LEFT JOIN Inscription i ON i.eleve = e AND i.statut = 'ACTIF'
            LEFT JOIN i.classe c
            LEFT JOIN c.niveau n
            WHERE (:from IS NULL OR p.datePaiement >= :from)
              AND (:to IS NULL OR p.datePaiement <= :to)
              AND (:mode IS NULL OR :mode = '' OR p.modePaiement = :mode)
              AND (:eleveId IS NULL OR e.id = :eleveId)
              AND (:classeId IS NULL OR c.id = :classeId)
              AND (:niveauId IS NULL OR n.id = :niveauId)
              AND (:search IS NULL OR :search = '' OR
                   LOWER(f.numero) LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(e.nom) LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(e.prenom) LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(e.matricule) LIKE LOWER(CONCAT('%', :search, '%')))
            ORDER BY p.datePaiement DESC, p.id DESC
            """)
    List<Paiement> findFiltered(
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            @Param("mode") String mode,
            @Param("niveauId") Long niveauId,
            @Param("classeId") Long classeId,
            @Param("eleveId") Long eleveId,
            @Param("search") String search,
            Pageable pageable);

    @Query("""
            SELECT COUNT(p) FROM Paiement p
            WHERE p.datePaiement >= :from AND p.datePaiement <= :to
            """)
    long countBetween(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("""
            SELECT COALESCE(SUM(p.montant), 0) FROM Paiement p
            WHERE p.datePaiement >= :from AND p.datePaiement <= :to
            """)
    java.math.BigDecimal sumBetween(@Param("from") LocalDate from, @Param("to") LocalDate to);

    List<Paiement> findByFactureIdOrderByDatePaiementDesc(Long factureId);
}
