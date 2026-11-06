package mr.ecoli.planification.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import mr.ecoli.planification.dtos.QuotaControleDto;
import mr.ecoli.planification.dtos.QuotasControlesRequest;
import mr.ecoli.planification.services.QuotasControlesService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/planification/quotas-controles")
@Tag(name = "Planification — Quotas contrôles", description = "Nombre de contrôles prévus par trimestre et type")
public class QuotasControlesController {

    private final QuotasControlesService quotasControlesService;

    public QuotasControlesController(QuotasControlesService quotasControlesService) {
        this.quotasControlesService = quotasControlesService;
    }

    @GetMapping
    @Operation(summary = "Quotas par trimestre et niveau")
    public List<QuotaControleDto> listQuotas(
            @RequestParam Long trimestreId, @RequestParam Long niveauId) {
        return quotasControlesService.listQuotas(trimestreId, niveauId);
    }

    @PutMapping
    @Operation(summary = "Enregistrer les quotas d'un trimestre pour un niveau")
    public List<QuotaControleDto> saveQuotas(@Valid @RequestBody QuotasControlesRequest request) {
        return quotasControlesService.saveQuotas(request);
    }
}
