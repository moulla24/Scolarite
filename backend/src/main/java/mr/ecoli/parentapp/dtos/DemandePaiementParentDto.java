package mr.ecoli.parentapp.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record DemandePaiementParentDto(
        Long id,
        Long factureId,
        String factureNumero,
        String eleveNom,
        BigDecimal montant,
        LocalDate datePaiement,
        String modePaiement,
        String reference,
        String preuveReference,
        String statut,
        String motifRefus,
        Long paiementId,
        String numeroRecu,
        LocalDateTime dateCreation,
        LocalDateTime dateTraitement
) {}
