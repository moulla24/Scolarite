package mr.ecoli.comptabilite.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record PaiementPersonnelRequest(
        @NotNull Long personnelId,
        @NotNull LocalDate mois,
        @NotNull @DecimalMin("0.01") BigDecimal montant,
        @NotBlank @Size(max = 30) String modePaiement,
        LocalDate datePaiement,
        @Size(max = 80) String reference
) {}
