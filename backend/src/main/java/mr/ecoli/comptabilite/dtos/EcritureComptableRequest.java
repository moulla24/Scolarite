package mr.ecoli.comptabilite.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record EcritureComptableRequest(
        @NotNull LocalDate dateEcriture,
        @NotBlank @Size(max = 200) String libelle,
        @NotBlank @Size(max = 20) String compteDebit,
        @NotBlank @Size(max = 20) String compteCredit,
        @NotNull @DecimalMin("0.01") BigDecimal montant,
        @Size(max = 40) String numeroPiece
) {}
