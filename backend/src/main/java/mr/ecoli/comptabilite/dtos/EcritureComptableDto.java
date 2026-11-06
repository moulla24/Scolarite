package mr.ecoli.comptabilite.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EcritureComptableDto(
        Long id,
        LocalDate dateEcriture,
        String numeroPiece,
        String libelle,
        String compteDebit,
        String compteCredit,
        String compteDebitLibelle,
        String compteCreditLibelle,
        BigDecimal montant,
        String referenceType,
        Long referenceId,
        boolean modifiable
) {}
