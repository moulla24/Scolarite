package mr.ecoli.note.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;

public record GrilleColonneDto(
        Long evaluationId,
        String matiere,
        Long matiereId,
        String type,
        LocalDate dateEvaluation,
        BigDecimal noteMax,
        String libelle
) {}
