package mr.ecoli.comptable.dtos;

import java.time.LocalDate;

public record ComptableDto(
        Long id,
        String nomComplet,
        String initials,
        String email,
        String telephone,
        String matricule,
        LocalDate dateEmbauche,
        String statut
) {}
