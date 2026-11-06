package mr.ecoli.dao.repositories;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import mr.ecoli.dao.entities.PaiementPersonnel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaiementPersonnelRepository extends JpaRepository<PaiementPersonnel, Long> {

    Optional<PaiementPersonnel> findByPersonnelIdAndMois(Long personnelId, LocalDate mois);

    @Query("""
            SELECT pp FROM PaiementPersonnel pp
            JOIN FETCH pp.personnel p
            WHERE pp.mois = :mois
            ORDER BY p.categorie, p.nom
            """)
    List<PaiementPersonnel> findByMois(@Param("mois") LocalDate mois);

    @Query("""
            SELECT COALESCE(SUM(pp.montant), 0) FROM PaiementPersonnel pp
            WHERE pp.datePaiement >= :from AND pp.datePaiement <= :to
            """)
    BigDecimal sumBetween(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("""
            SELECT COALESCE(SUM(pp.montant), 0) FROM PaiementPersonnel pp
            WHERE pp.mois >= :from AND pp.mois <= :to
            """)
    BigDecimal sumByMoisBetween(@Param("from") LocalDate from, @Param("to") LocalDate to);
}
