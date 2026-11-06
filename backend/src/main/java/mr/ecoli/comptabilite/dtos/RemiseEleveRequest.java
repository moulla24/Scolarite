package mr.ecoli.comptabilite.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record RemiseEleveRequest(
        @NotNull Long eleveId,
        @NotBlank @Size(max = 30) String typeRemise,
        @Size(max = 120) String libelle,
        BigDecimal pourcentage,
        BigDecimal montantFixe,
        boolean actif,
        LocalDate dateDebut,
        LocalDate dateFin
) {}
