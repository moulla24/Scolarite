package mr.ecoli.note.dtos;

import java.math.BigDecimal;

public record BulletinMatiereDto(
        String matiere,
        BigDecimal coefficient,
        BigDecimal noteDevoir,
        BigDecimal noteComposition,
        BigDecimal moyenne
) {}
