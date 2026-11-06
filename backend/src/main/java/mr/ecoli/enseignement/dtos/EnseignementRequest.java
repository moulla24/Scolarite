package mr.ecoli.enseignement.dtos;

import jakarta.validation.constraints.NotNull;

public record EnseignementRequest(
        @NotNull Long classeId,
        @NotNull Long matiereId,
        @NotNull Long enseignantId
) {}
