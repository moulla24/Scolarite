package mr.ecoli.comptabilite.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DepenseScolaireDto(
        Long id,
        String categorie,
        String categorieLibelle,
        LocalDate dateDepense,
        BigDecimal montant,
        String description,
        String justificatif,
        String agentNom
) {}
