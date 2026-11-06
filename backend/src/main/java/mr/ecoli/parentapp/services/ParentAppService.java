package mr.ecoli.parentapp.services;

import java.math.BigDecimal;
import java.util.List;
import mr.ecoli.common.BillingBalanceService;
import mr.ecoli.common.exception.ResourceNotFoundException;
import mr.ecoli.dao.entities.Eleve;
import mr.ecoli.dao.entities.Facture;
import mr.ecoli.dao.entities.Inscription;
import mr.ecoli.dao.entities.NotificationParent;
import mr.ecoli.dao.entities.ParentDeviceToken;
import mr.ecoli.dao.entities.PresenceEleve;
import mr.ecoli.dao.entities.Tuteur;
import mr.ecoli.dao.repositories.EleveRepository;
import mr.ecoli.dao.repositories.FactureRepository;
import mr.ecoli.dao.repositories.NotificationParentRepository;
import mr.ecoli.dao.repositories.ParentDeviceTokenRepository;
import mr.ecoli.dao.repositories.PresenceEleveRepository;
import mr.ecoli.dao.repositories.InscriptionRepository;
import mr.ecoli.facturation.dtos.FactureDto;
import mr.ecoli.note.dtos.BulletinDetailDto;
import mr.ecoli.note.services.NoteService;
import mr.ecoli.parentapp.ParentSecurityService;
import mr.ecoli.parentapp.dtos.DeviceTokenRequest;
import mr.ecoli.parentapp.dtos.ParentAbsenceDto;
import mr.ecoli.parentapp.dtos.ParentDashboardDto;
import mr.ecoli.parentapp.dtos.ParentEnfantResumeDto;
import mr.ecoli.parentapp.dtos.ParentNotificationDto;
import mr.ecoli.parentapp.dtos.ParentScolariteDto;
import mr.ecoli.seance.dtos.SeanceEmploiDto;
import mr.ecoli.seance.services.EmploiDuTempsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ParentAppService {

    private final ParentSecurityService security;
    private final EleveRepository eleveRepository;
    private final InscriptionRepository inscriptionRepository;
    private final NotificationParentRepository notificationRepository;
    private final ParentDeviceTokenRepository deviceTokenRepository;
    private final PresenceEleveRepository presenceEleveRepository;
    private final FactureRepository factureRepository;
    private final BillingBalanceService billing;
    private final NoteService noteService;
    private final EmploiDuTempsService emploiDuTempsService;

    public ParentAppService(
            ParentSecurityService security,
            EleveRepository eleveRepository,
            InscriptionRepository inscriptionRepository,
            NotificationParentRepository notificationRepository,
            ParentDeviceTokenRepository deviceTokenRepository,
            PresenceEleveRepository presenceEleveRepository,
            FactureRepository factureRepository,
            BillingBalanceService billing,
            NoteService noteService,
            EmploiDuTempsService emploiDuTempsService) {
        this.security = security;
        this.eleveRepository = eleveRepository;
        this.inscriptionRepository = inscriptionRepository;
        this.notificationRepository = notificationRepository;
        this.deviceTokenRepository = deviceTokenRepository;
        this.presenceEleveRepository = presenceEleveRepository;
        this.factureRepository = factureRepository;
        this.billing = billing;
        this.noteService = noteService;
        this.emploiDuTempsService = emploiDuTempsService;
    }

    @Transactional(readOnly = true)
    public ParentDashboardDto getDashboard() {
        security.requireParentRole();
        Long tuteurId = security.requireTuteurId();
        List<Eleve> enfants = eleveRepository.findByTuteurId(tuteurId);
        long nonLues = notificationRepository.countByTuteurIdAndLuFalse(tuteurId);
        BigDecimal solde = billing.getSoldeTuteur(tuteurId);
        return new ParentDashboardDto(
                enfants.size(),
                nonLues,
                solde,
                billing.getStatutTuteur(tuteurId));
    }

    @Transactional(readOnly = true)
    public List<ParentEnfantResumeDto> listEnfants() {
        security.requireParentRole();
        Long tuteurId = security.requireTuteurId();
        return eleveRepository.findByTuteurId(tuteurId).stream()
                .map(this::toEnfantResume)
                .toList();
    }

    @Transactional(readOnly = true)
    public ParentScolariteDto getScolarite(Long eleveId) {
        security.requireParentRole();
        security.requireEleveBelongsToParent(eleveId);
        Eleve eleve = requireEleve(eleveId);
        Inscription inscription = inscriptionRepository.findActiveForEleve(eleveId)
                .orElseThrow(() -> new ResourceNotFoundException("Aucune inscription active pour cet élève."));
        var classe = inscription.getClasse();
        var ecole = eleve.getEcole();
        var annee = inscription.getAnneeScolaire();
        return new ParentScolariteDto(
                eleve.getId(),
                eleve.getNomComplet(),
                eleve.getMatricule(),
                ecole.getNom(),
                ecole.getCode(),
                ecole.getTelephone(),
                ecole.getEmail(),
                classe.getLibelleComplet(),
                classe.getNiveau().getNom(),
                classe.getSalle(),
                annee.getNom(),
                annee.getDateDebut(),
                annee.getDateFin(),
                inscription.getStatut(),
                inscription.getDateInscription());
    }

    @Transactional(readOnly = true)
    public BulletinDetailDto getBulletin(Long eleveId) {
        security.requireParentRole();
        security.requireEleveBelongsToParent(eleveId);
        return noteService.getBulletinDetail(eleveId);
    }

    @Transactional(readOnly = true)
    public List<ParentAbsenceDto> getAbsences(Long eleveId) {
        security.requireParentRole();
        security.requireEleveBelongsToParent(eleveId);
        return presenceEleveRepository.findByEleveIdOrderByDateDesc(eleveId).stream()
                .map(this::toAbsence)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SeanceEmploiDto> getEmploiDuTemps(Long eleveId) {
        security.requireParentRole();
        security.requireEleveBelongsToParent(eleveId);
        Long classeId = inscriptionRepository.findActiveForEleve(eleveId)
                .map(i -> i.getClasse().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Aucune inscription active pour cet élève."));
        return emploiDuTempsService.listSeances(classeId, null);
    }

    @Transactional(readOnly = true)
    public List<FactureDto> listFactures() {
        security.requireParentRole();
        Long tuteurId = security.requireTuteurId();
        return factureRepository.findByTuteurId(tuteurId).stream()
                .map(this::toFactureDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public FactureDto getFacture(Long factureId) {
        security.requireParentRole();
        Long tuteurId = security.requireTuteurId();
        Facture facture = factureRepository.findByIdAndEleve_Tuteur_Id(factureId, tuteurId)
                .orElseThrow(() -> new ResourceNotFoundException("Facture introuvable : " + factureId));
        return toFactureDto(facture);
    }

    @Transactional(readOnly = true)
    public List<ParentNotificationDto> listNotifications() {
        security.requireParentRole();
        Long tuteurId = security.requireTuteurId();
        return notificationRepository.findByTuteurId(tuteurId).stream()
                .map(this::toNotification)
                .toList();
    }

    @Transactional(readOnly = true)
    public long countUnreadNotifications() {
        security.requireParentRole();
        return notificationRepository.countByTuteurIdAndLuFalse(security.requireTuteurId());
    }

    public void markNotificationRead(Long notificationId) {
        security.requireParentRole();
        Long tuteurId = security.requireTuteurId();
        NotificationParent n = notificationRepository.findByIdAndTuteurId(notificationId, tuteurId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification introuvable : " + notificationId));
        n.setLu(true);
        notificationRepository.save(n);
    }

    public void markAllNotificationsRead() {
        security.requireParentRole();
        notificationRepository.markAllRead(security.requireTuteurId());
    }

    public void registerDeviceToken(DeviceTokenRequest request) {
        security.requireParentRole();
        Tuteur tuteur = security.requireTuteur();
        deviceTokenRepository.findByTuteurIdAndToken(tuteur.getId(), request.token())
                .ifPresentOrElse(
                        existing -> {
                            existing.setPlateforme(request.plateforme());
                            deviceTokenRepository.save(existing);
                        },
                        () -> {
                            ParentDeviceToken token = new ParentDeviceToken();
                            token.setTuteur(tuteur);
                            token.setToken(request.token());
                            token.setPlateforme(request.plateforme());
                            deviceTokenRepository.save(token);
                        });
    }

    private ParentEnfantResumeDto toEnfantResume(Eleve e) {
        Inscription ins = inscriptionRepository.findActiveForEleve(e.getId()).orElse(null);
        String classe = ins != null ? ins.getClasse().getLibelleComplet() : "—";
        String niveau = ins != null ? ins.getClasse().getNiveau().getNom() : "—";
        String annee = ins != null ? ins.getAnneeScolaire().getNom() : "—";
        BigDecimal solde = billing.getSoldeEleve(e.getId());
        return new ParentEnfantResumeDto(
                e.getId(),
                e.getMatricule(),
                e.getNomComplet(),
                e.getSexe(),
                e.getDateNaissance(),
                e.getEcole().getNom(),
                classe,
                niveau,
                annee,
                billing.getStatutEleve(e.getId()),
                solde);
    }

    private ParentAbsenceDto toAbsence(PresenceEleve p) {
        if (p.getPlage() != null) {
            return new ParentAbsenceDto(
                    p.getId(),
                    p.getDateSeance(),
                    p.getPlage(),
                    p.getStatut(),
                    p.getMotif(),
                    p.getPlage());
        }
        var seance = p.getSeance();
        String horaire = seance.getHeureDebut() + " - " + seance.getHeureFin();
        return new ParentAbsenceDto(
                p.getId(),
                p.getDateSeance(),
                seance.getMatiere().getNom(),
                p.getStatut(),
                p.getMotif(),
                horaire);
    }

    private ParentNotificationDto toNotification(NotificationParent n) {
        String eleveNom = n.getEleve() != null ? n.getEleve().getNomComplet() : null;
        Long eleveId = n.getEleve() != null ? n.getEleve().getId() : null;
        return new ParentNotificationDto(
                n.getId(),
                n.getType(),
                n.getTitre(),
                n.getMessage(),
                n.isLu(),
                n.getDateCreation(),
                eleveId,
                eleveNom);
    }

    private Eleve requireEleve(Long eleveId) {
        return eleveRepository.findById(eleveId)
                .orElseThrow(() -> new ResourceNotFoundException("Élève introuvable : " + eleveId));
    }

    private FactureDto toFactureDto(Facture f) {
        BigDecimal reste = f.getMontantTotal().subtract(f.getMontantPaye());
        return new FactureDto(
                f.getId(),
                f.getNumero(),
                f.getCodePaiement(),
                f.getEleve().getId(),
                f.getEleve().getNomComplet(),
                f.getDateEmission(),
                f.getDateEcheance(),
                f.getMontantTotal(),
                f.getMontantPaye(),
                reste,
                f.getStatut());
    }
}
