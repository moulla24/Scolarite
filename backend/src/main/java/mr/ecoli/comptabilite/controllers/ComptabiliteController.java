package mr.ecoli.comptabilite.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.List;
import mr.ecoli.comptabilite.dtos.BilanDto;
import mr.ecoli.comptabilite.dtos.ComptabiliteStatsDto;
import mr.ecoli.comptabilite.dtos.DemandePaiementDto;
import mr.ecoli.comptabilite.dtos.EcritureComptableDto;
import mr.ecoli.comptabilite.dtos.FactureDetailDto;
import mr.ecoli.comptabilite.dtos.FactureListDto;
import mr.ecoli.comptabilite.dtos.NotificationComptableDto;
import mr.ecoli.comptabilite.dtos.PaiementHistoriqueDto;
import mr.ecoli.comptabilite.dtos.RefusDemandePaiementRequest;
import jakarta.validation.Valid;
import mr.ecoli.comptabilite.dtos.EcritureComptableRequest;
import mr.ecoli.comptabilite.services.ComptabiliteService;
import mr.ecoli.facturation.services.DemandePaiementService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/comptabilite")
@Tag(name = "Comptabilité", description = "Espace comptable — factures, paiements, journal, bilan")
@PreAuthorize("hasAnyRole('ADMIN', 'COMPTABLE')")
public class ComptabiliteController {

    private final ComptabiliteService comptabiliteService;
    private final DemandePaiementService demandePaiementService;

    public ComptabiliteController(
            ComptabiliteService comptabiliteService, DemandePaiementService demandePaiementService) {
        this.comptabiliteService = comptabiliteService;
        this.demandePaiementService = demandePaiementService;
    }

    @GetMapping("/stats")
    @Operation(summary = "Statistiques comptables")
    public ComptabiliteStatsDto stats(@AuthenticationPrincipal Long userId) {
        return comptabiliteService.getStats(userId);
    }

    @GetMapping("/factures")
    @Operation(summary = "Liste des factures")
    public List<FactureListDto> listFactures(
            @RequestParam(required = false) String statut,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long niveauId,
            @RequestParam(required = false) Long classeId,
            @RequestParam(required = false) Long eleveId) {
        return comptabiliteService.listFactures(statut, search, niveauId, classeId, eleveId);
    }

    @GetMapping("/factures/{id}")
    @Operation(summary = "Détail d'une facture")
    public FactureDetailDto getFacture(@PathVariable Long id) {
        return comptabiliteService.getFactureDetail(id);
    }

    @GetMapping("/paiements")
    @Operation(summary = "Historique des paiements")
    public List<PaiementHistoriqueDto> listPaiements(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String mode,
            @RequestParam(required = false) Long niveauId,
            @RequestParam(required = false) Long classeId,
            @RequestParam(required = false) Long eleveId,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "100") int limit) {
        return comptabiliteService.listPaiements(
                from, to, mode, niveauId, classeId, eleveId, search, limit);
    }

    @GetMapping("/journal")
    @Operation(summary = "Journal comptable")
    public List<EcritureComptableDto> journal(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String referenceType,
            @RequestParam(required = false) String compte,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "200") int limit) {
        return comptabiliteService.listJournal(from, to, referenceType, compte, search, limit);
    }

    @PostMapping("/journal")
    @Operation(summary = "Créer une écriture manuelle")
    public ResponseEntity<EcritureComptableDto> createEcriture(@Valid @RequestBody EcritureComptableRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(comptabiliteService.createEcriture(request));
    }

    @PutMapping("/journal/{id}")
    @Operation(summary = "Modifier une écriture manuelle")
    public EcritureComptableDto updateEcriture(
            @PathVariable Long id, @Valid @RequestBody EcritureComptableRequest request) {
        return comptabiliteService.updateEcriture(id, request);
    }

    @DeleteMapping("/journal/{id}")
    @Operation(summary = "Supprimer une écriture manuelle")
    public ResponseEntity<Void> deleteEcriture(@PathVariable Long id) {
        comptabiliteService.deleteEcriture(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/bilan")
    @Operation(summary = "Bilan comptable")
    public BilanDto bilan(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return comptabiliteService.getBilan(from, to);
    }

    @GetMapping("/demandes-paiement")
    @Operation(summary = "Demandes de paiement parent")
    public List<DemandePaiementDto> listDemandesPaiement(
            @RequestParam(required = false) String statut) {
        return demandePaiementService.listPourComptable(statut);
    }

    @GetMapping("/demandes-paiement/en-attente/count")
    @Operation(summary = "Nombre de demandes en attente")
    public long countDemandesEnAttente() {
        return demandePaiementService.countEnAttente();
    }

    @PostMapping("/demandes-paiement/{id}/valider")
    @Operation(summary = "Valider une demande et enregistrer le paiement")
    public DemandePaiementDto validerDemande(
            @PathVariable Long id, @AuthenticationPrincipal Long userId) {
        return demandePaiementService.valider(id, userId);
    }

    @PostMapping("/demandes-paiement/{id}/refuser")
    @Operation(summary = "Refuser une demande de paiement")
    public DemandePaiementDto refuserDemande(
            @PathVariable Long id,
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody RefusDemandePaiementRequest request) {
        return demandePaiementService.refuser(id, userId, request.motifRefus());
    }

    @GetMapping("/notifications")
    @Operation(summary = "Notifications comptable")
    public List<NotificationComptableDto> notifications(@AuthenticationPrincipal Long userId) {
        return comptabiliteService.listNotifications(userId);
    }

    @PutMapping("/notifications/{id}/lu")
    @Operation(summary = "Marquer une notification comme lue")
    public ResponseEntity<Void> markRead(@PathVariable Long id, @AuthenticationPrincipal Long userId) {
        comptabiliteService.markNotificationRead(userId, id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/notifications/lu-tout")
    @Operation(summary = "Marquer toutes les notifications comme lues")
    public ResponseEntity<Void> markAllRead(@AuthenticationPrincipal Long userId) {
        comptabiliteService.markAllNotificationsRead(userId);
        return ResponseEntity.noContent().build();
    }
}
