package mr.ecoli.note.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record SaisieSaveRequest(
        @NotNull Long classeId,
        @NotNull Long matiereId,
        @NotEmpty List<@Valid SaisieNoteItemDto> notes
) {}
