package mr.ecoli.comptabilite.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record DemandePaiementDto(
        Long id,
        Long factureId,
        String factureNumero,
        String parentNom,
        String parentTelephone,
        String eleveNom,
        String niveau,
        BigDecimal montant,
        LocalDate datePaiement,
        String modePaiement,
        String reference,
        String preuveReference,
        String statut,
        String motifRefus,
        Long paiementId,
        String numeroRecu,
        String agentNom,
        LocalDateTime dateCreation,
        LocalDateTime dateTraitement
) {}
