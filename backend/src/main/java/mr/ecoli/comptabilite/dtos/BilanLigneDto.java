package mr.ecoli.comptabilite.dtos;

import java.math.BigDecimal;

public record BilanLigneDto(String compte, String libelle, BigDecimal montant) {}
