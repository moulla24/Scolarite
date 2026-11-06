package mr.ecoli.note.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

public record NoteRequest(
        @NotNull Long evaluationId,
        @NotNull Long eleveId,
        @PositiveOrZero BigDecimal noteObtenue
) {}
