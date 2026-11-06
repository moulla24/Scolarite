package mr.ecoli.comptabilite.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record DepenseScolaireRequest(
        @NotBlank @Size(max = 30) String categorie,
        @NotNull LocalDate dateDepense,
        @NotNull @DecimalMin("0.01") BigDecimal montant,
        @NotBlank @Size(max = 250) String description,
        @Size(max = 255) String justificatif
) {}
