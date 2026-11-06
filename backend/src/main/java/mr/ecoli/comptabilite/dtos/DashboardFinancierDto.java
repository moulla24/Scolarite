package mr.ecoli.comptabilite.dtos;

import java.math.BigDecimal;
import java.util.List;

public record DashboardFinancierDto(
        BigDecimal encaisseAujourdhui,
        BigDecimal encaisseSemaine,
        BigDecimal encaisseMois,
        BigDecimal encaisseAnnee,
        BigDecimal creancesTotales,
        long facturesImpayees,
        List<RepartitionFraisDto> repartitionFrais,
        List<RevenuMensuelDto> revenusMensuels
) {}
