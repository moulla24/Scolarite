package mr.ecoli.comptabilite.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import mr.ecoli.comptabilite.dtos.DashboardFinancierDto;
import mr.ecoli.comptabilite.dtos.EcheanceMensuelleDto;
import mr.ecoli.comptabilite.dtos.HistoriqueRetardDto;
import mr.ecoli.comptabilite.dtos.DepenseScolaireDto;
import mr.ecoli.comptabilite.dtos.DepenseScolaireRequest;
import mr.ecoli.comptabilite.dtos.FraisScolaireDto;
import mr.ecoli.comptabilite.dtos.FraisScolaireRequest;
import mr.ecoli.comptabilite.dtos.RapportFinancierDto;
import mr.ecoli.comptabilite.dtos.RemiseEleveDto;
import mr.ecoli.comptabilite.dtos.RemiseEleveRequest;
import mr.ecoli.comptabilite.dtos.SoldeEleveDto;
import mr.ecoli.comptabilite.services.ComptabiliteFinanceService;
import mr.ecoli.comptabilite.services.RecuPdfService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/comptabilite/finance")
@Tag(name = "Comptabilité — Finance", description = "Frais, remises, dépenses, soldes, rapports, reçus PDF")
@PreAuthorize("hasAnyRole('ADMIN', 'COMPTABLE')")
public class ComptabiliteFinanceController {

    private final ComptabiliteFinanceService financeService;
    private final RecuPdfService recuPdfService;

    public ComptabiliteFinanceController(
            ComptabiliteFinanceService financeService, RecuPdfService recuPdfService) {
        this.financeService = financeService;
        this.recuPdfService = recuPdfService;
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Tableau de bord financier complet")
    public DashboardFinancierDto dashboard() {
        return financeService.getDashboard();
    }

    @GetMapping("/frais")
    public List<FraisScolaireDto> listFrais() {
        return financeService.listFrais();
    }

    @PostMapping("/frais")
    public ResponseEntity<FraisScolaireDto> createFrais(@Valid @RequestBody FraisScolaireRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(financeService.createFrais(request));
    }

    @PutMapping("/frais/{id}")
    public FraisScolaireDto updateFrais(@PathVariable Long id, @Valid @RequestBody FraisScolaireRequest request) {
        return financeService.updateFrais(id, request);
    }

    @GetMapping("/frais/types")
    public Map<String, String> typesFrais() {
        return financeService.typesFrais();
    }

    @GetMapping("/remises")
    public List<RemiseEleveDto> listRemises(@RequestParam(required = false) Long eleveId) {
        return financeService.listRemises(eleveId);
    }

    @PostMapping("/remises")
    public ResponseEntity<RemiseEleveDto> createRemise(@Valid @RequestBody RemiseEleveRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(financeService.createRemise(request));
    }

    @GetMapping("/remises/types")
    public Map<String, String> typesRemise() {
        return financeService.typesRemise();
    }

    @GetMapping("/depenses")
    public List<DepenseScolaireDto> listDepenses(@RequestParam(required = false) String categorie) {
        return financeService.listDepenses(categorie);
    }

    @PostMapping("/depenses")
    public ResponseEntity<DepenseScolaireDto> createDepense(
            @Valid @RequestBody DepenseScolaireRequest request, @AuthenticationPrincipal Long userId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(financeService.createDepense(request, userId));
    }

    @GetMapping("/depenses/categories")
    public Map<String, String> categoriesDepense() {
        return financeService.categoriesDepense();
    }

    @GetMapping("/soldes/{eleveId}")
    public SoldeEleveDto soldeEleve(@PathVariable Long eleveId) {
        return financeService.getSoldeEleve(eleveId);
    }

    @GetMapping("/echeances")
    public List<EcheanceMensuelleDto> listEcheances(@RequestParam Long eleveId) {
        return financeService.listEcheances(eleveId);
    }

    @GetMapping("/retards")
    public List<HistoriqueRetardDto> listRetards(@RequestParam Long eleveId) {
        return financeService.listRetards(eleveId);
    }

    @PostMapping("/penalites/recalculer")
    public ResponseEntity<Void> recalculerPenalites() {
        financeService.recalculerPenalites();
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/rapports")
    public RapportFinancierDto rapport(@RequestParam(defaultValue = "MENSUEL") String type) {
        return financeService.getRapport(type);
    }

    @GetMapping("/paiements/{id}/recu.pdf")
    @Operation(summary = "Télécharger le reçu PDF d'un paiement")
    public ResponseEntity<byte[]> recuPdf(@PathVariable Long id) {
        byte[] pdf = recuPdfService.genererRecuPdf(id);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "recu-" + id + ".pdf");
        return ResponseEntity.ok().headers(headers).body(pdf);
    }
}
