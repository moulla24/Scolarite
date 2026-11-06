package mr.ecoli.comptabilite.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;

public record HistoriqueRetardDto(
        Long id,
        String libelleEcheance,
        LocalDate dateEcheance,
        int joursRetard,
        BigDecimal montantPenalite,
        LocalDate dateConstat
) {}
