package mr.ecoli.facturation.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PaiementDto(
        Long id,
        Long factureId,
        String factureNumero,
        String parentNom,
        String eleveNom,
        String niveau,
        BigDecimal montant,
        LocalDate datePaiement,
        String modePaiement,
        String reference,
        String numeroRecu,
        String agentNom
) {}
