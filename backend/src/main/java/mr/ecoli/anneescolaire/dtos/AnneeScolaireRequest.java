package mr.ecoli.anneescolaire.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record AnneeScolaireRequest(
        @NotNull Long ecoleId,
        @NotBlank @Size(max = 50) String nom,
        @NotNull LocalDate dateDebut,
        @NotNull LocalDate dateFin,
        boolean active
) {}
