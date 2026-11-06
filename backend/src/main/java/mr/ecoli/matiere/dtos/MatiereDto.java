package mr.ecoli.matiere.dtos;

import java.math.BigDecimal;

public record MatiereDto(Long id, String nom, String code, BigDecimal coefficient) {}
