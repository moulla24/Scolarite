package mr.ecoli.parentapp.dtos;

import java.time.LocalDateTime;

public record ParentNotificationDto(
        Long id,
        String type,
        String titre,
        String message,
        boolean lu,
        LocalDateTime dateCreation,
        Long eleveId,
        String eleveNom
) {}
