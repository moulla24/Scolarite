package mr.ecoli.dashboard.dtos;

public record DashboardStatsDto(
        long elevesActifs,
        long elevesInscrits,
        long enseignantsActifs,
        long classesTotal,
        long seancesSemaine
) {}
