package mr.ecoli.comptabilite.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FactureListDto(
        Long id,
        String numero,
        String codePaiement,
        Long eleveId,
        String eleveNom,
        String parentNom,
        String parentTelephone,
        String niveau,
        LocalDate dateEmission,
        LocalDate dateEcheance,
        BigDecimal montantTotal,
        BigDecimal montantPaye,
        BigDecimal resteAPayer,
        String statut
) {}
