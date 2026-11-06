package mr.ecoli.evaluation.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EvaluationDto(
        Long id,
        String type,
        LocalDate dateEvaluation,
        BigDecimal noteMax,
        short trimestre,
        String classe,
        String matiere
) {}
