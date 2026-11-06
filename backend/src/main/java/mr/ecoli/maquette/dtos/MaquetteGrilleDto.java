package mr.ecoli.maquette.dtos;

import java.util.List;

public record MaquetteGrilleDto(
        Long maquetteClasseId,
        Long classeId,
        String classe,
        String niveau,
        List<MaquetteMatiereCardDto> matieres
) {}
