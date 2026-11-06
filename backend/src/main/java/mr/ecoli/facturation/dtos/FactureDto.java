package mr.ecoli.facturation.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FactureDto(
        Long id,
        String numero,
        String codePaiement,
        Long eleveId,
        String eleveNom,
        LocalDate dateEmission,
        LocalDate dateEcheance,
        BigDecimal montantTotal,
        BigDecimal montantPaye,
        BigDecimal resteAPayer,
        String statut
) {}
