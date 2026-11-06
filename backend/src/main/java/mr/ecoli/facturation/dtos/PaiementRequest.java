package mr.ecoli.facturation.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public record PaiementRequest(
        @NotNull Long factureId,
        @NotNull @DecimalMin("0.01") BigDecimal montant,
        LocalDate datePaiement,
        String modePaiement,
        String reference
) {}
