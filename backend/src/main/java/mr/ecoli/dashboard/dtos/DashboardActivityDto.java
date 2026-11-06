package mr.ecoli.dashboard.dtos;

import java.math.BigDecimal;

public record DashboardActivityDto(
        String reference,
        String parentName,
        int studentsCount,
        String level,
        BigDecimal amount,
        String date,
        String status
) {}
