package mr.ecoli.comptabilite.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EcheanceMensuelleDto(
        Long id,
        String libelle,
        LocalDate mois,
        BigDecimal montant,
        LocalDate dateEcheance,
        String statut,
        long joursRetard
) {}
