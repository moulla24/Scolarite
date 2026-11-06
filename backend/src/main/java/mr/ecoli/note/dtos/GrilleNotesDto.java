package mr.ecoli.note.dtos;

import java.util.List;

public record GrilleNotesDto(
        List<GrilleColonneDto> colonnes,
        List<GrilleLigneDto> lignes
) {}
