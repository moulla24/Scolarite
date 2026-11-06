package mr.ecoli.absence.dtos;

import java.util.List;

public record PlagePresenceDto(
        String plage,
        String libelle,
        List<PresenceEleveLigneDto> eleves
) {}
