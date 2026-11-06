package mr.ecoli.comptabilite.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PaiePersonnelLigneDto(
        Long personnelId,
        String nomComplet,
        String matricule,
        String categorie,
        String categorieLibelle,
        BigDecimal salaireMensuel,
        boolean paye,
        Long paiementId,
        BigDecimal montantPaye,
        LocalDate datePaiement,
        String modePaiement
) {}
