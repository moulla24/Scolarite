package mr.ecoli.comptabilite.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import mr.ecoli.dao.entities.AnneeScolaire;
import mr.ecoli.dao.entities.EcheanceMensuelle;
import mr.ecoli.dao.entities.Eleve;
import mr.ecoli.dao.entities.Facture;
import mr.ecoli.dao.entities.FraisScolaire;
import mr.ecoli.dao.entities.HistoriqueRetard;
import mr.ecoli.dao.entities.ReglePenalite;
import mr.ecoli.dao.repositories.EcheanceMensuelleRepository;
import mr.ecoli.dao.repositories.FactureRepository;
import mr.ecoli.dao.repositories.FraisScolaireRepository;
import mr.ecoli.dao.repositories.HistoriqueRetardRepository;
import mr.ecoli.dao.repositories.InscriptionRepository;
import mr.ecoli.dao.repositories.ReglePenaliteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class EcheancePenaliteService {

    private final EcheanceMensuelleRepository echeanceRepository;
    private final HistoriqueRetardRepository retardRepository;
    private final ReglePenaliteRepository reglePenaliteRepository;
    private final FactureRepository factureRepository;
    private final FraisScolaireRepository fraisScolaireRepository;
    private final InscriptionRepository inscriptionRepository;

    public EcheancePenaliteService(
            EcheanceMensuelleRepository echeanceRepository,
            HistoriqueRetardRepository retardRepository,
            ReglePenaliteRepository reglePenaliteRepository,
            FactureRepository factureRepository,
            FraisScolaireRepository fraisScolaireRepository,
            InscriptionRepository inscriptionRepository) {
        this.echeanceRepository = echeanceRepository;
        this.retardRepository = retardRepository;
        this.reglePenaliteRepository = reglePenaliteRepository;
        this.factureRepository = factureRepository;
        this.fraisScolaireRepository = fraisScolaireRepository;
        this.inscriptionRepository = inscriptionRepository;
    }

    public int compterMoisScolaires(AnneeScolaire annee) {
        return moisScolaires(annee).size();
    }

    public BigDecimal montantMensualitesAnnuelles(List<FraisScolaire> fraisList, AnneeScolaire annee) {
        int nbMois = compterMoisScolaires(annee);
        return fraisList.stream()
                .filter(f -> "MENSUALITE".equals(f.getTypeFrais()))
                .map(FraisScolaire::getMontant)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .multiply(BigDecimal.valueOf(nbMois));
    }

    public void genererEcheances(
            Eleve eleve, AnneeScolaire annee, Facture facture, List<FraisScolaire> fraisMensualite) {
        if (fraisMensualite.isEmpty()) {
            return;
        }
        for (YearMonth ym : moisScolaires(annee)) {
            for (FraisScolaire frais : fraisMensualite) {
                LocalDate premierJour = ym.atDay(1);
                if (echeanceRepository.existsByEleveIdAndMoisAndTypeFrais(
                        eleve.getId(), premierJour, frais.getTypeFrais())) {
                    continue;
                }
                int jour = Math.min(frais.getJourEcheance(), ym.lengthOfMonth());
                EcheanceMensuelle echeance = new EcheanceMensuelle();
                echeance.setEleve(eleve);
                echeance.setAnneeScolaire(annee);
                echeance.setMois(premierJour);
                echeance.setTypeFrais(frais.getTypeFrais());
                echeance.setMontant(frais.getMontant());
                echeance.setDateEcheance(ym.atDay(jour));
                echeance.setStatut("IMPAYEE");
                echeance.setFacture(facture);
                echeanceRepository.save(echeance);
            }
        }
    }

    public void synchroniserEcheancesApresPaiement(Facture facture) {
        List<EcheanceMensuelle> echeances =
                echeanceRepository.findByFactureIdOrderByDateEcheanceAsc(facture.getId());
        if (echeances.isEmpty()) {
            return;
        }

        BigDecimal montantEcheances =
                echeances.stream().map(EcheanceMensuelle::getMontant).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal montantOneShot = facture.getMontantBrut().subtract(montantEcheances).max(BigDecimal.ZERO);
        BigDecimal resteAllouable = facture.getMontantPaye().subtract(montantOneShot).max(BigDecimal.ZERO);

        for (EcheanceMensuelle e : echeances) {
            if (resteAllouable.compareTo(e.getMontant()) >= 0) {
                e.setStatut("PAYEE");
                resteAllouable = resteAllouable.subtract(e.getMontant());
            } else if (resteAllouable.signum() > 0) {
                e.setStatut("PARTIELLE");
                resteAllouable = BigDecimal.ZERO;
            } else {
                e.setStatut("IMPAYEE");
            }
            echeanceRepository.save(e);
        }
    }

    public void recalculerPenalitesEleve(Long eleveId) {
        synchroniserEcheancesManquantes(eleveId);
        factureRepository.findByEleveIdOrderByDateEmissionDesc(eleveId).stream()
                .map(Facture::getId)
                .forEach(this::recalculerPenalitesFacture);
    }

    public void synchroniserEcheancesManquantes(Long eleveId) {
        List<Facture> factures = factureRepository.findByEleveIdOrderByDateEmissionDesc(eleveId);
        for (Facture facture : factures) {
            if (!echeanceRepository.findByFactureIdOrderByDateEcheanceAsc(facture.getId()).isEmpty()) {
                continue;
            }
            Eleve eleve = facture.getEleve();
            AnneeScolaire annee = facture.getAnneeScolaire();
            var inscription = inscriptionRepository.findActiveForEleve(eleveId).orElse(null);
            Long niveauId = inscription != null ? inscription.getClasse().getNiveau().getId() : null;
            Long classeId = inscription != null ? inscription.getClasse().getId() : null;
            List<FraisScolaire> fraisList = fraisScolaireRepository.findApplicable(
                    eleve.getEcole().getId(), annee.getId(), niveauId, classeId);
            fraisList = preferFraisSpecifiques(fraisList);
            List<FraisScolaire> mensualites = fraisList.stream()
                    .filter(f -> "MENSUALITE".equals(f.getTypeFrais()))
                    .toList();
            if (!mensualites.isEmpty()) {
                genererEcheances(eleve, annee, facture, mensualites);
            }
        }
    }

    public void recalculerPenalitesFacture(Long factureId) {
        Facture facture = factureRepository.findById(factureId).orElse(null);
        if (facture == null || facture.getMontantBrut() == null) {
            return;
        }

        Long ecoleId = facture.getEleve().getEcole().getId();
        List<ReglePenalite> regles = reglePenaliteRepository.findByEcoleIdAndActifTrueOrderByJoursRetardMinAsc(ecoleId);
        if (regles.isEmpty()) {
            return;
        }

        LocalDate today = LocalDate.now();
        BigDecimal totalPenalite = BigDecimal.ZERO;
        List<EcheanceMensuelle> echeances =
                echeanceRepository.findByFactureIdOrderByDateEcheanceAsc(facture.getId());

        for (EcheanceMensuelle echeance : echeances) {
            if ("PAYEE".equals(echeance.getStatut())) {
                continue;
            }
            long jours = ChronoUnit.DAYS.between(echeance.getDateEcheance(), today);
            if (jours <= 0) {
                continue;
            }
            ReglePenalite regle = regles.stream()
                    .filter(r -> jours >= r.getJoursRetardMin())
                    .reduce((first, second) -> second)
                    .orElse(null);
            if (regle == null) {
                continue;
            }
            BigDecimal penalite = calculerPenalite(regle, echeance.getMontant());
            totalPenalite = totalPenalite.add(penalite);
            enregistrerHistoriqueRetard(facture, echeance, (int) jours, penalite);
        }

        if (echeances.isEmpty() && facture.getResteAPayer().signum() > 0
                && facture.getDateEcheance().isBefore(today)) {
            long jours = ChronoUnit.DAYS.between(facture.getDateEcheance(), today);
            ReglePenalite regle = regles.stream()
                    .filter(r -> jours >= r.getJoursRetardMin())
                    .reduce((first, second) -> second)
                    .orElse(null);
            if (regle != null) {
                BigDecimal penalite = calculerPenalite(regle, facture.getResteAPayer());
                totalPenalite = totalPenalite.add(penalite);
                enregistrerHistoriqueRetardFacture(facture, (int) jours, penalite);
            }
        }

        facture.setMontantPenalite(totalPenalite);
        facture.setMontantTotal(facture.getMontantBrut()
                .subtract(facture.getMontantRemise())
                .add(totalPenalite)
                .max(BigDecimal.ZERO));
        if (facture.getResteAPayer().signum() <= 0 && facture.getMontantPaye().signum() > 0) {
            facture.setStatut("PAYEE");
        }
        factureRepository.save(facture);
    }

    @Transactional(readOnly = true)
    public List<EcheanceMensuelle> listEcheancesEleve(Long eleveId, Long anneeId) {
        if (anneeId != null) {
            return echeanceRepository.findByEleveIdAndAnneeScolaireIdOrderByMoisAsc(eleveId, anneeId);
        }
        return echeanceRepository.findByEleveIdOrderByDateEcheanceDesc(eleveId);
    }

    @Transactional(readOnly = true)
    public List<HistoriqueRetard> listRetardsEleve(Long eleveId) {
        return retardRepository.findByEleveIdOrderByDateConstatDesc(eleveId);
    }

    private void enregistrerHistoriqueRetard(
            Facture facture, EcheanceMensuelle echeance, int joursRetard, BigDecimal penalite) {
        String libelle = libelleMois(echeance.getMois());
        HistoriqueRetard existing = retardRepository
                .findByEleveIdAndLibelleEcheanceAndDateEcheance(
                        facture.getEleve().getId(), libelle, echeance.getDateEcheance())
                .orElse(null);
        if (existing != null) {
            existing.setJoursRetard(joursRetard);
            existing.setMontantPenalite(penalite);
            existing.setDateConstat(LocalDate.now());
            retardRepository.save(existing);
            return;
        }
        HistoriqueRetard retard = new HistoriqueRetard();
        retard.setFacture(facture);
        retard.setEleve(facture.getEleve());
        retard.setLibelleEcheance(libelle);
        retard.setDateEcheance(echeance.getDateEcheance());
        retard.setJoursRetard(joursRetard);
        retard.setMontantPenalite(penalite);
        retardRepository.save(retard);
    }

    private void enregistrerHistoriqueRetardFacture(Facture facture, int joursRetard, BigDecimal penalite) {
        String libelle = "Facture " + facture.getNumero();
        HistoriqueRetard existing = retardRepository
                .findByEleveIdAndLibelleEcheanceAndDateEcheance(
                        facture.getEleve().getId(), libelle, facture.getDateEcheance())
                .orElse(null);
        if (existing != null) {
            existing.setJoursRetard(joursRetard);
            existing.setMontantPenalite(penalite);
            existing.setDateConstat(LocalDate.now());
            retardRepository.save(existing);
            return;
        }
        HistoriqueRetard retard = new HistoriqueRetard();
        retard.setFacture(facture);
        retard.setEleve(facture.getEleve());
        retard.setLibelleEcheance(libelle);
        retard.setDateEcheance(facture.getDateEcheance());
        retard.setJoursRetard(joursRetard);
        retard.setMontantPenalite(penalite);
        retardRepository.save(retard);
    }

    private BigDecimal calculerPenalite(ReglePenalite regle, BigDecimal base) {
        if ("POURCENTAGE".equalsIgnoreCase(regle.getTypePenalite())) {
            return base.multiply(regle.getValeur())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }
        return regle.getValeur();
    }

    private List<YearMonth> moisScolaires(AnneeScolaire annee) {
        List<YearMonth> months = new ArrayList<>();
        YearMonth current = YearMonth.from(annee.getDateDebut());
        YearMonth fin = YearMonth.from(annee.getDateFin());
        while (!current.isAfter(fin)) {
            months.add(current);
            current = current.plusMonths(1);
        }
        return months;
    }

    public String libelleMois(LocalDate mois) {
        String nom = mois.getMonth().getDisplayName(TextStyle.FULL, Locale.FRENCH);
        return "Mensualité " + nom.substring(0, 1).toUpperCase() + nom.substring(1);
    }

    public long joursRetardEcheance(EcheanceMensuelle echeance) {
        if ("PAYEE".equals(echeance.getStatut())) {
            return 0;
        }
        long jours = ChronoUnit.DAYS.between(echeance.getDateEcheance(), LocalDate.now());
        return Math.max(jours, 0);
    }

    private List<FraisScolaire> preferFraisSpecifiques(List<FraisScolaire> fraisList) {
        boolean hasSpecific = fraisList.stream()
                .anyMatch(f -> f.getClasse() != null || f.getNiveau() != null);
        if (!hasSpecific) {
            return fraisList;
        }
        return fraisList.stream()
                .filter(f -> f.getClasse() != null || f.getNiveau() != null)
                .toList();
    }
}
