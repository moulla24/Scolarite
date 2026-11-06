package mr.ecoli.note.dtos;

import java.math.BigDecimal;

public record SaisieNoteCellDto(
        Long evaluationId,
        Long noteId,
        BigDecimal noteObtenue,
        BigDecimal noteMax
) {}
