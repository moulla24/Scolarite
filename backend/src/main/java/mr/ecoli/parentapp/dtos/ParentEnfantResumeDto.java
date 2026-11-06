package mr.ecoli.parentapp.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ParentEnfantResumeDto(
        Long id,
        String matricule,
        String nomComplet,
        String sexe,
        LocalDate dateNaissance,
        String ecoleNom,
        String classe,
        String niveau,
        String anneeScolaire,
        String statutFacturation,
        BigDecimal soldeRestant
) {}
