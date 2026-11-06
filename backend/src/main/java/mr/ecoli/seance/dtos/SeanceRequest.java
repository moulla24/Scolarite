package mr.ecoli.seance.dtos;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalTime;

public record SeanceRequest(
        @NotNull Long semaineId,
        @NotNull Long classeId,
        @NotNull Long matiereId,
        @NotNull Long enseignantId,
        @Min(1) @Max(5) short jourSemaine,
        @NotNull LocalTime heureDebut,
        @NotNull LocalTime heureFin,
        @Size(max = 50) String salle,
        @NotBlank @Size(max = 30) String type
) {}
