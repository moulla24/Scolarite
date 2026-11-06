package mr.ecoli.note.dtos;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record SaisieNoteValueDto(@NotNull Long evaluationId, BigDecimal noteObtenue) {}
