package mr.ecoli.facturation.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import mr.ecoli.comptabilite.services.EcheancePenaliteService;
import mr.ecoli.dao.entities.AnneeScolaire;
import mr.ecoli.dao.entities.Eleve;
import mr.ecoli.dao.entities.Facture;
import mr.ecoli.dao.entities.FraisScolaire;
import mr.ecoli.dao.entities.Inscription;
import mr.ecoli.dao.entities.LigneFacture;
import mr.ecoli.dao.entities.RemiseEleve;
import mr.ecoli.dao.repositories.FactureRepository;
import mr.ecoli.dao.repositories.FraisScolaireRepository;
import mr.ecoli.dao.repositories.InscriptionRepository;
import mr.ecoli.dao.repositories.LigneFactureRepository;
import mr.ecoli.dao.repositories.RemiseEleveRepository;
import mr.ecoli.facturation.dtos.FactureDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class FactureService {

    private final FactureRepository factureRepository;
    private final FraisScolaireRepository fraisScolaireRepository;
    private final LigneFactureRepository ligneFactureRepository;
    private final InscriptionRepository inscriptionRepository;
    private final RemiseEleveRepository remiseEleveRepository;
    private final EcheancePenaliteService echeancePenaliteService;
    private final CodePaiementService codePaiementService;

    public FactureService(
            FactureRepository factureRepository,
            FraisScolaireRepository fraisScolaireRepository,
            LigneFactureRepository ligneFactureRepository,
            InscriptionRepository inscriptionRepository,
            RemiseEleveRepository remiseEleveRepository,
            EcheancePenaliteService echeancePenaliteService,
            CodePaiementService codePaiementService) {
        this.factureRepository = factureRepository;
        this.fraisScolaireRepository = fraisScolaireRepository;
        this.ligneFactureRepository = ligneFactureRepository;
        this.inscriptionRepository = inscriptionRepository;
        this.remiseEleveRepository = remiseEleveRepository;
        this.echeancePenaliteService = echeancePenaliteService;
        this.codePaiementService = codePaiementService;
    }

    public void generateForEleve(Eleve eleve, AnneeScolaire annee) {
        if (factureRepository.existsByEleveIdAndAnneeScolaire_Id(eleve.getId(), annee.getId())) {
            return;
        }
        Inscription inscription = inscriptionRepository.findActiveForEleve(eleve.getId()).orElse(null);
        Long niveauId = inscription != null ? inscription.getClasse().getNiveau().getId() : null;
        Long classeId = inscription != null ? inscription.getClasse().getId() : null;

        List<FraisScolaire> fraisList = fraisScolaireRepository.findApplicable(
                eleve.getEcole().getId(), annee.getId(), niveauId, classeId);
        fraisList = preferFraisSpecifiques(fraisList);
        if (fraisList.isEmpty()) {
            fraisList = preferFraisSpecifiques(fraisScolaireRepository.findByEcoleIdAndAnneeScolaireIdAndActifTrue(
                    eleve.getEcole().getId(), annee.getId()));
        }
        if (fraisList.isEmpty()) {
            return;
        }

        List<FraisScolaire> fraisPonctuels = new ArrayList<>();
        List<FraisScolaire> fraisMensualite = new ArrayList<>();
        for (FraisScolaire frais : fraisList) {
            if ("MENSUALITE".equals(frais.getTypeFrais())) {
                fraisMensualite.add(frais);
            } else {
                fraisPonctuels.add(frais);
            }
        }

        BigDecimal montantBrut = fraisPonctuels.stream()
                .map(FraisScolaire::getMontant)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        montantBrut = montantBrut.add(echeancePenaliteService.montantMensualitesAnnuelles(fraisMensualite, annee));

        BigDecimal remise = calculerRemise(eleve.getId(), montantBrut);
        BigDecimal total = montantBrut.subtract(remise).max(BigDecimal.ZERO);

        int jourEcheance = fraisList.stream()
                .mapToInt(FraisScolaire::getJourEcheance)
                .filter(j -> j > 0)
                .findFirst()
                .orElse(10);
        LocalDate echeance = LocalDate.now().withDayOfMonth(Math.min(jourEcheance, LocalDate.now().lengthOfMonth()));
        if (!echeance.isAfter(LocalDate.now())) {
            echeance = echeance.plusMonths(1);
        }

        Facture facture = new Facture();
        String numero = buildNumero(eleve.getId());
        facture.setNumero(numero);
        facture.setCodePaiement(codePaiementService.fromNumeroFacture(numero));
        facture.setEleve(eleve);
        facture.setAnneeScolaire(annee);
        facture.setDateEmission(LocalDate.now());
        facture.setDateEcheance(echeance);
        facture.setMontantBrut(montantBrut);
        facture.setMontantRemise(remise);
        facture.setMontantPenalite(BigDecimal.ZERO);
        facture.setMontantTotal(total);
        facture.setMontantPaye(BigDecimal.ZERO);
        facture.setStatut("IMPAYEE");
        facture = factureRepository.save(facture);

        int nbMois = echeancePenaliteService.compterMoisScolaires(annee);
        for (FraisScolaire frais : fraisPonctuels) {
            saveLigne(facture, frais.getLibelle(), frais.getMontant());
        }
        for (FraisScolaire frais : fraisMensualite) {
            saveLigne(
                    facture,
                    frais.getLibelle() + " (" + nbMois + " mois)",
                    frais.getMontant().multiply(BigDecimal.valueOf(nbMois)));
        }

        echeancePenaliteService.genererEcheances(eleve, annee, facture, fraisMensualite);
        echeancePenaliteService.recalculerPenalitesFacture(facture.getId());
    }

    private void saveLigne(Facture facture, String libelle, BigDecimal montant) {
        LigneFacture ligne = new LigneFacture();
        ligne.setFacture(facture);
        ligne.setLibelle(libelle);
        ligne.setMontant(montant);
        ligneFactureRepository.save(ligne);
    }

    private BigDecimal calculerRemise(Long eleveId, BigDecimal montantBrut) {
        BigDecimal remise = BigDecimal.ZERO;
        for (RemiseEleve r : remiseEleveRepository.findByEleveIdAndActifTrue(eleveId)) {
            if ("BOURSE_TOTALE".equals(r.getTypeRemise())) {
                return montantBrut;
            }
            if (r.getPourcentage() != null) {
                remise = remise.max(montantBrut.multiply(r.getPourcentage())
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
            }
            if (r.getMontantFixe() != null) {
                remise = remise.max(r.getMontantFixe());
            }
        }
        return remise.min(montantBrut);
    }

    @Transactional(readOnly = true)
    public List<FactureDto> listByEleve(Long eleveId) {
        return factureRepository.findByEleveIdOrderByDateEmissionDesc(eleveId).stream()
                .map(this::toDto)
                .toList();
    }

    private FactureDto toDto(Facture f) {
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
                f.getResteAPayer(),
                f.getStatut());
    }

    private String buildNumero(Long eleveId) {
        return "FAC-" + LocalDate.now().getYear() + "-" + String.format("%04d", eleveId);
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
