package mr.ecoli.planification.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record QuotasControlesRequest(
        @NotNull Long trimestreId,
        @NotNull Long niveauId,
        @NotNull @Valid List<QuotaControleItem> quotas
) {
    public record QuotaControleItem(
            @NotNull Long typeControleId,
            @NotNull @Min(0) Integer nombrePrevu
    ) {}
}
