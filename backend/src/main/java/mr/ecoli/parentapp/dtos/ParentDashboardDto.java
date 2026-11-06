package mr.ecoli.parentapp.dtos;

import java.math.BigDecimal;

public record ParentDashboardDto(
        int nombreEnfants,
        long notificationsNonLues,
        BigDecimal soldeGlobal,
        String statutCompte
) {}
