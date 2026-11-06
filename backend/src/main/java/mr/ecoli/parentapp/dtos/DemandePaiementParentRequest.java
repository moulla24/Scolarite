package mr.ecoli.parentapp.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record DemandePaiementParentRequest(
        @NotNull Long factureId,
        @NotNull @DecimalMin("0.01") BigDecimal montant,
        LocalDate datePaiement,
        @Size(max = 30) String modePaiement,
        @Size(max = 120) String reference,
        @Size(max = 255) String preuveReference
) {}
