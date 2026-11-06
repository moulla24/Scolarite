package mr.ecoli.note.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BulletinLigneDto(
        String matiere,
        String type,
        LocalDate dateEvaluation,
        BigDecimal noteMax,
        BigDecimal noteObtenue
) {}
