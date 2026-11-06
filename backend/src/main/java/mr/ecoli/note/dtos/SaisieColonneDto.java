package mr.ecoli.note.dtos;

import java.math.BigDecimal;

public record SaisieColonneDto(Long evaluationId, String libelle, BigDecimal noteMax) {}
