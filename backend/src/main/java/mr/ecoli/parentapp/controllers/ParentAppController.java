package mr.ecoli.parentapp.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import mr.ecoli.facturation.dtos.FactureDto;
import mr.ecoli.note.dtos.BulletinDetailDto;
import mr.ecoli.parentapp.dtos.DemandePaiementParentDto;
import mr.ecoli.parentapp.dtos.DemandePaiementParentRequest;
import mr.ecoli.parentapp.dtos.DeviceTokenRequest;
import mr.ecoli.parentapp.dtos.ParentAbsenceDto;
import mr.ecoli.parentapp.dtos.ParentDashboardDto;
import mr.ecoli.parentapp.dtos.ParentEnfantResumeDto;
import mr.ecoli.parentapp.dtos.ParentNotificationDto;
import mr.ecoli.parentapp.dtos.ParentScolariteDto;
import mr.ecoli.parentapp.ParentSecurityService;
import mr.ecoli.parentapp.services.ParentAppService;
import mr.ecoli.facturation.services.DemandePaiementService;
import mr.ecoli.seance.dtos.SeanceEmploiDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/parent-app")
@PreAuthorize("hasRole('PARENT')")
@Tag(name = "Application mobile parents", description = "API dédiée aux parents (Flutter)")
public class ParentAppController {

    private final ParentAppService parentAppService;
    private final DemandePaiementService demandePaiementService;
    private final ParentSecurityService parentSecurity;

    public ParentAppController(
            ParentAppService parentAppService,
            DemandePaiementService demandePaiementService,
            ParentSecurityService parentSecurity) {
        this.parentAppService = parentAppService;
        this.demandePaiementService = demandePaiementService;
        this.parentSecurity = parentSecurity;
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Tableau de bord parent")
    public ParentDashboardDto dashboard() {
        return parentAppService.getDashboard();
    }

    @GetMapping("/enfants")
    @Operation(summary = "Liste des enfants du parent connecté")
    public List<ParentEnfantResumeDto> enfants() {
        return parentAppService.listEnfants();
    }

    @GetMapping("/enfants/{eleveId}/scolarite")
    @Operation(summary = "Informations scolaires d'un enfant")
    public ParentScolariteDto scolarite(@PathVariable Long eleveId) {
        return parentAppService.getScolarite(eleveId);
    }

    @GetMapping("/enfants/{eleveId}/bulletin")
    @Operation(summary = "Bulletin / performances académiques")
    public BulletinDetailDto bulletin(@PathVariable Long eleveId) {
        return parentAppService.getBulletin(eleveId);
    }

    @GetMapping("/enfants/{eleveId}/absences")
    @Operation(summary = "Historique présences et absences")
    public List<ParentAbsenceDto> absences(@PathVariable Long eleveId) {
        return parentAppService.getAbsences(eleveId);
    }

    @GetMapping("/enfants/{eleveId}/emploi-du-temps")
    @Operation(summary = "Emploi du temps de la classe de l'enfant")
    public List<SeanceEmploiDto> emploiDuTemps(@PathVariable Long eleveId) {
        return parentAppService.getEmploiDuTemps(eleveId);
    }

    @GetMapping("/factures")
    @Operation(summary = "Factures de tous les enfants")
    public List<FactureDto> factures() {
        return parentAppService.listFactures();
    }

    @GetMapping("/factures/{factureId}")
    @Operation(summary = "Détail d'une facture")
    public FactureDto facture(@PathVariable Long factureId) {
        return parentAppService.getFacture(factureId);
    }

    @GetMapping("/paiements")
    @Operation(summary = "Mes déclarations de paiement")
    public List<DemandePaiementParentDto> mesPaiements() {
        return demandePaiementService.listPourParent(parentSecurity.requireTuteurId());
    }

    @PostMapping("/paiements")
    @Operation(summary = "Déclarer un paiement (simulation bancaire puis validation comptable)")
    public ResponseEntity<DemandePaiementParentDto> declarerPaiement(
            @Valid @RequestBody DemandePaiementParentRequest request) {
        DemandePaiementParentDto created =
                demandePaiementService.soumettre(parentSecurity.requireTuteurId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/paiements/{id}")
    @Operation(summary = "Suivi d'une déclaration de paiement")
    public DemandePaiementParentDto getPaiement(@PathVariable Long id) {
        return demandePaiementService.getPourParent(parentSecurity.requireTuteurId(), id);
    }

    @GetMapping("/paiements/config")
    @Operation(summary = "Configuration simulation paiement bancaire")
    public java.util.Map<String, Integer> paiementConfig() {
        return java.util.Map.of("delaiSecondes", demandePaiementService.getDelaiSimulationSecondes());
    }

    @GetMapping("/notifications")
    @Operation(summary = "Liste des notifications")
    public List<ParentNotificationDto> notifications() {
        return parentAppService.listNotifications();
    }

    @GetMapping("/notifications/unread-count")
    @Operation(summary = "Nombre de notifications non lues")
    public Map<String, Long> unreadCount() {
        return Map.of("count", parentAppService.countUnreadNotifications());
    }

    @PutMapping("/notifications/{id}/lu")
    @Operation(summary = "Marquer une notification comme lue")
    public void markRead(@PathVariable Long id) {
        parentAppService.markNotificationRead(id);
    }

    @PutMapping("/notifications/lu-tout")
    @Operation(summary = "Marquer toutes les notifications comme lues")
    public void markAllRead() {
        parentAppService.markAllNotificationsRead();
    }

    @PostMapping("/device-token")
    @Operation(summary = "Enregistrer le token FCM pour les notifications push")
    public void registerDeviceToken(@Valid @RequestBody DeviceTokenRequest request) {
        parentAppService.registerDeviceToken(request);
    }
}
