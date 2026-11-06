package mr.ecoli.planification.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record TypeControleRequest(
        @NotNull Long niveauId,
        @NotBlank @Size(max = 30) String code,
        @NotBlank @Size(max = 80) String libelle,
        @Positive BigDecimal noteMax,
        Integer ordre,
        boolean actif
) {}
