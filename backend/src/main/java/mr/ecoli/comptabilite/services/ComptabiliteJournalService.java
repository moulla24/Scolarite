package mr.ecoli.comptabilite.services;

import java.util.List;
import java.time.YearMonth;
import mr.ecoli.dao.entities.Comptable;
import mr.ecoli.dao.entities.DepenseScolaire;
import mr.ecoli.dao.entities.EcritureComptable;
import mr.ecoli.dao.entities.Facture;
import mr.ecoli.dao.entities.NotificationComptable;
import mr.ecoli.dao.entities.Paiement;
import mr.ecoli.dao.entities.PaiementPersonnel;
import mr.ecoli.dao.entities.Personnel;
import mr.ecoli.dao.entities.Utilisateur;
import mr.ecoli.dao.repositories.ComptableRepository;
import mr.ecoli.dao.repositories.EcritureComptableRepository;
import mr.ecoli.dao.repositories.NotificationComptableRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ComptabiliteJournalService {

    private final EcritureComptableRepository ecritureRepository;
    private final NotificationComptableRepository notificationRepository;
    private final ComptableRepository comptableRepository;

    public ComptabiliteJournalService(
            EcritureComptableRepository ecritureRepository,
            NotificationComptableRepository notificationRepository,
            ComptableRepository comptableRepository) {
        this.ecritureRepository = ecritureRepository;
        this.notificationRepository = notificationRepository;
        this.comptableRepository = comptableRepository;
    }

    public void enregistrerPaiement(Paiement paiement) {
        Facture facture = paiement.getFacture();
        var eleve = facture.getEleve();

        EcritureComptable ecriture = new EcritureComptable();
        ecriture.setDateEcriture(paiement.getDatePaiement());
        ecriture.setNumeroPiece("EC-" + paiement.getId());
        ecriture.setLibelle("Encaissement frais scolaires — " + facture.getNumero());
        ecriture.setCompteDebit("512");
        ecriture.setCompteCredit("706");
        ecriture.setMontant(paiement.getMontant());
        ecriture.setReferenceType("PAIEMENT");
        ecriture.setReferenceId(paiement.getId());
        ecritureRepository.save(ecriture);

        String message = "Paiement de " + paiement.getMontant() + " MRU pour la facture "
                + facture.getNumero() + " (" + eleve.getNomComplet() + ") via "
                + paiement.getModePaiement() + ".";

        notifierComptables("PAIEMENT_RECU", "Paiement reçu", message, paiement.getId(), null);
    }

    public void notifierDemandePaiement(Long demandeId, String message) {
        notifierComptables("PAIEMENT_A_VALIDER", "Paiement à valider", message, null, demandeId);
    }

    public void enregistrerPaiePersonnel(PaiementPersonnel paiement) {
        Personnel personnel = paiement.getPersonnel();
        YearMonth ym = YearMonth.from(paiement.getMois());

        EcritureComptable ecriture = new EcritureComptable();
        ecriture.setDateEcriture(paiement.getDatePaiement());
        ecriture.setNumeroPiece("PAIE-" + paiement.getId());
        ecriture.setLibelle("Salaire " + ym.getMonthValue() + "/" + ym.getYear()
                + " — " + personnel.getNomComplet() + " (" + personnel.getCategorie() + ")");
        ecriture.setCompteDebit("641");
        ecriture.setCompteCredit("512");
        ecriture.setMontant(paiement.getMontant());
        ecriture.setReferenceType("PAIE_PERSONNEL");
        ecriture.setReferenceId(paiement.getId());
        ecritureRepository.save(ecriture);

        String message = "Salaire de " + paiement.getMontant() + " MRU versé à "
                + personnel.getNomComplet() + " pour " + ym.getMonthValue() + "/" + ym.getYear() + ".";
        notifierComptables("PAIE_PERSONNEL", "Salaire versé", message, null, null);
    }

    public void enregistrerDepense(DepenseScolaire depense) {
        EcritureComptable ecriture = new EcritureComptable();
        ecriture.setDateEcriture(depense.getDateDepense());
        ecriture.setNumeroPiece("DEP-" + depense.getId());
        ecriture.setLibelle("Dépense " + depense.getCategorie() + " — " + depense.getDescription());
        ecriture.setCompteDebit("641");
        ecriture.setCompteCredit("512");
        ecriture.setMontant(depense.getMontant());
        ecriture.setReferenceType("DEPENSE");
        ecriture.setReferenceId(depense.getId());
        ecritureRepository.save(ecriture);
        depense.setEcritureId(ecriture.getId());
    }

    private void notifierComptables(
            String type, String titre, String message, Long paiementId, Long demandePaiementId) {
        List<Comptable> comptables = comptableRepository.findAll();
        for (Comptable comptable : comptables) {
            Utilisateur u = comptable.getUtilisateur();
            if (!u.isActif()) continue;
            NotificationComptable notif = new NotificationComptable();
            notif.setUtilisateur(u);
            notif.setPaiementId(paiementId);
            notif.setDemandePaiementId(demandePaiementId);
            notif.setType(type);
            notif.setTitre(titre);
            notif.setMessage(message);
            notificationRepository.save(notif);
        }
    }
}
