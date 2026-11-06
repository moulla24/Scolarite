package mr.ecoli.dashboard.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import mr.ecoli.dashboard.dtos.DashboardActivityDto;
import mr.ecoli.dashboard.dtos.DashboardStatsDto;
import mr.ecoli.dashboard.services.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
@Tag(name = "Tableau de bord", description = "Indicateurs agrégés de l'établissement")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/stats")
    @Operation(summary = "Statistiques du tableau de bord")
    public DashboardStatsDto getStats(@RequestParam(required = false) Long niveauId) {
        return dashboardService.getStats(niveauId);
    }

    @GetMapping("/activity")
    @Operation(summary = "Activité récente (inscriptions)")
    public java.util.List<DashboardActivityDto> getActivity(@RequestParam(required = false) Long niveauId) {
        return dashboardService.getRecentActivity(niveauId);
    }
}
