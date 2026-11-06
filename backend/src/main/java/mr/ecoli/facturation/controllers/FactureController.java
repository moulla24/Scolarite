package mr.ecoli.facturation.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import mr.ecoli.facturation.dtos.FactureDto;
import mr.ecoli.facturation.dtos.PaiementDto;
import mr.ecoli.facturation.dtos.PaiementRequest;
import mr.ecoli.facturation.services.FactureService;
import mr.ecoli.facturation.services.PaiementService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/factures")
@Tag(name = "Facturation", description = "Factures et paiements")
@PreAuthorize("hasAnyRole('ADMIN', 'COMPTABLE')")
public class FactureController {

    private final FactureService factureService;
    private final PaiementService paiementService;

    public FactureController(FactureService factureService, PaiementService paiementService) {
        this.factureService = factureService;
        this.paiementService = paiementService;
    }

    @GetMapping
    @Operation(summary = "Liste des factures d'un élève")
    public List<FactureDto> list(@RequestParam Long eleveId) {
        return factureService.listByEleve(eleveId);
    }

    @GetMapping("/paiements/recents")
    @Operation(summary = "Paiements récents")
    public List<PaiementDto> recentPayments(@RequestParam(defaultValue = "10") int limit) {
        return paiementService.listRecent(limit);
    }

    @PostMapping("/paiements")
    @Operation(summary = "Enregistrer un paiement")
    public ResponseEntity<PaiementDto> createPayment(
            @Valid @RequestBody PaiementRequest request,
            @AuthenticationPrincipal Long userId) {
        PaiementDto created = paiementService.enregistrer(request, userId);
        return ResponseEntity.created(URI.create("/api/factures/paiements/" + created.id())).body(created);
    }
}
