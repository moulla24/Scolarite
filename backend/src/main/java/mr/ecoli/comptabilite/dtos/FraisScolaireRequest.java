package mr.ecoli.comptabilite.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record FraisScolaireRequest(
        @NotBlank @Size(max = 30) String typeFrais,
        @NotBlank @Size(max = 120) String libelle,
        @NotNull @DecimalMin("0") BigDecimal montant,
        Long niveauId,
        Long classeId,
        int jourEcheance,
        boolean actif
) {}
