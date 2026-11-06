package mr.ecoli.enseignant.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record EnseignantRequest(
        @NotBlank @Size(max = 80) String nom,
        @NotBlank @Size(max = 80) String prenom,
        @NotBlank @Email @Size(max = 120) String email,
        @Size(max = 30) String telephone,
        @Size(max = 100) String specialite,
        LocalDate dateEmbauche,
        boolean actif
) {}
