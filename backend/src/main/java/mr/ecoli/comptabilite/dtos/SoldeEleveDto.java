package mr.ecoli.comptabilite.dtos;

import java.math.BigDecimal;
import java.util.List;

public record SoldeEleveDto(
        Long eleveId,
        String eleveNom,
        String classe,
        String niveau,
        BigDecimal montantBrut,
        BigDecimal montantRemise,
        BigDecimal montantPenalite,
        BigDecimal montantTotal,
        BigDecimal montantPaye,
        BigDecimal resteAPayer,
        String statut,
        long facturesImpayees,
        long joursRetardMax,
        List<EcheanceMensuelleDto> echeances,
        List<HistoriqueRetardDto> historiqueRetards
) {}
