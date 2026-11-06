package mr.ecoli.planification.dtos;

import java.math.BigDecimal;

public record TypeControleDto(
        Long id,
        Long niveauId,
        String niveauNom,
        Long ecoleId,
        String ecoleNom,
        String code,
        String libelle,
        BigDecimal noteMax,
        Integer ordre,
        boolean actif
) {}
