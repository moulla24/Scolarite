package mr.ecoli.seance.dtos;

public record SeanceEmploiDto(
        Long id,
        Long semaineId,
        String jour,
        short jourIndex,
        String horaire,
        Long classeId,
        String classe,
        Long matiereId,
        String matiere,
        Long enseignantId,
        String enseignant,
        String salle
) {}
