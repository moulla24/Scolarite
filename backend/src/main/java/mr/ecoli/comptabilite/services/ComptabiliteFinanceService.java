package mr.ecoli.comptabilite.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import mr.ecoli.comptabilite.dtos.DashboardFinancierDto;
import mr.ecoli.comptabilite.dtos.EcheanceMensuelleDto;
import mr.ecoli.comptabilite.dtos.HistoriqueRetardDto;
import mr.ecoli.comptabilite.dtos.DepenseScolaireDto;
import mr.ecoli.comptabilite.dtos.DepenseScolaireRequest;
import mr.ecoli.comptabilite.dtos.FraisScolaireDto;
import mr.ecoli.comptabilite.dtos.FraisScolaireRequest;
import mr.ecoli.comptabilite.dtos.PaiementHistoriqueDto;
import mr.ecoli.comptabilite.dtos.RapportFinancierDto;
import mr.ecoli.comptabilite.dtos.RemiseEleveDto;
import mr.ecoli.comptabilite.dtos.RemiseEleveRequest;
import mr.ecoli.comptabilite.dtos.RepartitionFraisDto;
import mr.ecoli.comptabilite.dtos.RevenuMensuelDto;
import mr.ecoli.comptabilite.dtos.SoldeEleveDto;
import mr.ecoli.common.exception.BadRequestException;
import mr.ecoli.common.exception.ResourceNotFoundException;
import mr.ecoli.common.EntityReferenceService;
import mr.ecoli.dao.entities.AnneeScolaire;
import mr.ecoli.dao.entities.Comptable;
import mr.ecoli.dao.entities.DepenseScolaire;
import mr.ecoli.dao.entities.EcheanceMensuelle;
import mr.ecoli.dao.entities.HistoriqueRetard;
import mr.ecoli.dao.entities.Ecole;
import mr.ecoli.dao.entities.Eleve;
import mr.ecoli.dao.entities.Facture;
import mr.ecoli.dao.entities.FraisScolaire;
import mr.ecoli.dao.entities.RemiseEleve;
import mr.ecoli.dao.repositories.AnneeScolaireRepository;
import mr.ecoli.dao.repositories.ComptableRepository;
import mr.ecoli.dao.repositories.DepenseScolaireRepository;
import mr.ecoli.dao.repositories.EleveRepository;
import mr.ecoli.dao.repositories.FactureRepository;
import mr.ecoli.dao.repositories.FraisScolaireRepository;
import mr.ecoli.dao.repositories.InscriptionRepository;
import mr.ecoli.dao.repositories.PaiementRepository;
import mr.ecoli.dao.repositories.RemiseEleveRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ComptabiliteFinanceService {

    private static final Map<String, String> TYPES_FRAIS = Map.of(
            "INSCRIPTION", "Inscription",
            "REINSCRIPTION", "Réinscription",
            "MENSUALITE", "Mensualité",
            "TRANSPORT", "Transport",
            "CANTINE", "Cantine",
            "UNIFORME", "Uniforme",
            "EXAMENS", "Examens");

    private static final Map<String, String> CATEGORIES_DEPENSE = Map.of(
            "SALAIRES", "Salaires",
            "FOURNITURES", "Fournitures",
            "ELECTRICITE", "Électricité",
            "INTERNET", "Internet",
            "MAINTENANCE", "Maintenance",
            "TRANSPORT", "Transport");

    private static final Map<String, String> TYPES_REMISE = Map.of(
            "FAMILIALE", "Réduction familiale",
            "BOURSE_PARTIELLE", "Bourse partielle",
            "BOURSE_TOTALE", "Bourse totale");

    private final FraisScolaireRepository fraisRepository;
    private final RemiseEleveRepository remiseRepository;
    private final DepenseScolaireRepository depenseRepository;
    private final AnneeScolaireRepository anneeRepository;
    private final FactureRepository factureRepository;
    private final PaiementRepository paiementRepository;
    private final EleveRepository eleveRepository;
    private final InscriptionRepository inscriptionRepository;
    private final ComptableRepository comptableRepository;
    private final ComptabiliteJournalService journalService;
    private final EntityReferenceService refs;
    private final EcheancePenaliteService echeancePenaliteService;

    public ComptabiliteFinanceService(
            FraisScolaireRepository fraisRepository,
            RemiseEleveRepository remiseRepository,
            DepenseScolaireRepository depenseRepository,
            AnneeScolaireRepository anneeRepository,
            FactureRepository factureRepository,
            PaiementRepository paiementRepository,
            EleveRepository eleveRepository,
            InscriptionRepository inscriptionRepository,
            ComptableRepository comptableRepository,
            ComptabiliteJournalService journalService,
            EntityReferenceService refs,
            EcheancePenaliteService echeancePenaliteService) {
        this.fraisRepository = fraisRepository;
        this.remiseRepository = remiseRepository;
        this.depenseRepository = depenseRepository;
        this.anneeRepository = anneeRepository;
        this.factureRepository = factureRepository;
        this.paiementRepository = paiementRepository;
        this.eleveRepository = eleveRepository;
        this.inscriptionRepository = inscriptionRepository;
        this.comptableRepository = comptableRepository;
        this.journalService = journalService;
        this.refs = refs;
        this.echeancePenaliteService = echeancePenaliteService;
    }

    @Transactional(readOnly = true)
    public DashboardFinancierDto getDashboard() {
        LocalDate today = LocalDate.now();
        LocalDate startWeek = today.with(DayOfWeek.MONDAY);
        LocalDate startMonth = today.withDayOfMonth(1);
        LocalDate startYear = today.withDayOfYear(1);

        List<RevenuMensuelDto> revenus = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            YearMonth ym = YearMonth.from(today).minusMonths(i);
            BigDecimal m = paiementRepository.sumBetween(ym.atDay(1), ym.atEndOfMonth());
            revenus.add(new RevenuMensuelDto(ym.getMonthValue(), ym.getYear(), m));
        }

        Map<String, BigDecimal> repartitionMap = new LinkedHashMap<>();
        AnneeScolaire annee = requireAnneeActive();
        for (FraisScolaire f : fraisRepository.findByEcoleIdAndAnneeScolaireIdAndActifTrue(
                annee.getEcole().getId(), annee.getId())) {
            repartitionMap.merge(f.getTypeFrais(), f.getMontant(), BigDecimal::add);
        }
        List<RepartitionFraisDto> repartition = repartitionMap.entrySet().stream()
                .map(e -> new RepartitionFraisDto(
                        e.getKey(), TYPES_FRAIS.getOrDefault(e.getKey(), e.getKey()), e.getValue()))
                .toList();

        return new DashboardFinancierDto(
                paiementRepository.sumBetween(today, today),
                paiementRepository.sumBetween(startWeek, today),
                paiementRepository.sumBetween(startMonth, today),
                paiementRepository.sumBetween(startYear, today),
                factureRepository.sumCreancesTotales(),
                factureRepository.countImpayees(),
                repartition,
                revenus);
    }

    @Transactional(readOnly = true)
    public List<FraisScolaireDto> listFrais() {
        AnneeScolaire annee = requireAnneeActive();
        return fraisRepository
                .findByEcoleIdAndAnneeScolaireIdAndActifTrue(annee.getEcole().getId(), annee.getId())
                .stream()
                .map(this::toFraisDto)
                .toList();
    }

    public FraisScolaireDto createFrais(FraisScolaireRequest request) {
        AnneeScolaire annee = requireAnneeActive();
        FraisScolaire f = new FraisScolaire();
        f.setEcole(annee.getEcole());
        f.setAnneeScolaire(annee);
        applyFrais(f, request);
        return toFraisDto(fraisRepository.save(f));
    }

    public FraisScolaireDto updateFrais(Long id, FraisScolaireRequest request) {
        FraisScolaire f = fraisRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Frais introuvable : " + id));
        applyFrais(f, request);
        return toFraisDto(fraisRepository.save(f));
    }

    @Transactional(readOnly = true)
    public List<RemiseEleveDto> listRemises(Long eleveId) {
        List<RemiseEleve> list = eleveId != null
                ? remiseRepository.findByEleveIdAndActifTrue(eleveId)
                : remiseRepository.findAll();
        return list.stream().map(this::toRemiseDto).toList();
    }

    public RemiseEleveDto createRemise(RemiseEleveRequest request) {
        Eleve eleve = eleveRepository.findById(request.eleveId())
                .orElseThrow(() -> new ResourceNotFoundException("Élève introuvable"));
        RemiseEleve r = new RemiseEleve();
        r.setEleve(eleve);
        applyRemise(r, request);
        return toRemiseDto(remiseRepository.save(r));
    }

    @Transactional(readOnly = true)
    public List<DepenseScolaireDto> listDepenses(String categorie) {
        return depenseRepository.findFiltered(normalize(categorie)).stream()
                .map(this::toDepenseDto)
                .toList();
    }

    public DepenseScolaireDto createDepense(DepenseScolaireRequest request, Long userId) {
        AnneeScolaire annee = requireAnneeActive();
        DepenseScolaire d = new DepenseScolaire();
        d.setEcole(annee.getEcole());
        d.setCategorie(request.categorie().toUpperCase());
        d.setDateDepense(request.dateDepense());
        d.setMontant(request.montant());
        d.setDescription(request.description().trim());
        d.setJustificatif(request.justificatif());
        resolveComptable(userId).ifPresent(d::setComptable);
        d = depenseRepository.save(d);
        journalService.enregistrerDepense(d);
        depenseRepository.save(d);
        return toDepenseDto(d);
    }

    @Transactional
    public SoldeEleveDto getSoldeEleve(Long eleveId) {
        echeancePenaliteService.recalculerPenalitesEleve(eleveId);

        Eleve eleve = eleveRepository.findById(eleveId)
                .orElseThrow(() -> new ResourceNotFoundException("Élève introuvable"));
        List<Facture> factures = factureRepository.findByEleveIdOrderByDateEmissionDesc(eleveId);
        BigDecimal total = factures.stream().map(Facture::getMontantTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal paye = factures.stream().map(Facture::getMontantPaye).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal brut = factures.stream()
                .map(f -> f.getMontantBrut() != null ? f.getMontantBrut() : f.getMontantTotal())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal remise = factures.stream().map(Facture::getMontantRemise).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal penalite = factures.stream().map(Facture::getMontantPenalite).reduce(BigDecimal.ZERO, BigDecimal::add);
        long impayees = factures.stream().filter(f -> !"PAYEE".equals(f.getStatut())).count();

        AnneeScolaire annee = requireAnneeActive();
        List<EcheanceMensuelleDto> echeances = echeancePenaliteService
                .listEcheancesEleve(eleveId, annee.getId())
                .stream()
                .map(this::toEcheanceDto)
                .toList();
        List<HistoriqueRetardDto> retards = echeancePenaliteService.listRetardsEleve(eleveId).stream()
                .map(this::toRetardDto)
                .toList();

        long joursRetard = echeances.stream()
                .mapToLong(EcheanceMensuelleDto::joursRetard)
                .max()
                .orElse(0);
        if (joursRetard == 0) {
            joursRetard = factures.stream()
                    .filter(f -> f.getResteAPayer().signum() > 0 && f.getDateEcheance().isBefore(LocalDate.now()))
                    .mapToLong(f -> ChronoUnit.DAYS.between(f.getDateEcheance(), LocalDate.now()))
                    .max()
                    .orElse(0);
        }

        var ins = inscriptionRepository.findActiveForEleve(eleveId);
        return new SoldeEleveDto(
                eleveId,
                eleve.getNomComplet(),
                ins.map(i -> i.getClasse().getNom()).orElse("—"),
                ins.map(i -> i.getClasse().getNiveau().getNom()).orElse("—"),
                brut,
                remise,
                penalite,
                total,
                paye,
                total.subtract(paye),
                joursRetard > 0 ? "EN_RETARD" : (paye.signum() > 0 ? "PARTIEL" : "A_JOUR"),
                impayees,
                joursRetard,
                echeances,
                retards);
    }

    @Transactional(readOnly = true)
    public List<EcheanceMensuelleDto> listEcheances(Long eleveId) {
        AnneeScolaire annee = requireAnneeActive();
        return echeancePenaliteService.listEcheancesEleve(eleveId, annee.getId()).stream()
                .map(this::toEcheanceDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<HistoriqueRetardDto> listRetards(Long eleveId) {
        return echeancePenaliteService.listRetardsEleve(eleveId).stream()
                .map(this::toRetardDto)
                .toList();
    }

    public void recalculerPenalites() {
        AnneeScolaire annee = requireAnneeActive();
        factureRepository.findAll().stream()
                .filter(f -> f.getAnneeScolaire().getId().equals(annee.getId()))
                .map(Facture::getId)
                .forEach(echeancePenaliteService::recalculerPenalitesFacture);
    }

    @Transactional(readOnly = true)
    public RapportFinancierDto getRapport(String type) {
        LocalDate today = LocalDate.now();
        LocalDate debut;
        LocalDate fin = today;
        String t = type == null ? "MENSUEL" : type.toUpperCase();
        debut = switch (t) {
            case "JOURNALIER" -> today;
            case "ANNUEL" -> today.withDayOfYear(1);
            default -> today.withDayOfMonth(1);
        };
        List<PaiementHistoriqueDto> paiements = paiementRepository
                .findFiltered(debut, fin, "", null, null, null, null, PageRequest.of(0, 500))
                .stream()
                .map(this::toPaiementHistorique)
                .toList();
        List<DepenseScolaireDto> depenses = depenseRepository.findFiltered("").stream()
                .filter(d -> !d.getDateDepense().isBefore(debut) && !d.getDateDepense().isAfter(fin))
                .map(this::toDepenseDto)
                .toList();
        BigDecimal enc = paiementRepository.sumBetween(debut, fin);
        BigDecimal dep = depenses.stream().map(DepenseScolaireDto::montant).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new RapportFinancierDto(t, debut, fin, enc, dep, enc.subtract(dep), paiements, depenses);
    }

    public Map<String, String> typesFrais() { return TYPES_FRAIS; }
    public Map<String, String> categoriesDepense() { return CATEGORIES_DEPENSE; }
    public Map<String, String> typesRemise() { return TYPES_REMISE; }

    public BigDecimal calculerRemise(Long eleveId, BigDecimal montantBrut) {
        BigDecimal remise = BigDecimal.ZERO;
        for (RemiseEleve r : remiseRepository.findByEleveIdAndActifTrue(eleveId)) {
            if (r.getPourcentage() != null) {
                remise = remise.max(montantBrut.multiply(r.getPourcentage())
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
            }
            if (r.getMontantFixe() != null) {
                remise = remise.max(r.getMontantFixe());
            }
            if ("BOURSE_TOTALE".equals(r.getTypeRemise())) {
                return montantBrut;
            }
        }
        return remise.min(montantBrut);
    }

    private void applyFrais(FraisScolaire f, FraisScolaireRequest request) {
        if (!TYPES_FRAIS.containsKey(request.typeFrais().toUpperCase())) {
            throw new BadRequestException("Type de frais invalide");
        }
        f.setTypeFrais(request.typeFrais().toUpperCase());
        f.setLibelle(request.libelle().trim());
        f.setMontant(request.montant());
        f.setJourEcheance(request.jourEcheance() > 0 ? request.jourEcheance() : 10);
        f.setActif(request.actif());
        f.setNiveau(request.niveauId() != null ? refs.requireNiveau(request.niveauId()) : null);
        f.setClasse(request.classeId() != null ? refs.requireClasse(request.classeId()) : null);
    }

    private void applyRemise(RemiseEleve r, RemiseEleveRequest request) {
        if (!TYPES_REMISE.containsKey(request.typeRemise().toUpperCase())) {
            throw new BadRequestException("Type de remise invalide");
        }
        r.setTypeRemise(request.typeRemise().toUpperCase());
        r.setLibelle(request.libelle());
        r.setPourcentage(request.pourcentage());
        r.setMontantFixe(request.montantFixe());
        r.setActif(request.actif());
        r.setDateDebut(request.dateDebut());
        r.setDateFin(request.dateFin());
    }

    private AnneeScolaire requireAnneeActive() {
        return anneeRepository.findFirstByActiveTrue()
                .orElseThrow(() -> new BadRequestException("Aucune année scolaire active"));
    }

    private java.util.Optional<Comptable> resolveComptable(Long userId) {
        if (userId == null) return java.util.Optional.empty();
        return comptableRepository.findByUtilisateurId(userId);
    }

    private FraisScolaireDto toFraisDto(FraisScolaire f) {
        return new FraisScolaireDto(
                f.getId(),
                f.getTypeFrais(),
                TYPES_FRAIS.getOrDefault(f.getTypeFrais(), f.getTypeFrais()),
                f.getLibelle(),
                f.getMontant(),
                f.getNiveau() != null ? f.getNiveau().getId() : null,
                f.getNiveau() != null ? f.getNiveau().getNom() : null,
                f.getClasse() != null ? f.getClasse().getId() : null,
                f.getClasse() != null ? f.getClasse().getNom() : null,
                f.getJourEcheance(),
                f.isActif());
    }

    private RemiseEleveDto toRemiseDto(RemiseEleve r) {
        return new RemiseEleveDto(
                r.getId(),
                r.getEleve().getId(),
                r.getEleve().getNomComplet(),
                r.getTypeRemise(),
                TYPES_REMISE.getOrDefault(r.getTypeRemise(), r.getTypeRemise()),
                r.getLibelle(),
                r.getPourcentage(),
                r.getMontantFixe(),
                r.isActif(),
                r.getDateDebut(),
                r.getDateFin());
    }

    private DepenseScolaireDto toDepenseDto(DepenseScolaire d) {
        String agent = d.getComptable() != null
                ? d.getComptable().getUtilisateur().getPrenom() + " " + d.getComptable().getUtilisateur().getNom()
                : null;
        return new DepenseScolaireDto(
                d.getId(),
                d.getCategorie(),
                CATEGORIES_DEPENSE.getOrDefault(d.getCategorie(), d.getCategorie()),
                d.getDateDepense(),
                d.getMontant(),
                d.getDescription(),
                d.getJustificatif(),
                agent);
    }

    private PaiementHistoriqueDto toPaiementHistorique(mr.ecoli.dao.entities.Paiement p) {
        var facture = p.getFacture();
        var eleve = facture.getEleve();
        var tuteur = eleve.getTuteur();
        String agent = p.getComptable() != null
                ? p.getComptable().getUtilisateur().getPrenom() + " " + p.getComptable().getUtilisateur().getNom()
                : null;
        return new PaiementHistoriqueDto(
                p.getId(),
                facture.getId(),
                facture.getNumero(),
                formatNom(tuteur.getPrenom(), tuteur.getNom()),
                eleve.getNomComplet(),
                inscriptionRepository.findActiveForEleve(eleve.getId())
                        .map(i -> i.getClasse().getNiveau().getNom())
                        .orElse("—"),
                p.getMontant(),
                p.getDatePaiement(),
                p.getModePaiement(),
                p.getReference(),
                p.getNumeroRecu(),
                agent);
    }

    private EcheanceMensuelleDto toEcheanceDto(EcheanceMensuelle e) {
        return new EcheanceMensuelleDto(
                e.getId(),
                echeancePenaliteService.libelleMois(e.getMois()),
                e.getMois(),
                e.getMontant(),
                e.getDateEcheance(),
                e.getStatut(),
                echeancePenaliteService.joursRetardEcheance(e));
    }

    private HistoriqueRetardDto toRetardDto(HistoriqueRetard r) {
        return new HistoriqueRetardDto(
                r.getId(),
                r.getLibelleEcheance(),
                r.getDateEcheance(),
                r.getJoursRetard(),
                r.getMontantPenalite(),
                r.getDateConstat());
    }

    private String formatNom(String prenom, String nom) {
        if (prenom == null || prenom.isBlank()) return nom;
        return prenom + " " + nom;
    }

    private String normalize(String v) {
        return v == null ? "" : v.trim();
    }
}
