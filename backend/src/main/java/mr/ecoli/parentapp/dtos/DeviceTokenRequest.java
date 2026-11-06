package mr.ecoli.parentapp.dtos;

import jakarta.validation.constraints.NotBlank;

public record DeviceTokenRequest(
        @NotBlank String token,
        String plateforme
) {}
