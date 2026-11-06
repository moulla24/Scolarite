package mr.ecoli.comptabilite.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ComptabiliteStatsDto(
        BigDecimal encaissementsMois,
        BigDecimal creancesTotales,
        BigDecimal facturesEmises,
        long facturesImpayees,
        long paiementsMois,
        long notificationsNonLues
) {}
