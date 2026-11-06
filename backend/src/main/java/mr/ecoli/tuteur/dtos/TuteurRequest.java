package mr.ecoli.tuteur.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public record TuteurRequest(
        @NotBlank @Size(max = 80) String nom,
        @Size(max = 80) String prenom,
        @Size(max = 120) String email,
        @NotBlank @Size(max = 30) String telephone,
        @Size(max = 255) String adresse,
        List<Long> eleveIds
) {}
