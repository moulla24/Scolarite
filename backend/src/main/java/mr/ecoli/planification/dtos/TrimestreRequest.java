package mr.ecoli.planification.dtos;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record TrimestreRequest(
        @NotNull Long anneeScolaireId,
        @NotNull @Min(1) @Max(3) Short numero,
        @NotBlank @Size(max = 80) String nom,
        @NotNull LocalDate dateDebut,
        @NotNull LocalDate dateFin,
        boolean actif
) {}
