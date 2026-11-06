package mr.ecoli.enseignement.dtos;

public record EnseignementDto(
        Long id,
        Long classeId,
        String classe,
        Long matiereId,
        String matiere,
        Long enseignantId,
        String enseignant,
        String libelle
) {}
