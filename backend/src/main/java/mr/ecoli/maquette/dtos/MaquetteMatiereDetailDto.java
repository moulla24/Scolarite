package mr.ecoli.maquette.dtos;

import java.math.BigDecimal;
import java.util.List;

public record MaquetteMatiereDetailDto(
        Long id,
        Long matiereId,
        String matiere,
        BigDecimal volumeHoraire,
        String classe,
        String niveau,
        List<MaquetteChapitreDto> chapitres
) {}
