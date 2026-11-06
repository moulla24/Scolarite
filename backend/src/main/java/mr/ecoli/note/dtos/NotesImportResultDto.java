package mr.ecoli.note.dtos;

public record NotesImportResultDto(
        int importees,
        int ignorees,
        int erreurs
) {}
