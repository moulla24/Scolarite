package mr.ecoli.comptable.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record ComptableRequest(
        @NotBlank @Size(max = 80) String nom,
        @NotBlank @Size(max = 80) String prenom,
        @NotBlank @Email @Size(max = 120) String email,
        @Size(max = 30) String telephone,
        @Size(max = 30) String matricule,
        LocalDate dateEmbauche,
        boolean actif
) {}
