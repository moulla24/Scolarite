package mr.ecoli.note.dtos;

import java.util.List;

public record BulletinDetailDto(
        Long eleveId,
        String nomComplet,
        String classe,
        List<BulletinLigneDto> lignes
) {}
