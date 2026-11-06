package mr.ecoli.note.dtos;

import java.util.List;

public record SaisieNoteItemDto(Long eleveId, List<SaisieNoteValueDto> notes) {}
