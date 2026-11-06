package mr.ecoli.tuteur.dtos;

public record EleveAffecteDto(
        Long id,
        String nomComplet,
        String niveau,
        String classe
) {}
