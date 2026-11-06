package mr.ecoli.comptabilite.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record FactureDetailDto(
        Long id,
        String numero,
        String codePaiement,
        Long eleveId,
        String eleveNom,
        String parentNom,
        String parentTelephone,
        String parentEmail,
        String niveau,
        String anneeScolaire,
        LocalDate dateEmission,
        LocalDate dateEcheance,
        BigDecimal montantTotal,
        BigDecimal montantPaye,
        BigDecimal resteAPayer,
        String statut,
        List<LigneFactureDto> lignes,
        List<PaiementHistoriqueDto> paiements
) {}
