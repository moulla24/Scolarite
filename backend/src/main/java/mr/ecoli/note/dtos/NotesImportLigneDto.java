package mr.ecoli.note.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public record NotesImportLigneDto(
        @NotBlank String matricule,
        @NotBlank String matiere,
        @NotBlank String typeEvaluation,
        @NotNull LocalDate dateEvaluation,
        @NotNull BigDecimal noteMax,
        @NotNull BigDecimal noteObtenue
) {}
