package mr.ecoli.comptabilite.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RefusDemandePaiementRequest(
        @NotBlank @Size(max = 500) String motifRefus
) {}
