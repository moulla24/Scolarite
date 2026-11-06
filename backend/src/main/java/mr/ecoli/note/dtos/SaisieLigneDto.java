package mr.ecoli.note.dtos;

import java.util.List;

public record SaisieLigneDto(Long eleveId, String nomComplet, List<SaisieNoteCellDto> notes) {}
