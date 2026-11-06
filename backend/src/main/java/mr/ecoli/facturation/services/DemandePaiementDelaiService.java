package mr.ecoli.facturation.services;

import java.time.Instant;
import mr.ecoli.comptabilite.services.ComptabiliteJournalService;
import mr.ecoli.dao.entities.DemandePaiement;
import mr.ecoli.dao.entities.Eleve;
import mr.ecoli.dao.entities.Facture;
import mr.ecoli.dao.entities.NotificationParent;
import mr.ecoli.dao.entities.Tuteur;
import mr.ecoli.dao.repositories.DemandePaiementRepository;
import mr.ecoli.dao.repositories.NotificationParentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class DemandePaiementDelaiService {

    private static final Logger log = LoggerFactory.getLogger(DemandePaiementDelaiService.class);

    private final TaskScheduler taskScheduler;
    private final TransactionTemplate transactionTemplate;
    private final DemandePaiementRepository demandeRepository;
    private final NotificationParentRepository notificationParentRepository;
    private final ComptabiliteJournalService comptabiliteJournalService;

    @Value("${ecoli.paiement.simulation-delai-secondes:30}")
    private int delaiSecondes;

    public DemandePaiementDelaiService(
            TaskScheduler taskScheduler,
            TransactionTemplate transactionTemplate,
            DemandePaiementRepository demandeRepository,
            NotificationParentRepository notificationParentRepository,
            ComptabiliteJournalService comptabiliteJournalService) {
        this.taskScheduler = taskScheduler;
        this.transactionTemplate = transactionTemplate;
        this.demandeRepository = demandeRepository;
        this.notificationParentRepository = notificationParentRepository;
        this.comptabiliteJournalService = comptabiliteJournalService;
    }

    public int getDelaiSecondes() {
        return delaiSecondes;
    }

    public void programmerNotificationComptable(Long demandeId) {
        log.info("Simulation bancaire : notification comptable programmée dans {} s (demande #{})",
                delaiSecondes, demandeId);
        taskScheduler.schedule(
                () -> transactionTemplate.executeWithoutResult(status -> transmettreAuComptable(demandeId)),
                Instant.now().plusSeconds(delaiSecondes));
    }

    private void transmettreAuComptable(Long demandeId) {
        DemandePaiement demande = demandeRepository.findByIdWithDetails(demandeId).orElse(null);
        if (demande == null || !"EN_COURS".equals(demande.getStatut())) {
            return;
        }

        demande.setStatut("EN_ATTENTE");
        demandeRepository.save(demande);

        Facture facture = demande.getFacture();
        Eleve eleve = facture.getEleve();
        Tuteur tuteur = demande.getTuteur();

        String message = "Le parent " + formatNom(tuteur)
                + " a effectué un paiement de " + demande.getMontant() + " MRU via "
                + libelleMode(demande.getModePaiement())
                + " pour la facture " + facture.getNumero()
                + " (" + eleve.getNomComplet() + "). À valider.";
        comptabiliteJournalService.notifierDemandePaiement(demande.getId(), message);

        NotificationParent notifParent = new NotificationParent();
        notifParent.setTuteur(tuteur);
        notifParent.setEleve(eleve);
        notifParent.setType("DEMANDE_PAIEMENT");
        notifParent.setTitre("Transmis au comptable");
        notifParent.setMessage(
                "Votre paiement de " + demande.getMontant() + " MRU pour " + facture.getNumero()
                        + " a été transmis au comptable pour validation.");
        notificationParentRepository.save(notifParent);

        log.info("Demande #{} passée en EN_ATTENTE — comptable notifié", demandeId);
    }

    private String libelleMode(String mode) {
        return switch (mode) {
            case "BANQUE" -> "application bancaire";
            case "MOBILE_MONEY" -> "Mobile Money";
            case "CHEQUE" -> "chèque";
            default -> mode.toLowerCase();
        };
    }

    private String formatNom(Tuteur tuteur) {
        String prenom = tuteur.getPrenom();
        String nom = tuteur.getNom();
        if (prenom == null || prenom.isBlank()) return nom;
        return prenom + " " + nom;
    }
}
