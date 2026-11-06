package mr.ecoli.comptabilite.dtos;

import java.math.BigDecimal;

public record RevenuMensuelDto(int mois, int annee, BigDecimal montant) {}
