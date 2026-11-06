package mr.ecoli.comptabilite.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PaiementPersonnelDto(
        Long id,
        Long personnelId,
        String nomComplet,
        String categorieLibelle,
        LocalDate mois,
        BigDecimal montant,
        String modePaiement,
        LocalDate datePaiement,
        String reference
) {}
