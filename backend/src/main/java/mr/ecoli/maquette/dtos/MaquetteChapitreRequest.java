package mr.ecoli.maquette.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MaquetteChapitreRequest(
        Short ordre,
        @NotBlank @Size(max = 200) String titre,
        String description
) {}
