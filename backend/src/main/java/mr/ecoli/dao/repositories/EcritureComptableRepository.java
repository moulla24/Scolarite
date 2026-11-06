package mr.ecoli.dao.repositories;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import mr.ecoli.dao.entities.EcritureComptable;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EcritureComptableRepository extends JpaRepository<EcritureComptable, Long> {

    @Query("""
            SELECT e FROM EcritureComptable e
            WHERE (:from IS NULL OR e.dateEcriture >= :from)
              AND (:to IS NULL OR e.dateEcriture <= :to)
              AND (:referenceType IS NULL OR :referenceType = '' OR e.referenceType = :referenceType)
              AND (:compte IS NULL OR :compte = '' OR e.compteDebit = :compte OR e.compteCredit = :compte)
              AND (:search IS NULL OR :search = '' OR LOWER(e.libelle) LIKE LOWER(CONCAT('%', :search, '%')))
            ORDER BY e.dateEcriture DESC, e.id DESC
            """)
    List<EcritureComptable> findFiltered(
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            @Param("referenceType") String referenceType,
            @Param("compte") String compte,
            @Param("search") String search,
            Pageable pageable);

    @Query("SELECT COALESCE(SUM(e.montant), 0) FROM EcritureComptable e WHERE e.compteCredit = '706'")
    BigDecimal sumEncaissements();

    @Query("""
            SELECT COALESCE(SUM(e.montant), 0) FROM EcritureComptable e
            WHERE e.compteCredit = '706'
              AND e.dateEcriture >= :from AND e.dateEcriture <= :to
            """)
    BigDecimal sumEncaissementsBetween(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("""
            SELECT COALESCE(SUM(e.montant), 0) FROM EcritureComptable e
            WHERE e.compteDebit = '641'
              AND e.dateEcriture >= :from AND e.dateEcriture <= :to
            """)
    BigDecimal sumChargesPersonnelBetween(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("""
            SELECT COALESCE(SUM(e.montant), 0) FROM EcritureComptable e
            WHERE e.compteDebit = '512' AND e.compteCredit = '706'
              AND e.dateEcriture >= :from AND e.dateEcriture <= :to
            """)
    BigDecimal sumTresorerieEntreesBetween(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("""
            SELECT COALESCE(SUM(e.montant), 0) FROM EcritureComptable e
            WHERE e.compteDebit = '641' AND e.compteCredit = '512'
              AND e.dateEcriture >= :from AND e.dateEcriture <= :to
            """)
    BigDecimal sumTresorerieSortiesBetween(@Param("from") LocalDate from, @Param("to") LocalDate to);
}
