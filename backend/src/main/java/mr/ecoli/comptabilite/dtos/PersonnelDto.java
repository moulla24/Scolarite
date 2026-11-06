package mr.ecoli.comptabilite.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PersonnelDto(
        Long id,
        String nom,
        String prenom,
        String nomComplet,
        String email,
        String telephone,
        String matricule,
        String categorie,
        String categorieLibelle,
        Long enseignantId,
        BigDecimal salaireMensuel,
        LocalDate dateEmbauche,
        String statut
) {}
