package mr.ecoli.planification.dtos;

import java.time.LocalDate;

public record SemaineDto(
        Long id,
        Long trimestreId,
        short numero,
        LocalDate dateDebut,
        LocalDate dateFin,
        String label
) {}
