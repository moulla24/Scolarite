package mr.ecoli.eleve.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record EleveRequest(
        @NotBlank @Size(max = 30) String matricule,
        @NotBlank @Size(max = 80) String nom,
        @NotBlank @Size(max = 80) String prenom,
        LocalDate dateNaissance,
        @Size(max = 1) String sexe,
        Long tuteurId,
        Long ecoleId,
        Long classeId
) {}
