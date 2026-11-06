package mr.ecoli.note.dtos;

import java.math.BigDecimal;

public record NoteDto(
        Long id,
        Long evaluationId,
        Long eleveId,
        String eleveNom,
        BigDecimal noteObtenue
) {}
