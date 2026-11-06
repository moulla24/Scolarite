package mr.ecoli.comptabilite.dtos;

import java.time.LocalDateTime;

public record NotificationComptableDto(
        Long id,
        String type,
        String titre,
        String message,
        boolean lu,
        Long paiementId,
        Long demandePaiementId,
        LocalDateTime dateCreation
) {}
