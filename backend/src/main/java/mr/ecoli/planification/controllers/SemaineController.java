package mr.ecoli.planification.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import mr.ecoli.planification.dtos.SemaineDto;
import mr.ecoli.planification.services.SemaineService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/planification/trimestres/{trimestreId}/semaines")
@Tag(name = "Semaines", description = "Semaines calendaires par trimestre")
public class SemaineController {

    private final SemaineService semaineService;

    public SemaineController(SemaineService semaineService) {
        this.semaineService = semaineService;
    }

    @GetMapping
    @Operation(summary = "Liste des semaines d'un trimestre")
    public List<SemaineDto> list(@PathVariable Long trimestreId) {
        return semaineService.listByTrimestre(trimestreId);
    }
}
