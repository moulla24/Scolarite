package mr.ecoli.note.dtos;

public record NotesSummaryLigneDto(
        Long eleveId,
        String nomComplet,
        String classe,
        int nombreMatieres
) {}
