package mr.ecoli.comptabilite.services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import mr.ecoli.comptabilite.dtos.BilanDto;
import mr.ecoli.comptabilite.dtos.BilanLigneDto;
import mr.ecoli.comptabilite.dtos.ComptabiliteStatsDto;
import mr.ecoli.comptabilite.dtos.EcritureComptableDto;
import mr.ecoli.comptabilite.dtos.EcritureComptableRequest;
import mr.ecoli.comptabilite.dtos.FactureDetailDto;
import mr.ecoli.comptabilite.dtos.FactureListDto;
import mr.ecoli.comptabilite.dtos.LigneFactureDto;
import mr.ecoli.comptabilite.dtos.NotificationComptableDto;
import mr.ecoli.comptabilite.dtos.PaiementHistoriqueDto;
import mr.ecoli.common.exception.BadRequestException;
import mr.ecoli.common.exception.ResourceNotFoundException;
import mr.ecoli.dao.entities.EcritureComptable;
import mr.ecoli.dao.entities.Facture;
import mr.ecoli.dao.entities.LigneFacture;
import mr.ecoli.dao.entities.NotificationComptable;
import mr.ecoli.dao.entities.Paiement;
import mr.ecoli.dao.repositories.EcritureComptableRepository;
import mr.ecoli.dao.repositories.FactureRepository;
import mr.ecoli.dao.repositories.InscriptionRepository;
import mr.ecoli.dao.repositories.LigneFactureRepository;
import mr.ecoli.dao.repositories.NotificationComptableRepository;
import mr.ecoli.dao.repositories.PaiementPersonnelRepository;
import mr.ecoli.dao.repositories.PaiementRepository;
import mr.ecoli.dao.repositories.PersonnelRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ComptabiliteService {

    private static final Map<String, String> COMPTES = Map.of(
            "512", "Banque / Caisse",
            "411", "Clients — parents d'élèves",
            "641", "Charges de personnel",
            "421", "Dettes salariales",
            "706", "Produits — frais scolaires");

    private final FactureRepository factureRepository;
    private final PaiementRepository paiementRepository;
    private final LigneFactureRepository ligneFactureRepository;
    private final EcritureComptableRepository ecritureRepository;
    private final NotificationComptableRepository notificationRepository;
    private final InscriptionRepository inscriptionRepository;
    private final PaiementPersonnelRepository paiementPersonnelRepository;
    private final PersonnelRepository personnelRepository;

    public ComptabiliteService(
            FactureRepository factureRepository,
            PaiementRepository paiementRepository,
            LigneFactureRepository ligneFactureRepository,
            EcritureComptableRepository ecritureRepository,
            NotificationComptableRepository notificationRepository,
            InscriptionRepository inscriptionRepository,
            PaiementPersonnelRepository paiementPersonnelRepository,
            PersonnelRepository personnelRepository) {
        this.factureRepository = factureRepository;
        this.paiementRepository = paiementRepository;
        this.ligneFactureRepository = ligneFactureRepository;
        this.ecritureRepository = ecritureRepository;
        this.notificationRepository = notificationRepository;
        this.inscriptionRepository = inscriptionRepository;
        this.paiementPersonnelRepository = paiementPersonnelRepository;
        this.personnelRepository = personnelRepository;
    }

    public ComptabiliteStatsDto getStats(Long userId) {
        YearMonth mois = YearMonth.now();
        LocalDate debut = mois.atDay(1);
        LocalDate fin = mois.atEndOfMonth();
        return new ComptabiliteStatsDto(
                ecritureRepository.sumEncaissementsBetween(debut, fin),
                factureRepository.sumCreancesTotales(),
                factureRepository.sumMontantTotal(),
                factureRepository.countImpayees(),
                paiementRepository.countBetween(debut, fin),
                userId != null ? notificationRepository.countByUtilisateurIdAndLuFalse(userId) : 0);
    }

    public List<FactureListDto> listFactures(
            String statut, String search, Long niveauId, Long classeId, Long eleveId) {
        return factureRepository
                .searchAll(normalize(statut), normalize(search), niveauId, classeId, eleveId)
                .stream()
                .map(this::toFactureListDto)
                .toList();
    }

    public FactureDetailDto getFactureDetail(Long id) {
        Facture f = factureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Facture introuvable : " + id));
        var eleve = f.getEleve();
        var tuteur = eleve.getTuteur();
        List<LigneFactureDto> lignes = ligneFactureRepository.findByFactureId(f.getId()).stream()
                .map(l -> new LigneFactureDto(l.getId(), l.getLibelle(), l.getMontant()))
                .toList();
        List<PaiementHistoriqueDto> paiements = paiementRepository.findByFactureIdOrderByDatePaiementDesc(f.getId())
                .stream()
                .map(this::toPaiementDto)
                .toList();
        return new FactureDetailDto(
                f.getId(),
                f.getNumero(),
                f.getCodePaiement(),
                eleve.getId(),
                eleve.getNomComplet(),
                formatNom(tuteur.getPrenom(), tuteur.getNom()),
                tuteur.getTelephone(),
                tuteur.getEmail(),
                niveauEleve(eleve.getId()),
                f.getAnneeScolaire().getNom(),
                f.getDateEmission(),
                f.getDateEcheance(),
                f.getMontantTotal(),
                f.getMontantPaye(),
                f.getResteAPayer(),
                f.getStatut(),
                lignes,
                paiements);
    }

    public List<PaiementHistoriqueDto> listPaiements(
            LocalDate from,
            LocalDate to,
            String mode,
            Long niveauId,
            Long classeId,
            Long eleveId,
            String search,
            int limit) {
        return paiementRepository
                .findFiltered(
                        from,
                        to,
                        normalize(mode),
                        niveauId,
                        classeId,
                        eleveId,
                        normalize(search),
                        PageRequest.of(0, limit))
                .stream()
                .map(this::toPaiementDto)
                .toList();
    }

    @Transactional
    public EcritureComptableDto createEcriture(EcritureComptableRequest request) {
        EcritureComptable e = new EcritureComptable();
        applyEcriture(e, request);
        e.setReferenceType("MANUEL");
        if (e.getNumeroPiece() == null || e.getNumeroPiece().isBlank()) {
            e.setNumeroPiece("MAN-" + System.currentTimeMillis());
        }
        return toEcritureDto(ecritureRepository.save(e));
    }

    @Transactional
    public EcritureComptableDto updateEcriture(Long id, EcritureComptableRequest request) {
        EcritureComptable e = requireModifiableEcriture(id);
        applyEcriture(e, request);
        return toEcritureDto(ecritureRepository.save(e));
    }

    @Transactional
    public void deleteEcriture(Long id) {
        EcritureComptable e = requireModifiableEcriture(id);
        ecritureRepository.delete(e);
    }

    private EcritureComptable requireModifiableEcriture(Long id) {
        EcritureComptable e = ecritureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Écriture introuvable : " + id));
        if (!isModifiable(e)) {
            throw new BadRequestException("Cette écriture automatique ne peut pas être modifiée.");
        }
        return e;
    }

    private void applyEcriture(EcritureComptable e, EcritureComptableRequest request) {
        e.setDateEcriture(request.dateEcriture());
        e.setLibelle(request.libelle().trim());
        e.setCompteDebit(request.compteDebit().trim());
        e.setCompteCredit(request.compteCredit().trim());
        e.setMontant(request.montant());
        if (request.numeroPiece() != null && !request.numeroPiece().isBlank()) {
            e.setNumeroPiece(request.numeroPiece().trim());
        }
    }

    private boolean isModifiable(EcritureComptable e) {
        String type = e.getReferenceType();
        return type == null || type.isBlank() || "MANUEL".equals(type);
    }

    public List<EcritureComptableDto> listJournal(
            LocalDate from, LocalDate to, String referenceType, String compte, String search, int limit) {
        return ecritureRepository
                .findFiltered(from, to, normalize(referenceType), normalize(compte), normalize(search), PageRequest.of(0, limit))
                .stream()
                .map(this::toEcritureDto)
                .toList();
    }

    public BilanDto getBilan(LocalDate from, LocalDate to) {
        LocalDate debut = from != null ? from : YearMonth.now().atDay(1);
        LocalDate fin = to != null ? to : LocalDate.now();

        BigDecimal produits = ecritureRepository.sumEncaissementsBetween(debut, fin);
        BigDecimal chargesPersonnel = ecritureRepository.sumChargesPersonnelBetween(debut, fin);
        BigDecimal entrees = ecritureRepository.sumTresorerieEntreesBetween(debut, fin);
        BigDecimal sorties = ecritureRepository.sumTresorerieSortiesBetween(debut, fin);
        BigDecimal tresorerieNette = entrees.subtract(sorties);
        BigDecimal creances = factureRepository.sumCreancesTotales();
        BigDecimal resultatNet = produits.subtract(chargesPersonnel);

        BigDecimal masseSalariale = personnelRepository.search("", "ACTIF", "").stream()
                .map(p -> p.getSalaireMensuel())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<BilanLigneDto> actif = List.of(
                new BilanLigneDto("512", COMPTES.get("512"), tresorerieNette.max(BigDecimal.ZERO)),
                new BilanLigneDto("411", COMPTES.get("411"), creances));
        List<BilanLigneDto> passif = List.of(
                new BilanLigneDto("706", COMPTES.get("706"), produits),
                new BilanLigneDto("421", COMPTES.get("421"), masseSalariale));
        List<BilanLigneDto> resultat = List.of(
                new BilanLigneDto("706", "Recettes scolaires", produits),
                new BilanLigneDto("641", COMPTES.get("641"), chargesPersonnel.negate()),
                new BilanLigneDto("R", "Résultat net", resultatNet));

        BigDecimal totalActif = actif.stream().map(BilanLigneDto::montant).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalPassif = passif.stream().map(BilanLigneDto::montant).reduce(BigDecimal.ZERO, BigDecimal::add);

        return new BilanDto(
                debut,
                fin,
                totalActif,
                totalPassif,
                entrees,
                creances,
                produits,
                chargesPersonnel,
                resultatNet,
                tresorerieNette,
                actif,
                passif,
                resultat);
    }

    public List<NotificationComptableDto> listNotifications(Long userId) {
        return notificationRepository.findByUtilisateurIdOrderByDateCreationDesc(userId).stream()
                .map(this::toNotificationDto)
                .toList();
    }

    @Transactional
    public void markNotificationRead(Long userId, Long notificationId) {
        notificationRepository.markAsRead(notificationId, userId);
    }

    @Transactional
    public void markAllNotificationsRead(Long userId) {
        notificationRepository.markAllAsRead(userId);
    }

    private FactureListDto toFactureListDto(Facture f) {
        var eleve = f.getEleve();
        var tuteur = eleve.getTuteur();
        return new FactureListDto(
                f.getId(),
                f.getNumero(),
                f.getCodePaiement(),
                eleve.getId(),
                eleve.getNomComplet(),
                formatNom(tuteur.getPrenom(), tuteur.getNom()),
                tuteur.getTelephone(),
                niveauEleve(eleve.getId()),
                f.getDateEmission(),
                f.getDateEcheance(),
                f.getMontantTotal(),
                f.getMontantPaye(),
                f.getResteAPayer(),
                f.getStatut());
    }

    private PaiementHistoriqueDto toPaiementDto(Paiement p) {
        var facture = p.getFacture();
        var eleve = facture.getEleve();
        var tuteur = eleve.getTuteur();
        return new PaiementHistoriqueDto(
                p.getId(),
                facture.getId(),
                facture.getNumero(),
                formatNom(tuteur.getPrenom(), tuteur.getNom()),
                eleve.getNomComplet(),
                niveauEleve(eleve.getId()),
                p.getMontant(),
                p.getDatePaiement(),
                p.getModePaiement(),
                p.getReference(),
                p.getNumeroRecu(),
                p.getComptable() != null
                        ? formatNom(
                                p.getComptable().getUtilisateur().getPrenom(),
                                p.getComptable().getUtilisateur().getNom())
                        : null);
    }

    private EcritureComptableDto toEcritureDto(EcritureComptable e) {
        return new EcritureComptableDto(
                e.getId(),
                e.getDateEcriture(),
                e.getNumeroPiece(),
                e.getLibelle(),
                e.getCompteDebit(),
                e.getCompteCredit(),
                COMPTES.getOrDefault(e.getCompteDebit(), e.getCompteDebit()),
                COMPTES.getOrDefault(e.getCompteCredit(), e.getCompteCredit()),
                e.getMontant(),
                e.getReferenceType(),
                e.getReferenceId(),
                isModifiable(e));
    }

    private NotificationComptableDto toNotificationDto(NotificationComptable n) {
        return new NotificationComptableDto(
                n.getId(),
                n.getType(),
                n.getTitre(),
                n.getMessage(),
                n.isLu(),
                n.getPaiementId(),
                n.getDemandePaiementId(),
                n.getDateCreation());
    }

    private String niveauEleve(Long eleveId) {
        return inscriptionRepository.findActiveForEleve(eleveId)
                .map(i -> i.getClasse().getNiveau().getNom())
                .orElse("—");
    }

    private String formatNom(String prenom, String nom) {
        if (prenom == null || prenom.isBlank()) return nom;
        return prenom + " " + nom;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}
