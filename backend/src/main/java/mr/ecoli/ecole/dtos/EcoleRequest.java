package mr.ecoli.ecole.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EcoleRequest(
        @NotBlank @Size(max = 150) String nom,
        @NotBlank @Size(max = 30) String code,
        @Size(max = 255) String adresse,
        @Size(max = 30) String telephone,
        @Email @Size(max = 120) String email,
        @Size(max = 500) String logoUrl
) {}
