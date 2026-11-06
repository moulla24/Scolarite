package mr.ecoli.comptabilite.dtos;

import java.math.BigDecimal;
import java.util.List;

public record LigneFactureDto(Long id, String libelle, BigDecimal montant) {}
