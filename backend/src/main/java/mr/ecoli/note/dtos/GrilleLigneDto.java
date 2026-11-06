package mr.ecoli.note.dtos;

import java.math.BigDecimal;
import java.util.List;

public record GrilleLigneDto(
        Long eleveId,
        String nomComplet,
        String classe,
        List<BigDecimal> notes
) {}
