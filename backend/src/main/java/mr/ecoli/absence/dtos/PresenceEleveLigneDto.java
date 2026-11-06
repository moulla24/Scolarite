package mr.ecoli.absence.dtos;

public record PresenceEleveLigneDto(
        Long eleveId,
        String nomComplet,
        String statut,
        String motif
) {}
