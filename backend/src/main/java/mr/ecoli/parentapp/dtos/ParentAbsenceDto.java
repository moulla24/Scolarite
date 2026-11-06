package mr.ecoli.parentapp.dtos;

import java.time.LocalDate;

public record ParentAbsenceDto(
        Long id,
        LocalDate date,
        String matiere,
        String statut,
        String motif,
        String horaire
) {}
