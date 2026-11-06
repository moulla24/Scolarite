package mr.ecoli.evaluation.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record EvaluationRequest(
        @NotNull Long enseignementId,
        @NotBlank @Size(max = 40) String type,
        @NotNull LocalDate dateEvaluation,
        @Positive BigDecimal noteMax,
        Short trimestre
) {}
