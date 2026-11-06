package mr.ecoli.comptabilite.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record PersonnelRequest(
        @NotBlank @Size(max = 80) String nom,
        @NotBlank @Size(max = 80) String prenom,
        @Size(max = 120) String email,
        @Size(max = 30) String telephone,
        @Size(max = 30) String matricule,
        @NotBlank @Size(max = 30) String categorie,
        Long enseignantId,
        @NotNull @DecimalMin("0.01") BigDecimal salaireMensuel,
        LocalDate dateEmbauche,
        @NotBlank @Size(max = 20) String statut
) {}
