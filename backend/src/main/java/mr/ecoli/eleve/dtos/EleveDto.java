package mr.ecoli.eleve.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EleveDto(
        Long id,
        String nom,
        String prenom,
        String nomComplet,
        String initials,
        String matricule,
        String classe,
        String niveau,
        LocalDate dateNaissance,
        String sexe,
        BigDecimal solde,
        String statut
) {}
