package mr.ecoli.comptabilite.services;

import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import mr.ecoli.common.exception.ResourceNotFoundException;
import mr.ecoli.dao.entities.Paiement;
import mr.ecoli.dao.repositories.InscriptionRepository;
import mr.ecoli.dao.repositories.PaiementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class RecuPdfService {

    private final PaiementRepository paiementRepository;
    private final InscriptionRepository inscriptionRepository;

    public RecuPdfService(PaiementRepository paiementRepository, InscriptionRepository inscriptionRepository) {
        this.paiementRepository = paiementRepository;
        this.inscriptionRepository = inscriptionRepository;
    }

    public byte[] genererRecuPdf(Long paiementId) {
        Paiement p = paiementRepository.findById(paiementId)
                .orElseThrow(() -> new ResourceNotFoundException("Paiement introuvable : " + paiementId));
        var facture = p.getFacture();
        var eleve = facture.getEleve();
        String classe = inscriptionRepository.findActiveForEleve(eleve.getId())
                .map(i -> i.getClasse().getNom())
                .orElse("—");
        String agent = p.getComptable() != null
                ? p.getComptable().getUtilisateur().getPrenom() + " "
                        + p.getComptable().getUtilisateur().getNom()
                : "—";

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document doc = new Document();
            PdfWriter.getInstance(doc, out);
            doc.open();
            Font title = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Font body = FontFactory.getFont(FontFactory.HELVETICA, 12);
            doc.add(new Paragraph("RECU N° " + (p.getNumeroRecu() != null ? p.getNumeroRecu() : p.getId()), title));
            doc.add(new Paragraph(" "));
            doc.add(new Paragraph("Etudiant : " + eleve.getNomComplet(), body));
            doc.add(new Paragraph("Classe : " + classe, body));
            doc.add(new Paragraph("Facture : " + facture.getNumero(), body));
            doc.add(new Paragraph(
                    "Montant paye : " + p.getMontant() + " MRU", body));
            doc.add(new Paragraph(
                    "Date : "
                            + p.getDatePaiement().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                    body));
            doc.add(new Paragraph("Mode : " + p.getModePaiement(), body));
            doc.add(new Paragraph("Agent : " + agent, body));
            doc.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Erreur generation PDF", e);
        }
    }
}
