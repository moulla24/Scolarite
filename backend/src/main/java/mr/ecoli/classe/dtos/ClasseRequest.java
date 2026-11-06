package mr.ecoli.classe.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ClasseRequest(
        @NotNull Long niveauId,
        @NotBlank @Size(max = 50) String nom,
        @Size(max = 50) String salle,
        @Positive int capacite
) {}
