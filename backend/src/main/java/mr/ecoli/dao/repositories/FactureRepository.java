package mr.ecoli.dao.repositories;

import java.math.BigDecimal;
import java.util.List;
import mr.ecoli.dao.entities.Facture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FactureRepository extends JpaRepository<Facture, Long> {

    boolean existsByEleveIdAndAnneeScolaire_Id(Long eleveId, Long anneeScolaireId);

    @Query("""
            SELECT COALESCE(SUM(f.montantTotal - f.montantPaye), 0)
            FROM Facture f
            WHERE f.eleve.id = :eleveId AND (f.montantTotal - f.montantPaye) > 0
            """)
    BigDecimal sumResteAPayerByEleveId(@Param("eleveId") Long eleveId);

    @Query("""
            SELECT COALESCE(SUM(f.montantTotal - f.montantPaye), 0)
            FROM Facture f
            WHERE f.eleve.tuteur.id = :tuteurId AND (f.montantTotal - f.montantPaye) > 0
            """)
    BigDecimal sumResteAPayerByTuteurId(@Param("tuteurId") Long tuteurId);

    List<Facture> findByEleveIdOrderByDateEmissionDesc(Long eleveId);

    @Query("""
            SELECT f FROM Facture f
            JOIN FETCH f.eleve
            WHERE f.eleve.tuteur.id = :tuteurId
            ORDER BY f.dateEmission DESC
            """)
    List<Facture> findByTuteurId(@Param("tuteurId") Long tuteurId);

    java.util.Optional<Facture> findByIdAndEleve_Tuteur_Id(Long id, Long tuteurId);

    @Query("""
            SELECT COALESCE(SUM(f.montantTotal - f.montantPaye), 0)
            FROM Facture f
            WHERE (f.montantTotal - f.montantPaye) > 0
            """)
    BigDecimal sumCreancesTotales();

    @Query("SELECT COUNT(f) FROM Facture f WHERE f.statut IN ('IMPAYEE', 'PARTIELLE')")
    long countImpayees();

    @Query("SELECT COALESCE(SUM(f.montantTotal), 0) FROM Facture f")
    BigDecimal sumMontantTotal();

    @Query("""
            SELECT DISTINCT f FROM Facture f
            JOIN FETCH f.eleve e
            JOIN FETCH e.tuteur
            JOIN FETCH f.anneeScolaire
            LEFT JOIN Inscription i ON i.eleve = e AND i.statut = 'ACTIF'
            LEFT JOIN i.classe c
            LEFT JOIN c.niveau n
            WHERE (:statut IS NULL OR :statut = '' OR f.statut = :statut)
              AND (:search IS NULL OR :search = '' OR
                   LOWER(f.numero) LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(e.nom) LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(e.prenom) LIKE LOWER(CONCAT('%', :search, '%')))
              AND (:eleveId IS NULL OR e.id = :eleveId)
              AND (:niveauId IS NULL OR n.id = :niveauId)
              AND (:classeId IS NULL OR c.id = :classeId)
            ORDER BY f.dateEmission DESC
            """)
    List<Facture> searchAll(
            @Param("statut") String statut,
            @Param("search") String search,
            @Param("niveauId") Long niveauId,
            @Param("classeId") Long classeId,
            @Param("eleveId") Long eleveId);
}
