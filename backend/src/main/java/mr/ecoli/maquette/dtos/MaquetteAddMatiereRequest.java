package mr.ecoli.maquette.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

public record MaquetteAddMatiereRequest(
        @NotBlank String nom,
        @NotNull BigDecimal volumeHoraire,
        List<MaquetteChapitreRequest> chapitres
) {}
