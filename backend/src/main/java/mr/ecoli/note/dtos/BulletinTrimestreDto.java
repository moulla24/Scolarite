package mr.ecoli.note.dtos;

import java.math.BigDecimal;
import java.util.List;

public record BulletinTrimestreDto(
        Long eleveId,
        String nomComplet,
        String classe,
        String ecole,
        String anneeScolaire,
        int trimestre,
        String trimestreLibelle,
        List<BulletinMatiereDto> matieres,
        BigDecimal moyenneGenerale
) {}
