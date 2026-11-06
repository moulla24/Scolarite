package mr.ecoli.note.dtos;

import java.util.List;

public record SaisieNotesDto(
        int trimestreCourant,
        String trimestreLibelle,
        String matiere,
        List<SaisieColonneDto> colonnes,
        List<SaisieLigneDto> lignes
) {}
