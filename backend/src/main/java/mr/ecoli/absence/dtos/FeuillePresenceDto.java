package mr.ecoli.absence.dtos;

import java.time.LocalDate;
import java.util.List;

public record FeuillePresenceDto(
        LocalDate date,
        Long classeId,
        String classeNom,
        int jourSemaine,
        String jourLibelle,
        boolean jourScolaire,
        List<PlagePresenceDto> plages
) {}
