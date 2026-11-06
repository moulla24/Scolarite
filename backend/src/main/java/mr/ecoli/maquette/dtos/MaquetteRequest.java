package mr.ecoli.maquette.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record MaquetteRequest(
        @NotBlank @Size(max = 200) String titre,
        @NotNull Long matiereId,
        @NotNull Long niveauId,
        String description,
        @NotBlank @Size(max = 10) String format,
        @Size(max = 500) String fichierUrl,
        @PositiveOrZero int nbPages,
        LocalDate dateMaj
) {}
