package mr.ecoli.maquette.dtos;

import java.math.BigDecimal;

public record MaquetteMatiereCardDto(
        Long id,
        Long matiereId,
        String matiere,
        BigDecimal volumeHoraire,
        int nombreChapitres
) {}
