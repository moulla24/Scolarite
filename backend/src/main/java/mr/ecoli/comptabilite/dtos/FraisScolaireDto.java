package mr.ecoli.comptabilite.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FraisScolaireDto(
        Long id,
        String typeFrais,
        String typeFraisLibelle,
        String libelle,
        BigDecimal montant,
        Long niveauId,
        String niveauNom,
        Long classeId,
        String classeNom,
        int jourEcheance,
        boolean actif
) {}
