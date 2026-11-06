package mr.ecoli.comptabilite.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record RapportFinancierDto(
        String type,
        LocalDate periodeDebut,
        LocalDate periodeFin,
        BigDecimal totalEncaissements,
        BigDecimal totalDepenses,
        BigDecimal resultatNet,
        List<PaiementHistoriqueDto> paiements,
        List<DepenseScolaireDto> depenses
) {}
