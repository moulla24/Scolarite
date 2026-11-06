package mr.ecoli.planification.services;

import java.math.BigDecimal;
import java.util.List;
import mr.ecoli.common.EntityReferenceService;
import mr.ecoli.common.exception.BadRequestException;
import mr.ecoli.common.mapper.EntityMapper;
import mr.ecoli.dao.entities.TypeControle;
import mr.ecoli.dao.repositories.TypeControleRepository;
import mr.ecoli.planification.dtos.TypeControleDto;
import mr.ecoli.planification.dtos.TypeControleRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TypeControleService {

    private final TypeControleRepository typeControleRepository;
    private final EntityMapper mapper;
    private final EntityReferenceService refs;

    public TypeControleService(
            TypeControleRepository typeControleRepository,
            EntityMapper mapper,
            EntityReferenceService refs) {
        this.typeControleRepository = typeControleRepository;
        this.mapper = mapper;
        this.refs = refs;
    }

    @Transactional(readOnly = true)
    public List<TypeControleDto> listTypesControles(Long niveauId) {
        return typeControleRepository.findAllFiltered(niveauId).stream()
                .map(mapper::toTypeControleDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public TypeControleDto getTypeControle(Long id) {
        return mapper.toTypeControleDto(refs.requireTypeControle(id));
    }

    public TypeControleDto createTypeControle(TypeControleRequest request) {
        ensureUniqueCode(request.niveauId(), request.code(), null);
        TypeControle t = new TypeControle();
        apply(t, request);
        return mapper.toTypeControleDto(typeControleRepository.save(t));
    }

    public TypeControleDto updateTypeControle(Long id, TypeControleRequest request) {
        TypeControle t = refs.requireTypeControle(id);
        ensureUniqueCode(request.niveauId(), request.code(), id);
        apply(t, request);
        return mapper.toTypeControleDto(typeControleRepository.save(t));
    }

    public void deleteTypeControle(Long id) {
        typeControleRepository.delete(refs.requireTypeControle(id));
    }

    private void apply(TypeControle t, TypeControleRequest request) {
        t.setNiveau(refs.requireNiveau(request.niveauId()));
        t.setCode(request.code().trim().toUpperCase());
        t.setLibelle(request.libelle().trim());
        t.setNoteMax(request.noteMax() != null ? request.noteMax() : BigDecimal.valueOf(20));
        t.setOrdre(request.ordre() != null ? request.ordre() : 0);
        t.setActif(request.actif());
    }

    private void ensureUniqueCode(Long niveauId, String code, Long excludeId) {
        if (typeControleRepository.existsByNiveauIdAndCodeIgnoreCase(niveauId, code.trim())) {
            TypeControle existing = typeControleRepository.findAllFiltered(niveauId).stream()
                    .filter(t -> t.getCode().equalsIgnoreCase(code.trim()))
                    .findFirst()
                    .orElse(null);
            if (existing != null && (excludeId == null || !existing.getId().equals(excludeId))) {
                throw new BadRequestException("Un type de contrôle avec ce code existe déjà pour ce niveau.");
            }
        }
    }
}
