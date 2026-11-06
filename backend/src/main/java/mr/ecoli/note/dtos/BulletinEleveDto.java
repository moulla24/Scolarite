package mr.ecoli.note.dtos;

public record BulletinEleveDto(
        Long eleveId,
        String nomComplet,
        String classe,
        int nombreMatieres
) {}
