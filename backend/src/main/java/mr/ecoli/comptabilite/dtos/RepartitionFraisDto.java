package mr.ecoli.comptabilite.dtos;

import java.math.BigDecimal;

public record RepartitionFraisDto(String typeFrais, String libelle, BigDecimal montant) {}
