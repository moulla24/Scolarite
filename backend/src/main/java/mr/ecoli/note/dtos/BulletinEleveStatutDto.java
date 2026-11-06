package mr.ecoli.note.dtos;

public record BulletinEleveStatutDto(
        Long eleveId,
        String nomComplet,
        String classe,
        boolean trimestre1Disponible,
        boolean trimestre2Disponible,
        boolean trimestre3Disponible
) {}
