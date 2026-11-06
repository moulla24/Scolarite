package mr.ecoli.maquette.dtos;

public record MaquetteChapitreDto(
        Long id,
        short ordre,
        String titre,
        String description
) {}
