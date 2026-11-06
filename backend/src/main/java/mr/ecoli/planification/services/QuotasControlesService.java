package mr.ecoli.planification.services;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import mr.ecoli.common.EntityReferenceService;
import mr.ecoli.common.exception.BadRequestException;
import mr.ecoli.common.mapper.EntityMapper;
import mr.ecoli.dao.entities.TrimestreTypeControle;
import mr.ecoli.dao.entities.TypeControle;
import mr.ecoli.dao.repositories.TrimestreTypeControleRepository;
import mr.ecoli.dao.repositories.TypeControleRepository;
import mr.ecoli.planification.dtos.QuotaControleDto;
import mr.ecoli.planification.dtos.QuotasControlesRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class QuotasControlesService {

    private final TrimestreTypeControleRepository quotaRepository;
    private final TypeControleRepository typeControleRepository;
    private final EntityMapper mapper;
    private final EntityReferenceService refs;

    public QuotasControlesService(
            TrimestreTypeControleRepository quotaRepository,
            TypeControleRepository typeControleRepository,
            EntityMapper mapper,
            EntityReferenceService refs) {
        this.quotaRepository = quotaRepository;
        this.typeControleRepository = typeControleRepository;
        this.mapper = mapper;
        this.refs = refs;
    }

    @Transactional(readOnly = true)
    public List<QuotaControleDto> listQuotas(Long trimestreId, Long niveauId) {
        refs.requireTrimestre(trimestreId);
        refs.requireNiveau(niveauId);

        List<TypeControle> types = typeControleRepository.findAllFiltered(niveauId);
        Map<Long, TrimestreTypeControle> existing = quotaRepository.findByTrimestreAndNiveau(trimestreId, niveauId)
                .stream()
                .collect(Collectors.toMap(q -> q.getTypeControle().getId(), Function.identity()));

        return types.stream()
                .map(type -> mapper.toQuotaControleDto(type, existing.get(type.getId())))
                .toList();
    }

    public List<QuotaControleDto> saveQuotas(QuotasControlesRequest request) {
        refs.requireTrimestre(request.trimestreId());
        refs.requireNiveau(request.niveauId());

        Map<Long, TypeControle> typesById = typeControleRepository.findAllFiltered(request.niveauId()).stream()
                .collect(Collectors.toMap(TypeControle::getId, Function.identity()));

        for (QuotasControlesRequest.QuotaControleItem item : request.quotas()) {
            TypeControle type = typesById.get(item.typeControleId());
            if (type == null) {
                throw new BadRequestException("Type de contrôle invalide pour ce niveau : " + item.typeControleId());
            }

            var existing = quotaRepository.findByTrimestreIdAndTypeControleId(
                    request.trimestreId(), item.typeControleId());

            if (item.nombrePrevu() == 0) {
                existing.ifPresent(quotaRepository::delete);
                continue;
            }

            TrimestreTypeControle quota = existing.orElseGet(() -> {
                TrimestreTypeControle q = new TrimestreTypeControle();
                q.setTrimestre(refs.requireTrimestre(request.trimestreId()));
                q.setTypeControle(type);
                return q;
            });
            quota.setNombrePrevu(item.nombrePrevu().shortValue());
            quotaRepository.save(quota);
        }

        return listQuotas(request.trimestreId(), request.niveauId());
    }
}
