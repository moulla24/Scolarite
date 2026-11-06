package mr.ecoli.enseignant.dtos;

import java.util.List;

public record EnseignantDto(
        Long id,
        String nomComplet,
        String initials,
        String email,
        String telephone,
        String specialite,
        List<String> matieres,
        List<String> classes,
        String statut
) {}
