package mr.ecoli.absence.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PresenceLigneSaveRequest(
        @NotBlank String plage,
        @NotNull Long eleveId,
        @NotBlank String statut,
        String motif
) {}
