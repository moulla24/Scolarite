package mr.ecoli.maquette.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.List;

public record MaquetteMatiereSaveRequest(
        @NotNull @Positive BigDecimal volumeHoraire,
        @NotEmpty List<@Valid MaquetteChapitreRequest> chapitres
) {}
