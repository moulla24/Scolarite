package mr.ecoli.comptabilite.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RemiseEleveDto(
        Long id,
        Long eleveId,
        String eleveNom,
        String typeRemise,
        String typeRemiseLibelle,
        String libelle,
        BigDecimal pourcentage,
        BigDecimal montantFixe,
        boolean actif,
        LocalDate dateDebut,
        LocalDate dateFin
) {}
