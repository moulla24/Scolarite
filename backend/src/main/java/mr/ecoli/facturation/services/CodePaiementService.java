package mr.ecoli.facturation.services;

import org.springframework.stereotype.Service;

@Service
public class CodePaiementService {

    public String fromNumeroFacture(String numeroFacture) {
        if (numeroFacture == null || numeroFacture.isBlank()) {
            throw new IllegalArgumentException("Numéro de facture requis.");
        }
        if (numeroFacture.startsWith("FAC-")) {
            return numeroFacture.replace("FAC-", "PAY-");
        }
        return "PAY-" + numeroFacture;
    }
}
