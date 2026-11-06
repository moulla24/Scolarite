package mr.ecoli.facturation.services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import mr.ecoli.common.exception.BadRequestException;
import mr.ecoli.comptabilite.services.ComptabiliteFinanceService;
import mr.ecoli.comptabilite.services.ComptabiliteJournalService;
import mr.ecoli.comptabilite.services.EcheancePenaliteService;
import mr.ecoli.comptabilite.services.RecuNumeroService;
import mr.ecoli.dao.entities.Comptable;
import mr.ecoli.dao.entities.Facture;
import mr.ecoli.dao.entities.Paiement;
import mr.ecoli.dao.repositories.ComptableRepository;
import mr.ecoli.dao.repositories.FactureRepository;
import mr.ecoli.dao.repositories.InscriptionRepository;
import mr.ecoli.dao.repositories.PaiementRepository;
import mr.ecoli.facturation.dtos.PaiementDto;
import mr.ecoli.facturation.dtos.PaiementRequest;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PaiementService {

    private final PaiementRepository paiementRepository;
    private final FactureRepository factureRepository;
    private final InscriptionRepository inscriptionRepository;
    private final ComptableRepository comptableRepository;
    private final ComptabiliteJournalService comptabiliteJournalService;
    private final RecuNumeroService recuNumeroService;
    private final EcheancePenaliteService echeancePenaliteService;

    public PaiementService(
            PaiementRepository paiementRepository,
            FactureRepository factureRepository,
            InscriptionRepository inscriptionRepository,
            ComptableRepository comptableRepository,
            ComptabiliteJournalService comptabiliteJournalService,
            RecuNumeroService recuNumeroService,
            EcheancePenaliteService echeancePenaliteService) {
        this.paiementRepository = paiementRepository;
        this.factureRepository = factureRepository;
        this.inscriptionRepository = inscriptionRepository;
        this.comptableRepository = comptableRepository;
        this.comptabiliteJournalService = comptabiliteJournalService;
        this.recuNumeroService = recuNumeroService;
        this.echeancePenaliteService = echeancePenaliteService;
    }

    public PaiementDto enregistrer(PaiementRequest request, Long userId) {
        Facture facture = factureRepository
                .findById(request.factureId())
                .orElseThrow(() -> new BadRequestException("Facture introuvable : " + request.factureId()));

        if (request.montant().compareTo(facture.getResteAPayer()) > 0) {
            throw new BadRequestException("Le montant dépasse le reste à payer.");
        }

        Paiement paiement = new Paiement();
        paiement.setFacture(facture);
        paiement.setMontant(request.montant());
        paiement.setDatePaiement(request.datePaiement() != null ? request.datePaiement() : LocalDate.now());
        paiement.setModePaiement(normalizeMode(request.modePaiement()));
        paiement.setReference(request.reference());
        paiement.setNumeroRecu(recuNumeroService.genererNumeroRecu());
        comptableRepository.findByUtilisateurId(userId).ifPresent(paiement::setComptable);
        paiement = paiementRepository.save(paiement);

        facture.setMontantPaye(facture.getMontantPaye().add(request.montant()));
        if (facture.getResteAPayer().compareTo(BigDecimal.ZERO) <= 0) {
            facture.setStatut("PAYEE");
        } else if (facture.getMontantPaye().signum() > 0) {
            facture.setStatut("PARTIELLE");
        }
        factureRepository.save(facture);

        echeancePenaliteService.synchroniserEcheancesApresPaiement(facture);
        echeancePenaliteService.recalculerPenalitesFacture(facture.getId());

        comptabiliteJournalService.enregistrerPaiement(paiement);

        return toDto(paiement);
    }

    @Transactional(readOnly = true)
    public List<PaiementDto> listRecent(int limit) {
        return listRecent(limit, null);
    }

    @Transactional(readOnly = true)
    public List<PaiementDto> listRecent(int limit, Long niveauId) {
        var page = PageRequest.of(0, limit);
        var paiements = niveauId == null
                ? paiementRepository.findRecent(page)
                : paiementRepository.findRecentByNiveau(niveauId, page);
        return paiements.stream().map(this::toDto).toList();
    }

    private PaiementDto toDto(Paiement p) {
        var facture = p.getFacture();
        var eleve = facture.getEleve();
        var tuteur = eleve.getTuteur();
        String parentNom = formatNom(tuteur.getPrenom(), tuteur.getNom());
        String niveau = inscriptionRepository.findActiveForEleve(eleve.getId())
                .map(i -> i.getClasse().getNiveau().getNom())
                .orElse("—");
        String agent = p.getComptable() != null
                ? formatNom(p.getComptable().getUtilisateur().getPrenom(), p.getComptable().getUtilisateur().getNom())
                : null;
        return new PaiementDto(
                p.getId(),
                facture.getId(),
                facture.getNumero(),
                parentNom,
                eleve.getNomComplet(),
                niveau,
                p.getMontant(),
                p.getDatePaiement(),
                p.getModePaiement(),
                p.getReference(),
                p.getNumeroRecu(),
                agent);
    }

    private String normalizeMode(String mode) {
        if (mode == null || mode.isBlank()) return "ESPECES";
        return mode.toUpperCase();
    }

    private String formatNom(String prenom, String nom) {
        if (prenom == null || prenom.isBlank()) return nom;
        return prenom + " " + nom;
    }
}
