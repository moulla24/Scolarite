package mr.ecoli.absence.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

public record FeuillePresenceSaveRequest(
        @NotNull LocalDate date,
        @NotNull Long classeId,
        @Valid List<PresenceLigneSaveRequest> lignes
) {}
