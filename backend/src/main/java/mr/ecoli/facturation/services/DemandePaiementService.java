package mr.ecoli.facturation.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import mr.ecoli.comptabilite.dtos.DemandePaiementDto;
import mr.ecoli.comptabilite.services.ComptabiliteJournalService;
import mr.ecoli.common.exception.BadRequestException;
import mr.ecoli.common.exception.ResourceNotFoundException;
import mr.ecoli.dao.entities.Comptable;
import mr.ecoli.dao.entities.DemandePaiement;
import mr.ecoli.dao.entities.Eleve;
import mr.ecoli.dao.entities.Facture;
import mr.ecoli.dao.entities.NotificationParent;
import mr.ecoli.dao.entities.Paiement;
import mr.ecoli.dao.entities.Tuteur;
import mr.ecoli.dao.repositories.ComptableRepository;
import mr.ecoli.dao.repositories.DemandePaiementRepository;
import mr.ecoli.dao.repositories.FactureRepository;
import mr.ecoli.dao.repositories.InscriptionRepository;
import mr.ecoli.dao.repositories.NotificationParentRepository;
import mr.ecoli.dao.repositories.PaiementRepository;
import mr.ecoli.facturation.dtos.PaiementDto;
import mr.ecoli.facturation.dtos.PaiementRequest;
import mr.ecoli.parentapp.dtos.DemandePaiementParentDto;
import mr.ecoli.parentapp.dtos.DemandePaiementParentRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DemandePaiementService {

    private final DemandePaiementRepository demandeRepository;
    private final FactureRepository factureRepository;
    private final PaiementService paiementService;
    private final PaiementRepository paiementRepository;
    private final ComptableRepository comptableRepository;
    private final InscriptionRepository inscriptionRepository;
    private final NotificationParentRepository notificationParentRepository;
    private final ComptabiliteJournalService comptabiliteJournalService;
    private final DemandePaiementDelaiService delaiService;

    public DemandePaiementService(
            DemandePaiementRepository demandeRepository,
            FactureRepository factureRepository,
            PaiementService paiementService,
            PaiementRepository paiementRepository,
            ComptableRepository comptableRepository,
            InscriptionRepository inscriptionRepository,
            NotificationParentRepository notificationParentRepository,
            ComptabiliteJournalService comptabiliteJournalService,
            DemandePaiementDelaiService delaiService) {
        this.demandeRepository = demandeRepository;
        this.factureRepository = factureRepository;
        this.paiementService = paiementService;
        this.paiementRepository = paiementRepository;
        this.comptableRepository = comptableRepository;
        this.inscriptionRepository = inscriptionRepository;
        this.notificationParentRepository = notificationParentRepository;
        this.comptabiliteJournalService = comptabiliteJournalService;
        this.delaiService = delaiService;
    }

    public DemandePaiementParentDto soumettre(Long tuteurId, DemandePaiementParentRequest request) {
        Facture facture = factureRepository
                .findByIdAndEleve_Tuteur_Id(request.factureId(), tuteurId)
                .orElseThrow(() -> new ResourceNotFoundException("Facture introuvable : " + request.factureId()));

        if ("PAYEE".equals(facture.getStatut())) {
            throw new BadRequestException("Cette facture est déjà payée.");
        }
        if (request.montant().compareTo(facture.getResteAPayer()) > 0) {
            throw new BadRequestException("Le montant dépasse le reste à payer.");
        }
        if (demandeRepository.existsByFactureIdAndStatut(facture.getId(), "EN_COURS")
                || demandeRepository.existsByFactureIdAndStatut(facture.getId(), "EN_ATTENTE")) {
            throw new BadRequestException(
                    "Un paiement est déjà en cours ou en attente pour cette facture.");
        }

        DemandePaiement demande = new DemandePaiement();
        demande.setFacture(facture);
        demande.setTuteur(facture.getEleve().getTuteur());
        demande.setMontant(request.montant());
        demande.setDatePaiement(request.datePaiement() != null ? request.datePaiement() : LocalDate.now());
        demande.setModePaiement(normalizeMode(request.modePaiement()));
        String codeAttendu = facture.getCodePaiement();
        String reference = request.reference() != null ? request.reference().trim() : "";
        if (reference.isBlank()) {
            reference = codeAttendu;
        } else if (!reference.equalsIgnoreCase(codeAttendu)) {
            throw new BadRequestException(
                    "La référence doit correspondre au code de paiement de la facture : " + codeAttendu);
        }
        demande.setReference(codeAttendu);
        demande.setPreuveReference(request.preuveReference());
        demande.setStatut("EN_COURS");
        DemandePaiement saved = demandeRepository.save(demande);

        Eleve eleve = facture.getEleve();
        notifierParent(
                saved.getTuteur(),
                eleve,
                "PAIEMENT_EN_COURS",
                "Paiement en cours",
                "Votre paiement de " + saved.getMontant() + " MRU via "
                        + libelleMode(saved.getModePaiement())
                        + " est en cours de traitement (simulation "
                        + delaiService.getDelaiSecondes() + " s)…");

        delaiService.programmerNotificationComptable(saved.getId());

        return demandeRepository
                .findByIdWithDetails(saved.getId())
                .map(this::toParentDto)
                .orElseGet(() -> toParentDto(saved));
    }

    @Transactional(readOnly = true)
    public DemandePaiementParentDto getPourParent(Long tuteurId, Long demandeId) {
        DemandePaiement demande = demandeRepository
                .findByIdAndTuteurId(demandeId, tuteurId)
                .orElseThrow(() -> new ResourceNotFoundException("Demande introuvable : " + demandeId));
        return toParentDto(demande);
    }

    @Transactional(readOnly = true)
    public List<DemandePaiementParentDto> listPourParent(Long tuteurId) {
        return demandeRepository.findByTuteurId(tuteurId).stream().map(this::toParentDto).toList();
    }

    @Transactional(readOnly = true)
    public List<DemandePaiementDto> listPourComptable(String statut) {
        return demandeRepository.findFiltered(statut).stream().map(this::toComptableDto).toList();
    }

    @Transactional(readOnly = true)
    public long countEnAttente() {
        return demandeRepository.countByStatut("EN_ATTENTE");
    }

    public int getDelaiSimulationSecondes() {
        return delaiService.getDelaiSecondes();
    }

    public DemandePaiementDto valider(Long demandeId, Long userId) {
        DemandePaiement demande = requireEnAttente(demandeId);

        PaiementDto paiement = paiementService.enregistrer(
                new PaiementRequest(
                        demande.getFacture().getId(),
                        demande.getMontant(),
                        demande.getDatePaiement(),
                        demande.getModePaiement(),
                        demande.getReference()),
                userId);

        demande.setStatut("VALIDE");
        demande.setPaiementId(paiement.id());
        demande.setDateTraitement(LocalDateTime.now());
        comptableRepository.findByUtilisateurId(userId).ifPresent(demande::setComptable);
        demandeRepository.save(demande);

        Eleve eleve = demande.getFacture().getEleve();
        notifierParent(
                demande.getTuteur(),
                eleve,
                "PAIEMENT_VALIDE",
                "Paiement confirmé",
                "Votre paiement de " + demande.getMontant() + " MRU pour "
                        + demande.getFacture().getNumero()
                        + " est confirmé. Reçu officiel n° " + paiement.numeroRecu() + ".");

        return toComptableDto(demande);
    }

    public DemandePaiementDto refuser(Long demandeId, Long userId, String motifRefus) {
        DemandePaiement demande = requireEnAttente(demandeId);

        demande.setStatut("REFUSE");
        demande.setMotifRefus(motifRefus.trim());
        demande.setDateTraitement(LocalDateTime.now());
        comptableRepository.findByUtilisateurId(userId).ifPresent(demande::setComptable);
        demandeRepository.save(demande);

        Eleve eleve = demande.getFacture().getEleve();
        notifierParent(
                demande.getTuteur(),
                eleve,
                "PAIEMENT_REFUSE",
                "Paiement refusé",
                "Votre déclaration de paiement pour " + demande.getFacture().getNumero()
                        + " a été refusée. Motif : " + motifRefus);

        return toComptableDto(demande);
    }

    private DemandePaiement requireEnAttente(Long demandeId) {
        DemandePaiement demande = demandeRepository
                .findByIdWithDetails(demandeId)
                .orElseThrow(() -> new ResourceNotFoundException("Demande introuvable : " + demandeId));
        if (!"EN_ATTENTE".equals(demande.getStatut())) {
            throw new BadRequestException("Cette demande a déjà été traitée.");
        }
        return demande;
    }

    private void notifierParent(Tuteur tuteur, Eleve eleve, String type, String titre, String message) {
        NotificationParent n = new NotificationParent();
        n.setTuteur(tuteur);
        n.setEleve(eleve);
        n.setType(type);
        n.setTitre(titre);
        n.setMessage(message);
        notificationParentRepository.save(n);
    }

    private DemandePaiementParentDto toParentDto(DemandePaiement d) {
        Facture f = d.getFacture();
        String numeroRecu = null;
        if (d.getPaiementId() != null) {
            numeroRecu = paiementRepository.findById(d.getPaiementId()).map(Paiement::getNumeroRecu).orElse(null);
        }
        return new DemandePaiementParentDto(
                d.getId(),
                f.getId(),
                f.getNumero(),
                f.getEleve().getNomComplet(),
                d.getMontant(),
                d.getDatePaiement(),
                d.getModePaiement(),
                d.getReference(),
                d.getPreuveReference(),
                d.getStatut(),
                d.getMotifRefus(),
                d.getPaiementId(),
                numeroRecu,
                d.getDateCreation(),
                d.getDateTraitement());
    }

    private DemandePaiementDto toComptableDto(DemandePaiement d) {
        Facture f = d.getFacture();
        Eleve eleve = f.getEleve();
        Tuteur tuteur = eleve.getTuteur();
        String numeroRecu = null;
        if (d.getPaiementId() != null) {
            numeroRecu = paiementRepository.findById(d.getPaiementId()).map(Paiement::getNumeroRecu).orElse(null);
        }
        String agentNom = null;
        if (d.getComptable() != null) {
            agentNom = formatNom(
                    d.getComptable().getUtilisateur().getPrenom(),
                    d.getComptable().getUtilisateur().getNom());
        }
        return new DemandePaiementDto(
                d.getId(),
                f.getId(),
                f.getNumero(),
                formatNom(tuteur.getPrenom(), tuteur.getNom()),
                tuteur.getTelephone(),
                eleve.getNomComplet(),
                niveauEleve(eleve.getId()),
                d.getMontant(),
                d.getDatePaiement(),
                d.getModePaiement(),
                d.getReference(),
                d.getPreuveReference(),
                d.getStatut(),
                d.getMotifRefus(),
                d.getPaiementId(),
                numeroRecu,
                agentNom,
                d.getDateCreation(),
                d.getDateTraitement());
    }

    private String niveauEleve(Long eleveId) {
        return inscriptionRepository
                .findActiveForEleve(eleveId)
                .map(i -> i.getClasse().getNiveau().getNom())
                .orElse("—");
    }

    private String normalizeMode(String mode) {
        if (mode == null || mode.isBlank()) return "ESPECES";
        return mode.toUpperCase();
    }

    private String libelleMode(String mode) {
        return switch (mode) {
            case "BANQUE" -> "application bancaire";
            case "MOBILE_MONEY" -> "Mobile Money";
            case "CHEQUE" -> "chèque";
            default -> "espèces";
        };
    }

    private String formatNom(Tuteur tuteur) {
        return formatNom(tuteur.getPrenom(), tuteur.getNom());
    }

    private String formatNom(String prenom, String nom) {
        if (prenom == null || prenom.isBlank()) return nom;
        return prenom + " " + nom;
    }
}
