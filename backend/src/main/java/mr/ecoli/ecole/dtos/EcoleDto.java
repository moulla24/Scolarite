package mr.ecoli.ecole.dtos;

public record EcoleDto(
        Long id,
        String nom,
        String code,
        String adresse,
        String telephone,
        String email,
        String logoUrl
) {}
