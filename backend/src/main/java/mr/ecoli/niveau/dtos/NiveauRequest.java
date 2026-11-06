package mr.ecoli.niveau.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record NiveauRequest(
        @NotNull Long ecoleId,
        @NotBlank @Size(max = 30) String nom
) {}
