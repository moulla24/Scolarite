package mr.ecoli.planification.dtos;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record TrimestreDto(
        Long id,
        Long anneeScolaireId,
        String anneeScolaireNom,
        Short numero,
        String nom,
        LocalDate dateDebut,
        LocalDate dateFin,
        boolean actif
) {}
