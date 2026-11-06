package mr.ecoli.planification.dtos;

public record QuotaControleDto(
        Long id,
        Long typeControleId,
        String typeControleCode,
        String typeControleLibelle,
        Integer nombrePrevu
) {}
